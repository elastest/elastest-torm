package io.elastest.etm.service;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import javax.annotation.PostConstruct;

import org.apache.maven.plugins.surefire.report.ReportTestCase;
import org.apache.maven.plugins.surefire.report.ReportTestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.Container;

import io.elastest.epm.client.json.DockerComposeCreateProject;
import io.elastest.epm.client.json.DockerContainerInfo.DockerContainer;
import io.elastest.epm.client.service.DockerComposeService;
import io.elastest.epm.client.service.DockerService.ContainersListActionEnum;
import io.elastest.etm.dao.TJobExecRepository;
import io.elastest.etm.dao.TestCaseRepository;
import io.elastest.etm.dao.TestSuiteRepository;
import io.elastest.etm.model.EusExecutionData;
import io.elastest.etm.model.Parameter;
import io.elastest.etm.model.SupportServiceInstance;
import io.elastest.etm.model.SutExecution;
import io.elastest.etm.model.SutExecution.DeployStatusEnum;
import io.elastest.etm.model.SutSpecification;
import io.elastest.etm.model.SutSpecification.CommandsOptionEnum;
import io.elastest.etm.model.SutSpecification.ManagedDockerType;
import io.elastest.etm.model.SutSpecification.SutTypeEnum;
import io.elastest.etm.model.TJobExecution;
import io.elastest.etm.model.TJobExecution.ResultEnum;
import io.elastest.etm.model.TJobSupportService;
import io.elastest.etm.model.TestCase;
import io.elastest.etm.model.TestSuite;
import io.elastest.etm.utils.UtilTools;

@Service
public class TJobExecOrchestratorService {
    private static final Logger logger = LoggerFactory
            .getLogger(TJobExecOrchestratorService.class);

    @Value("${exec.mode}")
    public String execMode;

    @Value("${elastest.docker.network}")
    private String elastestDockerNetwork;

    private final DockerEtmService dockerEtmService;
    private final DockerComposeService dockerComposeService;
    private final TestSuiteRepository testSuiteRepo;
    private final TestCaseRepository testCaseRepo;

    private final TJobExecRepository tJobExecRepositoryImpl;

    private DatabaseSessionManager dbmanager;
    private final EsmService esmService;
    private SutService sutService;
    private MonitoringServiceInterface monitoringService;

    private EtmContextService etmContextService;
    

    public TJobExecOrchestratorService(DockerEtmService dockerEtmService,
            TestSuiteRepository testSuiteRepo, TestCaseRepository testCaseRepo,
            TJobExecRepository tJobExecRepositoryImpl,
            DatabaseSessionManager dbmanager, EsmService esmService,
            SutService sutService, DockerComposeService dockerComposeService,
            MonitoringServiceInterface monitoringService,
            EtmContextService etmContextService) {
        super();
        this.dockerEtmService = dockerEtmService;
        this.testSuiteRepo = testSuiteRepo;
        this.testCaseRepo = testCaseRepo;
        this.tJobExecRepositoryImpl = tJobExecRepositoryImpl;
        this.dbmanager = dbmanager;
        this.esmService = esmService;
        this.sutService = sutService;
        this.dockerComposeService = dockerComposeService;
        this.monitoringService = monitoringService;
        this.etmContextService = etmContextService;
    }

    @PostConstruct
    private void init() {
        dbmanager.bindSession();
        manageZombieJobs();
        dbmanager.unbindSession();
    }

    public void manageZombieJobs() {
        logger.info("Looking for zombie Jobs");
        // Clean non finished TJob Executions
        List<TJobExecution> notFinishedOrExecutedExecs = this.tJobExecRepositoryImpl
                .findByResults(ResultEnum.getNotFinishedOrExecutedResultList());
        if (notFinishedOrExecutedExecs != null) {
            logger.info("Cleaning non finished TJob Executions ({} total):",
                    notFinishedOrExecutedExecs.size());
            for (TJobExecution currentExec : notFinishedOrExecutedExecs) {
                logger.debug("Cleaning TJobExecution {}...",
                        currentExec.getId());
                try {
                    currentExec = this.forceEndExecution(currentExec);
                } catch (Exception e) {
                    logger.error("Error on force end execution of {}",
                            currentExec);
                }
                if (!currentExec.isFinished()) {
                    String resultMsg = "Stopped";
                    dockerEtmService.updateTJobExecResultStatus(currentExec,
                            TJobExecution.ResultEnum.STOPPED, resultMsg);
                }
            }
        }
        logger.info("End Manage zombie Jobs");
    }

