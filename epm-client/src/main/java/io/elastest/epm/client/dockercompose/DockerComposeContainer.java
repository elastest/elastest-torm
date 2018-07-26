package io.elastest.epm.client.dockercompose;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.invoke.MethodHandles.lookup;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.SystemUtils;
import org.slf4j.Logger;
import org.zeroturnaround.exec.InvalidExitValueException;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.ProcessOutput;
import org.zeroturnaround.exec.ProcessResult;
import org.zeroturnaround.exec.stream.slf4j.Slf4jStream;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.base.Splitter;
import com.google.common.collect.Maps;

import io.elastest.epm.client.model.ResourceGroup;
import io.elastest.epm.client.service.EpmService;
import io.elastest.epm.client.service.FilesService;
import io.elastest.epm.client.service.ServiceException;

/**
 * Container which launches Docker Compose, for the purposes of launching a
 * defined set of containers.
 */
public class DockerComposeContainer<SELF extends DockerComposeContainer<SELF>> {
    final Logger logger = getLogger(lookup().lookupClass());

    private final String identifier;
    private final List<File> composeFiles;
    private List<String> composeYmlList = new ArrayList<>();

    private final Map<String, Integer> scalingPreferences = new HashMap<>();
    private boolean pull = false;

    private String project;
    private static final Object MUTEX = new Object();
    private Map<String, String> env = new HashMap<>();

    private List<String> imagesList = new ArrayList<>();
    boolean started = false;

    EpmService epmService;
    FilesService filesService;
    private String basePath;
    /* ******************** */
    /* *** Constructors *** */
    /* ******************** */

    public DockerComposeContainer(File... composeFiles) {
        this(Arrays.asList(composeFiles));
    }

    public DockerComposeContainer(List<File> composeFiles) {
        this(Base58.randomString(6).toLowerCase(), true, null, null, null,
                composeFiles);
    }

    public DockerComposeContainer(String identifier, File... composeFiles) {
        this(identifier, false, null, null, null, Arrays.asList(composeFiles));
    }

    public DockerComposeContainer(String identifier, boolean withUniqueId,
            EpmService epmService, FilesService filesService, String basePath,
            File... composeFiles) {
        this(identifier, withUniqueId, epmService, filesService, basePath,
                Arrays.asList(composeFiles));
    }

    public DockerComposeContainer(String identifier, boolean withUniqueId,
            EpmService epmService, FilesService filesServices, String basePath,
            List<File> composeFiles) {
        this.composeFiles = composeFiles;
        this.identifier = identifier;
        this.epmService = epmService;
        this.filesService = filesServices;
        this.basePath = basePath;

        if (withUniqueId) {
            project = randomProjectId();
        } else {
            project = identifier;
        }
    }

    /* ***************** */
    /* **** Methods **** */
    /* ***************** */

    public void start() throws InterruptedException, ServiceException {
        synchronized (MUTEX) {
            if (pull) {
                runWithCompose("pull");
            }
            // scale before up, so that all scaled instances are available first
            // for linking
            //applyScaling();
            // Run the docker-compose container, which starts up the services
            ProcessResult result = runWithCompose("up -d");
            this.started = result.getExitValue() == 0;
        }
    }

    private ProcessResult runWithCompose(String cmd) throws ServiceException {
        final DockerCompose dockerCompose;
        ProcessResult processResult;

        if (EpmService.etMasterSlaveMode && epmService != null) {
            logger.info("Deploying SUT in a Slave.");
            dockerCompose = new RemoteDockerCompose(composeFiles.get(0),
                    this.composeYmlList.get(0), project, epmService,
                    filesService, basePath);
            processResult = dockerCompose.invoke();
        } else {
            logger.info("Deploying SUT in Master.");
            dockerCompose = new LocalDockerCompose(composeFiles, project);
            processResult = dockerCompose.withCommand(cmd).withEnv(env)
                    .invoke();
        }

        return processResult;
    }

    private void applyScaling() throws ServiceException {
        // Apply scaling
        if (!scalingPreferences.isEmpty()) {
            StringBuilder sb = new StringBuilder("scale");
            for (Map.Entry<String, Integer> scale : scalingPreferences
                    .entrySet()) {
                sb.append(" ").append(scale.getKey()).append("=")
                        .append(scale.getValue());
            }

            runWithCompose(sb.toString());
        }
    }

    public void stop() throws IOException {
        synchronized (MUTEX) {
            try {
                // Kill the services using docker-compose
                runWithCompose("down -v");
            } catch (ServiceException se) {
                throw new IOException(se.getMessage(), se.getCause());
            } finally {
                project = randomProjectId();
            }
        }
    }

    public SELF withScaledService(String serviceBaseName, int numInstances) {
        scalingPreferences.put(serviceBaseName, numInstances);

        return self();
    }

    public SELF withEnv(String key, String value) {
        this.env.put(key, value);
        return self();
    }

