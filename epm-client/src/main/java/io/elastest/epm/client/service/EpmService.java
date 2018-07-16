package io.elastest.epm.client.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.gson.reflect.TypeToken;

import io.elastest.epm.client.ApiClient;
import io.elastest.epm.client.ApiException;
import io.elastest.epm.client.JSON;
import io.elastest.epm.client.api.KeyApi;
import io.elastest.epm.client.api.PackageApi;
import io.elastest.epm.client.api.PoPApi;
import io.elastest.epm.client.api.WorkerApi;
import io.elastest.epm.client.model.Key;
import io.elastest.epm.client.model.PoP;
import io.elastest.epm.client.model.RemoteEnvironment;
import io.elastest.epm.client.model.ResourceGroup;
import io.elastest.epm.client.model.Worker;
import io.elastest.epm.client.service.ServiceException.ExceptionCode;

@Service
public class EpmService {
    private static final Logger logger = LoggerFactory
            .getLogger(EpmService.class);

    private final int MAX_ATTEMPTS = 10;
    private final int TIME_BETWEEN_ATTEMPTS = 15;

    @Value("${et.epm.packages.path}")
    private String packageFilePath;
    @Value("${et.epm.key.path}")
    private String keyFilePath;
    @Value("${et.master.slave.mode}")
    public boolean etMasterSlaveMode;

    private FilesService filesService;
    private RemoteEnvironment re;

    private PackageApi packageApiInstance;
    private WorkerApi workerApiInstance;
    private KeyApi keyApiInstance;
    private PoPApi popApi;
    private ApiClient apiClient;
    private JSON json;

    private Map<String, String> adapters;

    public enum AdaptersNames {
        DOCKER("docker"), DOCKER_COMPOSE("docker-compose");

        private String name;

        public String getName() {
            return name;
        }

        private AdaptersNames(String name) {
            this.name = name;
        }
    }

    public EpmService(FilesService filesService) {
        apiClient = new ApiClient();
        packageApiInstance = new PackageApi();
        packageApiInstance.setApiClient(apiClient);
        workerApiInstance = new WorkerApi();
        keyApiInstance = new KeyApi();
        popApi = new PoPApi();
        json = new JSON(apiClient);
        this.filesService = filesService;
        adapters = new HashMap<>();

    }

    @PostConstruct
    public void initRemoteenvironment() {
        if (etMasterSlaveMode) {
            logger.info("Creating slave");
            try {
                re = provisionRemoteEnvironment();
            } catch (ServiceException se) {
                etMasterSlaveMode = false;
                se.printStackTrace();
            }
        }
    }

    @PreDestroy
    public void endRemoteenvironment() {
        if (etMasterSlaveMode) {
            logger.info("Removing slave");
            try {
                deprovisionRemoteEnvironment(re);
            } catch (ServiceException se) {
                se.printStackTrace();
            }
        }
    }

    public RemoteEnvironment provisionRemoteEnvironment()
            throws ServiceException {
        logger.info("Provisioning virtual machine.");
        RemoteEnvironment re = new RemoteEnvironment();
        ResourceGroup resourceGroup = null;
        Worker worker = null;

        try {
            // Providing VM
            resourceGroup = registerAdapter(packageFilePath);
            re.setResourceGroup(resourceGroup);
            logger.debug("Virtual machine provided with id: {}",
                    resourceGroup.getId());
            // Registering privated key
            Key key = addKey(
                    filesService.getFileFromResources(keyFilePath, "key.json"));
            logger.debug("Key {} value: {}", key.getName(), key.getKey());
            re.setKey(key);
            int currentAttempts = 0;
            boolean registeredWorker = false;
            while (currentAttempts < MAX_ATTEMPTS && !registeredWorker) {
                logger.debug("Attempts: {}", currentAttempts);
                worker = registerWorker(resourceGroup);
                registeredWorker = worker != null ? true : false;
                if (!registeredWorker) {
                    currentAttempts++;
                    TimeUnit.SECONDS.sleep(TIME_BETWEEN_ATTEMPTS);
                }
            }

            if (!registeredWorker) {
                throw new ServiceException(
                        "Error provisioning a new remote environment",
                        ExceptionCode.ERROR_PROVISIONING_VM);
            }
            re.setWorker(worker);
            re.setHostIp(worker.getIp());
            logger.debug("Worker id: {}", worker.getId());
            adapters.put(AdaptersNames.DOCKER.getName(), installAdapter(
                    worker.getId(), AdaptersNames.DOCKER.getName()));
            adapters.put(AdaptersNames.DOCKER_COMPOSE.getName(), installAdapter(
                    worker.getId(), AdaptersNames.DOCKER_COMPOSE.getName()));

        } catch (ApiException | IOException | InterruptedException
                | ServiceException | URISyntaxException e) {
            logger.error("Error: {} ", e.getMessage());
            if (e instanceof ServiceException) {
                throw (ServiceException) e;
            } else {
                throw new ServiceException(
                        "Error provisioning a new remote environment",
                        e.getCause());
            }
        }

        return re;
    }