    @Async
    public void executeExternalJob(TJobExecution tJobExec) {
        dbmanager.bindSession();
        tJobExec = tJobExecRepositoryImpl.findById(tJobExec.getId()).get();
        monitoringService
                .createMonitoringIndex(tJobExec.getMonitoringIndicesList());

        String resultMsg = "Initializing";
        tJobExec.setResultMsg(resultMsg);
        tJobExecRepositoryImpl.save(tJobExec);

        try {
            initTSS(tJobExec, tJobExec.getTjob().getSelectedServices());
        } catch (Exception e) {
            // TODO Auto-generated catch block
            logger.error("Error on init TSS", e);
        }
        setTJobExecEnvVars(tJobExec, true, false);

        // Start Test
        resultMsg = "Executing Test";
        dockerEtmService.updateTJobExecResultStatus(tJobExec,
                TJobExecution.ResultEnum.EXECUTING_TEST, resultMsg);
        dbmanager.unbindSession();
    }

    @Async
    public Future<Void> executeTJob(TJobExecution tJobExec,
            String tJobServices) {
        dbmanager.bindSession();
        tJobExec = tJobExecRepositoryImpl.findById(tJobExec.getId()).get();

        monitoringService
                .createMonitoringIndex(tJobExec.getMonitoringIndicesList());

        String resultMsg = "Initializing";
        tJobExec.setResultMsg(resultMsg);
        tJobExec = tJobExecRepositoryImpl.save(tJobExec);

        DockerExecution dockerExec = new DockerExecution(tJobExec);
        try {
            initTSS(tJobExec, tJobServices);
            setTJobExecEnvVars(tJobExec, false, false);
            tJobExec = tJobExecRepositoryImpl.save(tJobExec);

            dockerExec.settJobexec(tJobExec);
            // Create queues and load basic services
            dockerEtmService.loadBasicServices(dockerExec);

            resultMsg = "Starting Dockbeat to get metrics...";
            dockerEtmService.updateTJobExecResultStatus(tJobExec,
                    TJobExecution.ResultEnum.IN_PROGRESS, resultMsg);

            // Start Dockbeat
            dockerEtmService.startDockbeat(dockerExec);

            // Start SuT if it's necessary
            if (dockerExec.isWithSut()) {
                initSut(dockerExec);
            }

            List<ReportTestSuite> testSuites;

            // Start Test
            resultMsg = "Preparing Test";
            dockerEtmService.updateTJobExecResultStatus(tJobExec,
                    TJobExecution.ResultEnum.EXECUTING_TEST, resultMsg);

            testSuites = dockerEtmService
                    .createAndStartTestContainer(dockerExec);

            resultMsg = "Waiting for Test Results";
            dockerEtmService.updateTJobExecResultStatus(tJobExec,
                    TJobExecution.ResultEnum.WAITING, resultMsg);
            saveTestResults(testSuites, tJobExec);

            tJobExec.setEndDate(new Date());

            logger.info("Ending Execution...");
            // End and purge services
            endAllExecs(dockerExec);

            saveFinishStatus(tJobExec, dockerExec);
        } catch (TJobStoppedException e) {
            logger.warn("TJob Stopped");
            // Stop exception
        } catch (Exception e) {
            logger.error("Error during Test execution", e);
            if (!"end error".equals(e.getMessage())) {
                resultMsg = "Error";
                dockerEtmService.updateTJobExecResultStatus(tJobExec,
                        TJobExecution.ResultEnum.ERROR, resultMsg);

                tJobExec.setEndDate(new Date());
                try {
                    endAllExecs(dockerExec);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            } else {
                saveFinishStatus(tJobExec, dockerExec);
            }
        } finally {
            if (tJobServices != null && tJobServices != "") {
                try {
                    deprovideServices(tJobExec);
                } catch (Exception e) {
                    logger.error("Exception deprovision TSS: {}",
                            e.getMessage());
                    // TODO Customize Exception
                }
            }
        }

        // Setting execution data
        tJobExec.setSutExecution(dockerExec.getSutExec());

        // Saving execution data
        tJobExecRepositoryImpl.save(tJobExec);
        dbmanager.unbindSession();
        return new AsyncResult<Void>(null);
    }

    public TJobExecution forceEndExecution(TJobExecution tJobExec)
            throws Exception {
        tJobExec = tJobExecRepositoryImpl.findById(tJobExec.getId()).get();
        DockerExecution dockerExec = new DockerExecution(tJobExec);
        dockerEtmService.configureDocker(dockerExec);
        try {
            endAllExecs(dockerExec);
        } catch (TJobStoppedException e) {
            // Stop exception
        } catch (Exception e) {
            logger.error("Exception during Force End execution", e);
        }

        // Deprovision all TSS associated
        logger.debug("Requesting the TSS deprovision.");
        try {
            deprovideServices(tJobExec);
        } catch (Exception e) {
            logger.error("Exception during Deprovide Services");
        }

        if (tJobExec.getTjob().isExternal()) {
            String resultMsg = "Success";
            dockerEtmService.updateTJobExecResultStatus(tJobExec,
                    TJobExecution.ResultEnum.SUCCESS, resultMsg);
        } else {
            String resultMsg = "Stopped";
            dockerEtmService.updateTJobExecResultStatus(tJobExec,
                    TJobExecution.ResultEnum.STOPPED, resultMsg);
        }

        tJobExec = tJobExecRepositoryImpl.findById(tJobExec.getId()).get();
        return tJobExec;
    }

    public void saveFinishStatus(TJobExecution tJobExec,
            DockerExecution dockerExec) {
        String resultMsg = "";
        ResultEnum finishStatus = ResultEnum.SUCCESS;

        if (tJobExec.getTestSuites() != null
                && tJobExec.getTestSuites().size() > 0) {
            for (TestSuite testSuite : tJobExec.getTestSuites()) {
                if (testSuite.getFinalStatus() == ResultEnum.FAIL) { // Else
                                                                     // always
                                                                     // success
                    finishStatus = testSuite.getFinalStatus();
                    break;
                }
            }

        } else {
            if (dockerExec.getTestContainerExitCode() != 0) {
                finishStatus = ResultEnum.FAIL;
            }
        }

        resultMsg = "Finished: " + finishStatus;
        dockerEtmService.updateTJobExecResultStatus(tJobExec, finishStatus,
                resultMsg);
    }

    public void endAllExecs(DockerExecution dockerExec) throws Exception {
        try {
            endTestExec(dockerExec);
            if (dockerExec.isWithSut()) {
                endSutExec(dockerExec);
            }
            endDockbeatExec(dockerExec);
        } catch (Exception e) {
            logger.error("Error on end all execs");
            throw new Exception("end error", e); // TODO Customize Exception
        }
    }

    /* ******************* */
    /* *** TSS methods *** */
    /* ******************* */

    private void initTSS(TJobExecution tJobExec, String tJobServices)
            throws Exception {
        String resultMsg = "";

        if (tJobServices != null && tJobServices != "") {
            provideServices(tJobServices, tJobExec);
        }

        Map<String, SupportServiceInstance> tSSInstAssocToTJob = new HashMap<>();
        tJobExec.getServicesInstances().forEach((tSSInstId) -> {
            tSSInstAssocToTJob.put(tSSInstId,
                    esmService.gettJobServicesInstances().get(tSSInstId));
        });

        logger.info("Waiting for associated TSS {}", tSSInstAssocToTJob);
        resultMsg = "Waiting for Test Support Services";
        dockerEtmService.updateTJobExecResultStatus(tJobExec,
                TJobExecution.ResultEnum.WAITING_TSS, resultMsg);
        while (!tSSInstAssocToTJob.isEmpty()) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ie) {
                logger.error("Interrupted Exception {}: " + ie.getMessage());
            }
            tJobExec.getServicesInstances().forEach((tSSInstId) -> {
                if (esmService.checkInstanceUrlIsUp(
                        esmService.gettJobServicesInstances().get(tSSInstId))) {
                    tSSInstAssocToTJob.remove(tSSInstId);
                }
            });
        }