    public SELF withEnv(Map<String, String> envs) {
        this.env.putAll(envs);
        return self();
    }

    public SELF withImages(List<String> images) {
        this.imagesList = images;
        return self();
    }

    public List<String> getImagesList() {
        return this.imagesList;
    }

    public List<File> getComposeFiles() {
        return composeFiles;
    }

    public SELF setComposeYmlList(List<String> ymlList) {
        if (ymlList != null) {
            this.composeYmlList = ymlList;
        }
        return self();
    }

    public List<String> getComposeYmlList() {
        return this.composeYmlList;
    }

    public boolean isStarted() {
        return started;
    }

    /**
     * Whether to pull images first.
     *
     * @return this instance, for chaining
     */
    public SELF withPull(boolean pull) {
        this.pull = pull;
        return self();
    }

    @SuppressWarnings("unchecked")
    private SELF self() {
        return (SELF) this;
    }

    private String randomProjectId() {
        return identifier + Base58.randomString(6).toLowerCase();
    }

    @Override
    public String toString() {
        return "DockerComposeContainer [logger=" + logger + ", identifier="
                + identifier + ", composeFiles=" + composeFiles
                + ", composeYmlList=" + composeYmlList + ", scalingPreferences="
                + scalingPreferences + ", pull=" + pull + ", project=" + project
                + ", env=" + env + ", imagesList=" + imagesList + ", started="
                + started + "]";
    }

}

interface DockerCompose {
    String ENV_PROJECT_NAME = "COMPOSE_PROJECT_NAME";
    String ENV_COMPOSE_FILE = "COMPOSE_FILE";

    DockerCompose withCommand(String cmd);

    DockerCompose withEnv(Map<String, String> env);

    ProcessResult invoke();

    default void validateFileList(List<File> composeFiles) {
        checkNotNull(composeFiles);
        checkArgument(!composeFiles.isEmpty(),
                "No docker compose file have been provided");
    }
}

class RemoteDockerCompose implements DockerCompose {
    final Logger logger = getLogger(lookup().lookupClass());

    private final File composeFile;
    private final String composeYml;
    private final String identifier;
    private final EpmService epmService;
    private final FilesService filesService;
    private final String basePath;

    public RemoteDockerCompose(File composeFile, String composeYml,
            String identifier, EpmService epmService, FilesService filesService,
            String basePath) {
        this.composeFile = composeFile;
        this.composeYml = composeYml;
        this.identifier = identifier;
        this.epmService = epmService;
        this.filesService = filesService;
        this.basePath = basePath;
    }

    @Override
    public DockerCompose withCommand(String cmd) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public DockerCompose withEnv(Map<String, String> env) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ProcessResult invoke() {

        String packageDirPath = filesService.createTempFolderName(basePath,
                "compose-package");
        File packageDir = new File(packageDirPath);
        logger.info("Folder's name: {}", packageDirPath);
        packageDir.mkdirs();

        // Prepare metadata file
        String metadataContent;
        try {
            logger.info("Metadata file path: {}",
                    EpmService.composePackageFilePath);
            metadataContent = filesService.readFile(filesService
                    .getFileFromResources(EpmService.composePackageFilePath,
                            "metadata.yaml", basePath));
            logger.info("Metadata: {}", metadataContent);
            // HashMap<String, String> ymlMap = stringYmlToMap(metadataContent);
            // ymlMap.put("pop-name", epmService
            // .getPopName(epmService.getRe().getHostIp(), "compose"));
            // logger.info("New value for the field pop-name: {}",
            // ymlMap.get("pop-name"));

            String metadataFilePath = packageDirPath + "/metadata.yaml";
            File metadataFile = new File(metadataFilePath);
            logger.info("Folder's name: {}", packageDirPath);
            String metadataUpdated = replacePopNameValueInYmlFile(
                    metadataContent);
            // logger.info("New metadata content: {}",
            // simpleYmlMapToString(ymlMap));
            // filesService.writeFileFromString(simpleYmlMapToString(ymlMap),
            // metadataFile);
            filesService.writeFileFromString(metadataUpdated, metadataFile);

        } catch (Exception e) {
            e.printStackTrace();
            throw new ContainerLaunchException(
                    "Error reading the metadata template file.");
        }

        // Creating compose file inside the package directory
        try {
            String composeFilePath = packageDirPath + "/docker-compose.yml";
            File composeFile = new File(composeFilePath);
            filesService.writeFileFromString(composeYml, composeFile);
        } catch (Exception e) {
            throw new ContainerLaunchException(
                    "Error creating the compose file to send to the EPM.");
        }

        // Create package as tar file
        File packageTarFile = null;
        try {
            packageTarFile = filesService.createTarFile(packageDirPath + ".tar",
                    packageDir);
        } catch (IOException e) {
            e.printStackTrace();
            throw new ContainerLaunchException(
                    "Error creating a new package to send to the EPM.");
        }

        // Send package to the EPM
        try {
            ResourceGroup result = epmService.sendPackage(packageTarFile);
        } catch (ServiceException e) {
            throw new ContainerLaunchException(
                    "Error sending a package to the EPM.");
        }

        BigInteger bIng = BigInteger.valueOf(0);
        return new ProcessResult(0, new ProcessOutput(bIng.toByteArray()));
    }