    public String deprovisionRemoteEnvironment(RemoteEnvironment re)
            throws ServiceException {
        logger.info("Removing remote environment.");
        try {
            deleteWorker(re.getWorker().getId());
            deleteKey(re.getKey().getId());
            deleteAdapter(re.getResourceGroup().getId());
        } catch (FileNotFoundException | ApiException e) {
            e.printStackTrace();
            throw new ServiceException(
                    "Error removing a new remote environment", e.getCause(),
                    ExceptionCode.ERROR_PROVISIONING_VM);
        }

        return re.getResourceGroup().getId();
    }

    private Key parserKeyFromJsonFile(File key) throws FileNotFoundException {
        InputStream is = new FileInputStream(key);
        Scanner s = new Scanner(is).useDelimiter("\\A");
        String result = s.hasNext() ? s.next() : "";
        result = result.replace("  ", "");
        return json.deserialize(result, new TypeToken<Key>() {
        }.getType());
    }

    public ResourceGroup registerAdapter(String packagePath)
            throws ServiceException {
        logger.info("Registering adapter described in the file: {}",
                packagePath);
        ResourceGroup result = null;
        try {
            File file = filesService.getFileFromResources(packagePath,
                    "m1tub.tar");
            result = packageApiInstance.receivePackage(file);
            logger.debug("New instance id: {} ", result.getId());
            logger.debug(String.valueOf(result));
        } catch (ApiException | IOException | URISyntaxException re) {
            re.printStackTrace();
            throw new ServiceException(
                    "Error provisioning a new remote environment",
                    re.getCause(), ExceptionCode.ERROR_DEPROVISIONING_VM);
        }

        return result;
    }

    public void deleteAdapter(String id) {
        logger.info("Delete adapter: {}", id);
        try {
            packageApiInstance.deletePackage(id);
        } catch (ApiException e) {
            System.err.println(
                    "Exception when calling PackageApi#receivePackage");
            e.printStackTrace();
        }
    }

    public Worker registerWorker(ResourceGroup rg) throws ServiceException {
        Worker worker = new Worker();
        worker.setIp(rg.getVdus().get(0).getIp());
        worker.setUser("ubuntu");
        worker.setEpmIp("localhost");
        worker.setKeyname("tub-ansible");
        worker.passphrase("");
        worker.password("");

        Worker result = null;
        try {
            result = workerApiInstance.registerWorker(worker);
            System.out.println(result);
        } catch (ApiException e) {
            System.err
                    .println("Exception when calling WorkerApi#registerWorker");
            e.printStackTrace();
        }

        return result;
    }

    public String deleteWorker(String id) throws ApiException {
        String result = workerApiInstance.deleteWorker(id);
        return result;
    }

    public Key addKey(File keyFile) throws FileNotFoundException, ApiException {
        Key key = parserKeyFromJsonFile(keyFile);
        return keyApiInstance.addKey(key);
    }

    public String deleteKey(String id)
            throws FileNotFoundException, ApiException {
        return keyApiInstance.deleteKey(id);
    }

    public String installAdapter(String workerId, String type)
            throws ApiException {

        return workerApiInstance.installAdapter(workerId, type);
    }
    
    public String getPopName(String reIp, String popType) throws ServiceException {
        String popName = null;
        try {
            for (PoP pop: popApi.getAllPoPs()) {
                if (pop.getName().equals(popType + "-" + reIp)) {
                    popName = pop.getName();
                    break;
                }
            }
            if (popName == null) {
                throw new ApiException("There isn't any pop with the name provided");
            }
        } catch (ApiException e) {
            throw new ServiceException(
                    e.getMessage(), e.getCause(), ExceptionCode.GENERIC_ERROR);
        }
        return popName;
    }

    public RemoteEnvironment getRe() {
        return re;
    }

    public void setRe(RemoteEnvironment re) {
        this.re = re;
    }
}