        logger.info("TSS availabes");
    }

    private void provideServices(String tJobServices, TJobExecution tJobExec)
            throws Exception {
        logger.info("Start the service provision.");
        String resultMsg = "Starting Test Support Service: ";
        try {
            TJobSupportService[] tssArray = UtilTools.convertJsonStringToObj(
                    tJobServices, TJobSupportService[].class,
                    Include.NON_EMPTY);
            List<TJobSupportService> services = Arrays.asList(tssArray);
            // Start EMS first if is selected
            List<TJobSupportService> servicesWithoutEMS = provideEmsTssIfSelected(
                    services, tJobExec);

            for (TJobSupportService service : servicesWithoutEMS) {
                if (service.isSelected()) {
                    dockerEtmService.updateTJobExecResultStatus(tJobExec,
                            TJobExecution.ResultEnum.STARTING_TSS,
                            resultMsg + service.getName());

                    this.provideService(service, tJobExec);
                }
            }
        } catch (IOException e) {
            throw new Exception("Error on provide TSS", e);
        }
        // catch (RuntimeException re) {
        // logger.error("Error provisioning TSS", re);
        // }
    }

    private String provideService(TJobSupportService service,
            TJobExecution tJobExec) {
        String instanceId = esmService.provisionTJobExecServiceInstanceSync(
                service.getId(), tJobExec);

        tJobExec.getServicesInstances().add(instanceId);
        return instanceId;
    }

    public List<TJobSupportService> provideEmsTssIfSelected(
            List<TJobSupportService> services, TJobExecution tJobExec)
            throws JsonParseException, JsonMappingException, IOException {
        List<TJobSupportService> servicesWithoutEMS = new ArrayList<>(services);
        int pos = 0;
        for (TJobSupportService service : services) {
            if (service.getName().toLowerCase().equals("ems")
                    && service.isSelected()) {
                String instanceId = this.provideService(service, tJobExec);
                servicesWithoutEMS.remove(pos);
                this.setTJobExecTssEnvVars(tJobExec,
                        tJobExec.getTjob().isExternal(), false, instanceId);
                break;
            }
            pos++;
        }
        return servicesWithoutEMS;
    }

    /*
     * Gets the Env vars of given TJob TSS Instance
     */
    private Map<String, String> getTJobExecTssEnvVars(boolean externalTJob,
            boolean withPublicPrefix, String tSSInstanceId) {
        SupportServiceInstance ssi = esmService.gettJobServicesInstances()
                .get(tSSInstanceId);
        Map<String, String> tssInstanceEnvVars = esmService
                .getTSSInstanceEnvVars(ssi, externalTJob, withPublicPrefix);

        return tssInstanceEnvVars;
    }

    /*
     * Sets the Env vars of given TSS Instance into tJobExec
     */
    private void setTJobExecTssEnvVars(TJobExecution tJobExec,
            boolean externalTJob, boolean withPublicPrefix,
            String tSSInstanceId) {
        Map<String, String> envVars = new HashMap<>();
        envVars.putAll(tJobExec.getEnvVars());
        envVars.putAll(this.getTJobExecTssEnvVars(externalTJob,
                withPublicPrefix, tSSInstanceId));
        tJobExec.setEnvVars(envVars);
    }

    private void setTJobExecEnvVars(TJobExecution tJobExec,
            boolean externalTJob, boolean withPublicPrefix) {
        Map<String, String> envVars = new HashMap<>();
        envVars.putAll(tJobExec.getEnvVars());
        // Get TSS Env Vars
        for (String tSSInstanceId : tJobExec.getServicesInstances()) {
            envVars.putAll(this.getTJobExecTssEnvVars(externalTJob,
                    withPublicPrefix, tSSInstanceId));
        }
        // Get monitoring Env Vars
        envVars.putAll(
                etmContextService.getTJobExecMonitoringEnvVars(tJobExec));

        // In normal mode, tjobs make use of started EUS
        String etEusApiKey = "ET_EUS_API";
        if (execMode.equals("normal") && envVars.containsKey(etEusApiKey)) {
            String eusApi = envVars.get(etEusApiKey);
            if (eusApi != null) {
                EusExecutionData eusExecutionDate = new EusExecutionData(
                        tJobExec, "");
                eusApi = eusApi.endsWith("/") ? eusApi : eusApi + "/";
                eusApi += "/execution/" + eusExecutionDate.getKey() + "/";
                envVars.put(etEusApiKey, eusApi);
            }
        }

        tJobExec.setEnvVars(envVars);
    }

    public void deprovideServices(TJobExecution tJobExec) {
        logger.info("Start the service deprovision.");
        List<String> instancesAux = new ArrayList<String>();
        if (tJobExec.getServicesInstances().size() > 0) {
            logger.debug("Deprovide TJob's TSSs stored in the TJob object");
            instancesAux = tJobExec.getServicesInstances();
        } else if (esmService.gettSSIByTJobExecAssociated()
                .get(tJobExec.getId()) != null) {
            logger.debug("Deprovide TJob's TSSs stored in the EsmService");
            instancesAux = esmService.gettSSIByTJobExecAssociated()
                    .get(tJobExec.getId());
        }

        logger.debug("TSS list size: {}", esmService
                .gettSSIByTJobExecAssociated().get(tJobExec.getId()).size());
        for (String instanceId : instancesAux) {
            esmService.deprovisionTJobExecServiceInstance(instanceId,
                    tJobExec.getId());
            logger.debug("TSS Instance id to deprovide: {}", instanceId);
        }
    }

    /* ******************* */
    /* *** SuT Methods *** */
    /* ******************* */

    public void initSut(DockerExecution dockerExec) throws Exception {
        SutSpecification sut = dockerExec.gettJobexec().getTjob().getSut();
        SutExecution sutExec;

        String sutIP = "";

        // If it's MANAGED SuT
        if (sut.getSutType() != SutTypeEnum.DEPLOYED) {
            try {
                sutExec = startManagedSut(dockerExec);
            } catch (TJobStoppedException e) {
                throw e;
            }
        }
        // If it's DEPLOYED SuT
        else {
            Long currentSutExecId = sut.getCurrentSutExec();
            sutExec = sutService.getSutExecutionById(currentSutExecId);
            sutIP = sut.getSpecification();
            String sutUrl = "http://" + sutIP + ":"
                    + (sut.getPort() != null ? sut.getPort() : "");
            sutExec.setUrl(sutUrl);
            sutExec.setIp(sutIP);
        }

        dockerExec.setSutExec(sutExec);
    }

    private SutExecution startManagedSut(DockerExecution dockerExec)
            throws Exception {
        TJobExecution tJobExec = dockerExec.gettJobexec();
        SutSpecification sut = dockerExec.gettJobexec().getTjob().getSut();

        String resultMsg = "Preparing dockerized SuT";
        dockerEtmService.updateTJobExecResultStatus(tJobExec,
                TJobExecution.ResultEnum.EXECUTING_SUT, resultMsg);
        logger.info(resultMsg + " " + dockerExec.getExecutionId());

        SutExecution sutExec = sutService.createSutExecutionBySut(sut);
        try {
            // By Docker Image
            if (sut.getManagedDockerType() != ManagedDockerType.COMPOSE) {
                logger.debug("Is Sut By Docker Image");
                startSutByDockerImage(dockerExec);
            }
            // By Docker Compose
            else {
                logger.debug("Is Sut By Docker Compose");
                startSutByDockerCompose(dockerExec);
            }
            sutExec.setDeployStatus(SutExecution.DeployStatusEnum.DEPLOYED);

            String sutContainerId = dockerExec.getAppContainerId();
            String sutIP = dockerEtmService.getContainerIpWithDockerExecution(
                    sutContainerId, dockerExec);
            // If port is defined, wait for SuT ready
            if (sut.getPort() != null) {
                String sutPort = sut.getPort();
                resultMsg = "Waiting for dockerized SuT";
                logger.info(resultMsg);
                dockerEtmService.updateTJobExecResultStatus(tJobExec,
                        TJobExecution.ResultEnum.WAITING_SUT, resultMsg);

                // If is Sut In new Container
                if (sut.isSutInNewContainer()) {
                    sutIP = this.waitForSutInContainer(dockerExec, 480000); // 8min
                }

                // Wait for SuT started
                resultMsg = "Waiting for SuT service ready at ip " + sutIP
                        + " and port " + sutPort;
                logger.debug(resultMsg);
                dockerEtmService.checkSut(dockerExec, sutIP, sutPort);
                endCheckSutExec(dockerExec);
            }

            // Save SuTUrl and Ip into sutexec
            String sutUrl = "http://" + sutIP + ":"
                    + (sut.getPort() != null ? sut.getPort() : "");
            sutExec.setUrl(sutUrl);
            sutExec.setIp(sutIP);

        } catch (TJobStoppedException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Exception during TJob execution", e);
            sutExec.setDeployStatus(SutExecution.DeployStatusEnum.ERROR);
            try {
                sutService.modifySutExec(dockerExec.getSutExec());
            } catch (Exception e1) {

            }
            throw e;
        }
        return sutExec;
    }

    public String waitForSutInContainer(DockerExecution dockerExec,
            long timeout) throws Exception {
        SutSpecification sut = dockerExec.gettJobexec().getTjob().getSut();
        String containerName = null;
        String sutPrefix = null;
        boolean isDockerCompose = false;
        // If is Docker compose Sut
        if (sut.getCommandsOption() == CommandsOptionEnum.IN_DOCKER_COMPOSE) {
            containerName = this.getCurrentExecSutMainServiceName(sut,
                    dockerExec);
            sutPrefix = this.dockerEtmService.getSutPrefix(dockerExec);
            isDockerCompose = true;
            logger.debug(
                    "Is SuT in new container With Docker Compose. Main Service Container Name: {}",
                    containerName);
        }
        // If is unique Docker image Sut
        else if (sut
                .getCommandsOption() == CommandsOptionEnum.IN_NEW_CONTAINER) {
            containerName = dockerEtmService.getSutPrefix(dockerExec);
            sutPrefix = containerName;
            logger.debug(
                    "Is SuT in new container With Docker Image. Container Name: {}",
                    containerName);
        }
        // Wait for created
        this.dockerEtmService.dockerService
                .waitForContainerCreated(containerName, timeout);

        String containerId = this.dockerEtmService.dockerService
                .getContainerIdByName(containerName);
        // Insert main sut/service into ET network if it's necessary
        this.dockerEtmService.dockerService
                .insertIntoNetwork(dockerExec.getNetwork(), containerId);

        // Get Main sut/service ip from ET network
        String sutIp = dockerEtmService.waitForContainerIpWithDockerExecution(
                containerName, dockerExec, timeout);

        // Add containers to dockerEtmService list
        if (isDockerCompose) {
            List<Container> containersList = this.dockerEtmService.dockerService
                    .getContainersCreatedSinceId(
                            dockerExec.getAppContainerId());
            this.dockerEtmService.dockerService
                    .getContainersByNamePrefixByGivenList(containersList,
                            sutPrefix, ContainersListActionEnum.ADD,
                            dockerExec.getNetwork());
        } else {
            containerId = this.dockerEtmService.dockerService
                    .getContainerIdByName(containerName);
            this.dockerEtmService.insertCreatedContainer(containerId,
                    containerName);
        }
        return sutIp;
    }

    public String getCurrentExecSutMainServiceName(SutSpecification sut,
            DockerExecution dockerExec) {
        return dockerEtmService.getSutPrefix(dockerExec) + "_"
                + sut.getMainService() + "_1";
    }

    public void startSutByDockerImage(DockerExecution dockerExec)
            throws Exception {
        // Create and Start container
        dockerEtmService.createAndStartSutContainer(dockerExec);
    }

    public void startSutByDockerCompose(DockerExecution dockerExec)
            throws Exception {
        SutSpecification sut = dockerExec.gettJobexec().getTjob().getSut();
        String mainService = sut.getMainService();
        String composeProjectName = dockerEtmService.getSutName(dockerExec);

        // TMP replace sut exec and logstash sut tcp
        String dockerComposeYml = sut.getSpecification();

        // Set logging, network and do pull of images
        dockerComposeYml = prepareElasTestConfigInDockerComposeYml(
                dockerComposeYml, composeProjectName, dockerExec);

        // Environment variables (optional)
        ArrayList<String> envList = new ArrayList<>();
        String envVar;

        // Get Parameters and insert into Env Vars
        for (Parameter parameter : sut.getParameters()) {
            envVar = parameter.getName() + "=" + parameter.getValue();
            envList.add(envVar);
        }

        DockerComposeCreateProject project = new DockerComposeCreateProject(
                composeProjectName, dockerComposeYml, envList);

        String resultMsg = "Starting dockerized SuT";
        dockerEtmService.updateTJobExecResultStatus(dockerExec.gettJobexec(),
                TJobExecution.ResultEnum.EXECUTING_SUT, resultMsg);
        logger.info(resultMsg + " " + dockerExec.getExecutionId());

        // Create Containers
        String pathToSaveTmpYml = esmService
                .getTJobExecFolderPath(dockerExec.gettJobexec());
        boolean created = dockerComposeService.createProject(project,
                pathToSaveTmpYml, false);

        // Start Containers
        if (!created) {
            throw new Exception(
                    "Sut docker compose containers are not created");
        }

        dockerComposeService.startProject(composeProjectName, false);

        for (DockerContainer container : dockerComposeService
                .getContainers(composeProjectName).getContainers()) {
            String containerId = dockerEtmService.dockerService
                    .getContainerIdByName(container.getName());

            // Insert container into containers list
            dockerEtmService.insertCreatedContainer(containerId,
                    container.getName());
            // If is main service container, set app id
            if (container.getName()
                    .equals(composeProjectName + "_" + mainService + "_1")) {
                dockerExec.setAppContainerId(containerId);
            }
        }

        if (dockerExec.getAppContainerId() == null
                || dockerExec.getAppContainerId().isEmpty()) {
            throw new Exception(
                    "Main Sut service from docker compose not started");
        }

    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public String prepareElasTestConfigInDockerComposeYml(
            String dockerComposeYml, String composeProjectName,
            DockerExecution dockerExec) throws Exception {
        YAMLFactory yf = new YAMLFactory();
        ObjectMapper mapper = new ObjectMapper(yf);
        Object object;
        try {
            object = mapper.readValue(dockerComposeYml, Object.class);

            Map<String, HashMap<String, HashMap>> dockerComposeMap = (HashMap) object;
            Map<String, HashMap> servicesMap = dockerComposeMap.get("services");
            for (HashMap.Entry<String, HashMap> service : servicesMap
                    .entrySet()) {
                // Pull images in a local execution
                if (!etmContextService.etMasterSlaveMode ) {
                    this.pullDockerComposeYmlService(service, dockerExec);
                }

                // Set Logging
                service = this.setLoggingToDockerComposeYmlService(service,
                        composeProjectName, dockerExec);

                // Set Elastest Network
                service = this.setNetworkToDockerComposeYmlService(service,
                        composeProjectName, dockerExec);
            }

            dockerComposeMap = this.setNetworkToDockerComposeYmlRoot(
                    dockerComposeMap, composeProjectName, dockerExec);

            StringWriter writer = new StringWriter();

            yf.createGenerator(writer).writeObject(object);
            dockerComposeYml = writer.toString();

            writer.close();
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new Exception(
                    "Error on setting logging into docker compose yml");
        }

        return dockerComposeYml;
    }

    /* Compose Root */

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private Map<String, HashMap<String, HashMap>> setNetworkToDockerComposeYmlRoot(
            Map<String, HashMap<String, HashMap>> dockerComposeMap,
            String composeProjectName, DockerExecution dockerExec) {

        String networksKey = "networks";
        // If service has networks, remove it
        if (dockerComposeMap.containsKey(networksKey)) {
            dockerComposeMap.remove(networksKey);
        }

        HashMap<String, HashMap> networkMap = new HashMap();
        HashMap<String, Boolean> networkOptions = new HashMap<>();
        networkOptions.put("external", true);

        networkMap.put(elastestDockerNetwork, networkOptions);
        dockerComposeMap.put(networksKey, networkMap);

        return dockerComposeMap;

    }

    /* Compose service */

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void pullDockerComposeYmlService(
            HashMap.Entry<String, HashMap> service, DockerExecution dockerExec)
            throws DockerException, InterruptedException, Exception {
        HashMap<String, String> serviceContent = service.getValue();

        String imageKey = "image";
        // If service has image, pull
        if (serviceContent.containsKey(imageKey)) {
            String image = serviceContent.get(imageKey);
            dockerEtmService.pullETExecutionImage(dockerExec, image,
                    service.getKey(), false);
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public HashMap.Entry<String, HashMap> setLoggingToDockerComposeYmlService(
            HashMap.Entry<String, HashMap> service, String composeProjectName,
            DockerExecution dockerExec) throws TJobStoppedException {
        TJobExecution tJobExec = dockerExec.gettJobexec();
        HashMap<String, HashMap> serviceContent = service.getValue();
        String loggingKey = "logging";
        // If service has logging, remove it
        if (serviceContent.containsKey(loggingKey)) {
            serviceContent.remove(loggingKey);
        }
        HashMap<String, Object> loggingContent = new HashMap<String, Object>();
        loggingContent.put("driver", "syslog");

        HashMap<String, Object> loggingOptionsContent = new HashMap<String, Object>();

        String host = "";
        String port = "5000";

        if (tJobExec.getTjob().isSelectedService("ems")) {
            host = tJobExec.getEnvVars().get("ET_EMS_TCP_SUTLOGS_HOST");
            port = tJobExec.getEnvVars().get("ET_EMS_TCP_SUTLOGS_PORT");
        } else {
            try {
                host = dockerEtmService.getLogstashHost();
            } catch (Exception e) {
                throw new TJobStoppedException(
                        "Error on set Logging to Service of docker compose yml:"
                                + e);
            }
        }
        loggingOptionsContent.put("syslog-address",
                "tcp://" + host + ":" + port);

        loggingOptionsContent.put("tag",
                composeProjectName + "_" + service.getKey() + "_exec");

        loggingContent.put("options", loggingOptionsContent);

        serviceContent.put(loggingKey, loggingContent);

        return service;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public HashMap.Entry<String, HashMap> setNetworkToDockerComposeYmlService(
            HashMap.Entry<String, HashMap> service, String composeProjectName,
            DockerExecution dockerExec) {

        HashMap serviceContent = service.getValue();
        String networksKey = "networks";
        // If service has networks, remove it
        if (serviceContent.containsKey(networksKey)) {
            serviceContent.remove(networksKey);
        }

        List<String> networksList = new ArrayList<>();
        networksList.add(elastestDockerNetwork);
        serviceContent.put(networksKey, networksList);

        return service;
    }

    public void endSutExec(DockerExecution dockerExec) throws Exception {
        SutSpecification sut = dockerExec.gettJobexec().getTjob().getSut();
        dockerEtmService.removeSutVolumeFolder(dockerExec);
        // If it's Managed Sut, and container is created
        if (sut.getSutType() != SutTypeEnum.DEPLOYED) {
            updateSutExecDeployStatus(dockerExec, DeployStatusEnum.UNDEPLOYING);

            if (sut.getManagedDockerType() != ManagedDockerType.COMPOSE) {
                if (sut.isSutInNewContainer()) {
                    endSutInContainer(dockerExec);
                }
                dockerEtmService
                        .endContainer(dockerEtmService.getSutName(dockerExec));
            } else {
                endComposedSutExec(dockerExec);
            }
            updateSutExecDeployStatus(dockerExec, DeployStatusEnum.UNDEPLOYED);
        } else {
            logger.info("SuT not ended by ElasTest -> Deployed SuT");
        }
        endCheckSutExec(dockerExec);
    }

    public void endComposedSutExec(DockerExecution dockerExec)
            throws Exception {
        String composeProjectName = dockerEtmService.getSutName(dockerExec);
        dockerComposeService.stopAndRemoveProject(composeProjectName);
    }

    public void endSutInContainer(DockerExecution dockerExec) throws Exception {
        SutSpecification sut = dockerExec.gettJobexec().getTjob().getSut();
        String containerName = null;
        String sutPrefix = null;
        boolean isDockerCompose = false;

        // If is Docker compose Sut
        if (sut.getCommandsOption() == CommandsOptionEnum.IN_DOCKER_COMPOSE) {
            containerName = this.getCurrentExecSutMainServiceName(sut,
                    dockerExec);
            sutPrefix = this.dockerEtmService.getSutPrefix(dockerExec);
            isDockerCompose = true;
        }
        // If is unique Docker image Sut
        else if (sut
                .getCommandsOption() == CommandsOptionEnum.IN_NEW_CONTAINER) {
            containerName = dockerEtmService.getSutPrefix(dockerExec);
            sutPrefix = containerName;
        }

        // Add containers to dockerEtmService list
        if (isDockerCompose) {
            List<Container> containersList = this.dockerEtmService.dockerService
                    .getContainersCreatedSinceId(
                            dockerExec.getAppContainerId());
            this.dockerEtmService.dockerService
                    .getContainersByNamePrefixByGivenList(containersList,
                            sutPrefix, ContainersListActionEnum.REMOVE,
                            dockerExec.getNetwork());
        } else {
            this.dockerEtmService.endContainer(containerName);
        }

    }

    public void updateSutExecDeployStatus(DockerExecution dockerExec,
            DeployStatusEnum status) {
        SutExecution sutExec = dockerExec.getSutExec();

        if (sutExec != null) {
            sutExec.setDeployStatus(status);
        }
        dockerExec.setSutExec(sutExec);
    }

    public void endCheckSutExec(DockerExecution dockerExec) throws Exception {
        dockerEtmService
                .endContainer(dockerEtmService.getCheckName(dockerExec));
    }

    /* **************** */
    /* *** Dockbeat *** */
    /* **************** */

    public void endDockbeatExec(DockerExecution dockerExec) throws Exception {
        dockerEtmService.endContainer(
                dockerEtmService.getDockbeatContainerName(dockerExec));
    }

    /* ************************* */
    /* *** TJob Exec Methods *** */
    /* ************************* */

    public void endTestExec(DockerExecution dockerExec) throws Exception {
        dockerEtmService.endContainer(dockerEtmService.getTestName(dockerExec));
    }

    public void saveTestResults(List<ReportTestSuite> testSuites,
            TJobExecution tJobExec) {
        logger.info("Saving test results {} ", tJobExec.getId());

        TestSuite tSuite;
        TestCase tCase;
        if (testSuites != null && testSuites.size() > 0) {
            for (ReportTestSuite reportTestSuite : testSuites) {
                tSuite = new TestSuite();
                tSuite.setTimeElapsed(reportTestSuite.getTimeElapsed());
                tSuite.setErrors(reportTestSuite.getNumberOfErrors());
                tSuite.setFailures(reportTestSuite.getNumberOfFailures());
                tSuite.setFlakes(reportTestSuite.getNumberOfFlakes());
                tSuite.setSkipped(reportTestSuite.getNumberOfSkipped());
                tSuite.setName(reportTestSuite.getName());
                tSuite.setnumTests(reportTestSuite.getNumberOfTests());

                tSuite = testSuiteRepo.save(tSuite);

                for (ReportTestCase reportTestCase : reportTestSuite
                        .getTestCases()) {
                    tCase = new TestCase();
                    tCase.cleanNameAndSet(reportTestCase.getName());
                    tCase.setTime(reportTestCase.getTime());
                    tCase.setFailureDetail(reportTestCase.getFailureDetail());
                    tCase.setFailureErrorLine(
                            reportTestCase.getFailureErrorLine());
                    tCase.setFailureMessage(reportTestCase.getFailureMessage());
                    tCase.setFailureType(reportTestCase.getFailureType());
                    tCase.setTestSuite(tSuite);
                    try {
                        Date startDate = this.monitoringService
                                .findFirstStartTestMsgAndGetTimestamp(
                                        tJobExec.getMonitoringIndex(),
                                        tCase.getName(), "test");
                        tCase.setStartDate(startDate);

                        Date endDate = this.monitoringService
                                .findFirstFinishTestMsgAndGetTimestamp(
                                        tJobExec.getMonitoringIndex(),
                                        tCase.getName(), "test");
                        tCase.setEndDate(endDate);
                    } catch (Exception e) {
                        logger.debug(
                                "Cannot save start/end date for Test Case {}",
                                tCase.getName(), e);
                    }

                    testCaseRepo.save(tCase);
                }

                tSuite.settJobExec(tJobExec);
                testSuiteRepo.save(tSuite);
                tJobExec.getTestSuites().add(tSuite);
            }
        }
    }

}