    private String replacePopNameValueInYmlFile(String metadataContent)
            throws Exception {
        if (metadataContent != null && !metadataContent.isEmpty()) {
            YAMLFactory yf = new YAMLFactory();
            ObjectMapper mapper = new ObjectMapper(yf);
            Object object;
            object = mapper.readValue(metadataContent, Object.class);

            HashMap<String, String> metadataMap = (HashMap) object;
            metadataMap.put("pop", epmService
                    .getPopName(epmService.getRe().getHostIp(), "compose"));
            logger.info("New value for the field pop-name: {}",
                    metadataMap.get("pop-name"));

            StringWriter writer = new StringWriter();

            yf.createGenerator(writer).writeObject(metadataMap);
            return writer.toString();

        } else {
            throw new Exception("Error on replace pop-name value");
        }
    }

    private HashMap<String, String> stringYmlToMap(String yml)
            throws Exception {
        if (yml != null && !yml.isEmpty()) {
            YAMLFactory yf = new YAMLFactory();
            ObjectMapper mapper = new ObjectMapper(yf);
            Object object;
            object = mapper.readValue(yml, Object.class);

            return (HashMap) object;
        } else {
            throw new Exception("Error on get yml services: the yml is empty");
        }
    }

    private String simpleYmlMapToString(HashMap<String, String> ymlMap)
            throws IOException {
        YAMLFactory yf = new YAMLFactory();
        StringWriter writer = new StringWriter();

        yf.createGenerator(writer).writeObject(ymlMap);
        return writer.toString();
    }
}

/**
 * Use local Docker Compose binary, if present.
 */
class LocalDockerCompose implements DockerCompose {
    final Logger logger = getLogger(lookup().lookupClass());

    /**
     * Executable name for Docker Compose.
     */
    public static final char UNIX_PATH_SEPERATOR = ':';

    private static final String COMPOSE_EXECUTABLE = SystemUtils.IS_OS_WINDOWS
            ? "docker-compose.exe"
            : "docker-compose";

    private final List<File> composeFiles;
    private final String identifier;
    private String cmd = "";
    private Map<String, String> env = new HashMap<>();

    public LocalDockerCompose(List<File> composeFiles, String identifier) {
        validateFileList(composeFiles);

        this.composeFiles = composeFiles;
        this.identifier = identifier;
    }

    public DockerCompose withCommand(String cmd) {
        this.cmd = cmd;
        return this;
    }

    public DockerCompose withEnv(Map<String, String> env) {
        this.env = env;
        return this;
    }

    public ProcessResult invoke() {
        // bail out early
        if (!CommandLine.executableExists(COMPOSE_EXECUTABLE)) {
            throw new ContainerLaunchException(
                    "Local Docker Compose not found. Is " + COMPOSE_EXECUTABLE
                            + " on the PATH?");
        }

        final Map<String, String> environment = Maps.newHashMap(env);
        environment.put(ENV_PROJECT_NAME, identifier);
        final List<String> absoluteDockerComposeFiles = composeFiles.stream()
                .map(File::getAbsolutePath).collect(toList());

        final String composeFileEnvVariableValue = absoluteDockerComposeFiles
                .stream().collect(joining(UNIX_PATH_SEPERATOR + ""));

        logger.debug("Set env COMPOSE_FILE={}", composeFileEnvVariableValue);

        final File pwd = composeFiles.get(0).getAbsoluteFile().getParentFile()
                .getAbsoluteFile();
        environment.put(ENV_COMPOSE_FILE, composeFileEnvVariableValue);

        logger.info("Local Docker Compose is running command: {}", cmd);

        final List<String> command = Splitter.onPattern(" ").omitEmptyStrings()
                .splitToList(COMPOSE_EXECUTABLE + " " + cmd);

        try {
            ProcessResult processResult = new ProcessExecutor().command(command)
                    .readOutput(true)
                    .redirectOutput(Slf4jStream.of(logger).asInfo())
                    .redirectError(Slf4jStream.of(logger).asError())
                    .environment(environment).directory(pwd).exitValueNormal()
                    .executeNoTimeout();

            logger.info("Docker Compose has finished running");

            return processResult;
        } catch (InvalidExitValueException e) {
            throw new ContainerLaunchException(
                    "Local Docker Compose exited abnormally with code "
                            + e.getExitValue() + " whilst running command: "
                            + cmd);

        } catch (Exception e) {
            throw new ContainerLaunchException(
                    "Error running local Docker Compose command: " + cmd, e);
        }
    }

}
