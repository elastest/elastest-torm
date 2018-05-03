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

import org.apache.maven.plugins.surefire.report.ReportTestCase;
import org.apache.maven.plugins.surefire.report.ReportTestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.github.dockerjava.api.model.Container;

import io.elastest.epm.client.json.DockerComposeCreateProject;
import io.elastest.epm.client.json.DockerContainerInfo.DockerContainer;
import io.elastest.epm.client.service.DockerComposeService;
import io.elastest.etm.dao.TJobExecRepository;
import io.elastest.etm.dao.TestCaseRepository;
import io.elastest.etm.dao.TestSuiteRepository;
import io.elastest.etm.model.Parameter;
import io.elastest.etm.model.SupportServiceInstance;
import io.elastest.etm.model.SutExecution;
import io.elastest.etm.model.SutSpecification;
import io.elastest.etm.model.TJobExecution;
import io.elastest.etm.model.TJobExecution.ResultEnum;
import io.elastest.etm.model.TestCase;
import io.elastest.etm.model.TestSuite;
import io.elastest.etm.service.DockerService2.ContainersListActionEnum;
import io.elastest.etm.model.SutExecution.DeployStatusEnum;
import io.elastest.etm.model.SutSpecification.CommandsOptionEnum;
import io.elastest.etm.model.SutSpecification.ManagedDockerType;
import io.elastest.etm.model.SutSpecification.SutTypeEnum;

@Service
public class TJobExecOrchestratorService {
    private static final Logger logger = LoggerFactory
            .getLogger(TJobExecOrchestratorService.class);

    @Value("${elastest.docker.network}")
    private String elastestDockerNetwork;

    @Value("${et.etm.logstash.container.name}")
    private String etEtmLogstashContainerName;

    @Value("${test.case.start.msg.prefix}")
    public String tcStartMsgPrefix;

    @Value("${test.case.finish.msg.prefix}")
    public String tcFinishMsgPrefix;

    private final DockerService2 dockerService;
    private final DockerComposeService dockerComposeService;
    private final TestSuiteRepository testSuiteRepo;
    private final TestCaseRepository testCaseRepo;

    private final TJobExecRepository tJobExecRepositoryImpl;

    private DatabaseSessionManager dbmanager;
    private final EsmService esmService;
    private SutService sutService;
    private ElasticsearchService elasticsearchService;

    private EtmContextService etmContextService;

    public TJobExecOrchestratorService(DockerService2 dockerService,
            TestSuiteRepository testSuiteRepo, TestCaseRepository testCaseRepo,
            TJobExecRepository tJobExecRepositoryImpl,
            DatabaseSessionManager dbmanager, EsmService esmService,
            SutService sutService, DockerComposeService dockerComposeService,
            ElasticsearchService elasticsearchService,
            EtmContextService etmContextService) {
        super();
        this.dockerService = dockerService;
        this.testSuiteRepo = testSuiteRepo;
        this.testCaseRepo = testCaseRepo;
        this.tJobExecRepositoryImpl = tJobExecRepositoryImpl;
        this.dbmanager = dbmanager;
        this.esmService = esmService;
        this.sutService = sutService;
        this.dockerComposeService = dockerComposeService;
        this.elasticsearchService = elasticsearchService;
        this.etmContextService = etmContextService;
    }

    @Async
    public void executeExternalJob(TJobExecution tJobExec) {
        dbmanager.bindSession();
        tJobExec = tJobExecRepositoryImpl.findOne(tJobExec.getId());
        elasticsearchService
                .createMonitoringIndex(tJobExec.getMonitoringIndicesList());

        String resultMsg = "Initializing";
        tJobExec.setResultMsg(resultMsg);
        tJobExecRepositoryImpl.save(tJobExec);

        initTSS(tJobExec, tJobExec.getTjob().getSelectedServices());
        setTJobExecEnvVars(tJobExec, true, false);

        // Start Test
        resultMsg = "Executing Test";
        updateTJobExecResultStatus(tJobExec,
                TJobExecution.ResultEnum.EXECUTING_TEST, resultMsg);
    }

    @Async
    public Future<Void> executeTJob(TJobExecution tJobExec,
            String tJobServices) {
        dbmanager.bindSession();
        tJobExec = tJobExecRepositoryImpl.findOne(tJobExec.getId());

        elasticsearchService
                .createMonitoringIndex(tJobExec.getMonitoringIndicesList());

        String resultMsg = "Initializing";
        tJobExec.setResultMsg(resultMsg);
        tJobExecRepositoryImpl.save(tJobExec);

        initTSS(tJobExec, tJobServices);
        setTJobExecEnvVars(tJobExec, false, false);
        tJobExecRepositoryImpl.save(tJobExec);

        DockerExecution dockerExec = new DockerExecution(tJobExec);

        try {
            // Create queues and load basic services
            dockerService.loadBasicServices(dockerExec);

            resultMsg = "Starting Dockbeat to get metrics...";
            updateTJobExecResultStatus(tJobExec,
                    TJobExecution.ResultEnum.IN_PROGRESS, resultMsg);

            // Start Dockbeat
            dockerService.startDockbeat(dockerExec);

            // Start SuT if it's necessary
            if (dockerExec.isWithSut()) {
                initSut(dockerExec);
            }

            List<ReportTestSuite> testSuites;

            // Start Test
            resultMsg = "Executing Test";
            updateTJobExecResultStatus(tJobExec,
                    TJobExecution.ResultEnum.EXECUTING_TEST, resultMsg);

            dockerService.createTestContainer(dockerExec);
            testSuites = dockerService.startTestContainer(dockerExec);

            resultMsg = "Waiting for Test Results";
            updateTJobExecResultStatus(tJobExec,
                    TJobExecution.ResultEnum.WAITING, resultMsg);
            saveTestResults(testSuites, tJobExec);

            tJobExec.setEndDate(new Date());

            logger.info("Ending Execution...");
            // End and purge services
            endAllExecs(dockerExec);

            saveFinishStatus(tJobExec, dockerExec);
        } catch (TJobStoppedException e) {
            logger.debug("TJob Stopped");
            // Stop exception
        } catch (Exception e) {
            logger.error("Error during Test execution", e);
            if (!"end error".equals(e.getMessage())) {
                resultMsg = "Error";
                updateTJobExecResultStatus(tJobExec,
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

    public TJobExecution forceEndExecution(TJobExecution tJobExec) {
        tJobExec = tJobExecRepositoryImpl.findOne(tJobExec.getId());
        DockerExecution dockerExec = new DockerExecution(tJobExec);
        dockerService.configureDocker(dockerExec);
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
            updateTJobExecResultStatus(tJobExec,
                    TJobExecution.ResultEnum.SUCCESS, resultMsg);
        } else {
            String resultMsg = "Stopped";
            updateTJobExecResultStatus(tJobExec,
                    TJobExecution.ResultEnum.STOPPED, resultMsg);
        }

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
        updateTJobExecResultStatus(tJobExec, finishStatus, resultMsg);
    }

    public void endAllExecs(DockerExecution dockerExec) throws Exception {
        try {
            endTestExec(dockerExec);
            if (dockerExec.isWithSut()) {
                endSutExec(dockerExec);
            }
            endDockbeatExec(dockerExec);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("end error"); // TODO Customize Exception
        }
    }

    /*********************/
    /**** TSS methods ****/
    /*********************/

    private void initTSS(TJobExecution tJobExec, String tJobServices) {
        String resultMsg = "";

        if (tJobServices != null && tJobServices != "") {
            provideServices(tJobServices, tJobExec);
        }

        Map<String, SupportServiceInstance> tSSInstAssocToTJob = new HashMap<>();
        tJobExec.getServicesInstances().forEach((tSSInstId) -> {
            tSSInstAssocToTJob.put(tSSInstId,
                    esmService.gettJobServicesInstances().get(tSSInstId));
        });

        logger.info("Waiting for associated TSS");
        resultMsg = "Waiting for Test Support Services";
        updateTJobExecResultStatus(tJobExec,
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

    /**
     * 
     * @param tJobServices
     * @param tJobExec
     */
    private void provideServices(String tJobServices, TJobExecution tJobExec) {
        logger.info("Start the service provision.");
        String resultMsg = "Starting Test Support Service: ";
        ObjectMapper mapper = new ObjectMapper();
        try {
            List<ObjectNode> services = Arrays
                    .asList(mapper.readValue(tJobServices, ObjectNode[].class));
            for (ObjectNode service : services) {
                if (service.get("selected").toString()
                        .equals(Boolean.toString(true))) {

                    updateTJobExecResultStatus(tJobExec,
                            TJobExecution.ResultEnum.STARTING_TSS,
                            resultMsg + service.get("name").toString()
                                    .replaceAll("\"", ""));

                    String instanceId = esmService
                            .provisionTJobExecServiceInstanceSync(service
                                    .get("id").toString().replaceAll("\"", ""),
                                    tJobExec);

                    tJobExec.getServicesInstances().add(instanceId);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        // catch (RuntimeException re) {
        // logger.error("Error provisioning TSS", re);
        // }
    }

    private void setTJobExecEnvVars(TJobExecution tJobExec,
            boolean externalTJob, boolean withPublicPrefix) {
        // Get TSS Env Vars
        for (String tSSInstanceId : tJobExec.getServicesInstances()) {
            SupportServiceInstance ssi = esmService.gettJobServicesInstances()
                    .get(tSSInstanceId);
            tJobExec.getEnvVars().putAll(esmService.getTSSInstanceEnvVars(ssi,
                    externalTJob, withPublicPrefix));
        }

        // Get monitoring Env Vars
        tJobExec.getEnvVars().putAll(
                etmContextService.getTJobExecMonitoringEnvVars(tJobExec));
    }

    /**
     * 
     * @param tJobExec
     */
    public void deprovideServices(TJobExecution tJobExec) {
        logger.info("Start the service deprovision.");
        List<String> instancesAux = new ArrayList<String>();
        if (tJobExec.getServicesInstances().size() > 0) {
            logger.debug("Deprovide TJob's TSSs stored in the TJob object");
            instancesAux = new ArrayList<String>(
                    tJobExec.getServicesInstances());
        } else if (esmService.gettSSIByTJobExecAssociated()
                .get(tJobExec.getId()) != null) {
            logger.debug("Deprovide TJob's TSSs stored in the EsmService");
            instancesAux = new ArrayList<String>(esmService
                    .gettSSIByTJobExecAssociated().get(tJobExec.getId()));
        }

        logger.debug("TSS list size: {}", esmService
                .gettSSIByTJobExecAssociated().get(tJobExec.getId()).size());
        for (String instanceId : instancesAux) {
            esmService.deprovisionTJobExecServiceInstance(instanceId,
                    tJobExec.getId());
            logger.debug("TSS Instance id to deprovide: {}", instanceId);
        }
    }

    /**********************/
    /**** SuT Methods ****/
    /**
     * @throws Exception
     ********************/

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

        String resultMsg = "Executing dockerized SuT";
        updateTJobExecResultStatus(tJobExec,
                TJobExecution.ResultEnum.EXECUTING_SUT, resultMsg);

        logger.info(resultMsg + " " + dockerExec.getExecutionId());
        SutExecution sutExec = sutService.createSutExecutionBySut(sut);
        try {
            // By Docker Image
            if (sut.getManagedDockerType() != ManagedDockerType.COMPOSE) {
                startSutByDockerImage(dockerExec);
            }
            // By Docker Compose
            else {
                startSutByDockerCompose(dockerExec);
            }
            sutExec.setDeployStatus(SutExecution.DeployStatusEnum.DEPLOYED);

            String sutContainerId = dockerExec.getAppContainerId();
            String sutIP = dockerService.getContainerIpWithDockerExecution(
                    sutContainerId, dockerExec);

            // If port is defined, wait for SuT ready
            if (sut.getPort() != null) {
                String sutPort = sut.getPort();

                resultMsg = "Waiting for SuT service ready in port " + sutPort;
                logger.info(resultMsg);
                updateTJobExecResultStatus(tJobExec,
                        TJobExecution.ResultEnum.WAITING_SUT, resultMsg);

                // If is Sut In new Container
                if (sut.isSutInNewContainer()) {
                    sutIP = this.waitForSutInContainer(dockerExec, 480000); // 8min
                }

                // Wait for SuT started
                dockerService.checkSut(dockerExec, sutIP, sutPort);
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
            sutPrefix = this.dockerService.getSutPrefix(dockerExec);
            isDockerCompose = true;

        }
        // If is unique Docker image Sut
        else if (sut
                .getCommandsOption() == CommandsOptionEnum.IN_NEW_CONTAINER) {
            containerName = dockerService.getSutPrefix(dockerExec);
            sutPrefix = containerName;
        }
        // Wait for created
        this.dockerService.waitForContainerCreated(containerName, dockerExec,
                timeout);

        // Insert main sut/service into ET network
        this.dockerService.insertIntoNetwork(dockerExec.getNetwork(),
                this.dockerService.getContainerIdByName(containerName));
        // Get Main sut/service ip from ET network
        String sutIp = dockerService.waitForContainerIpWithDockerExecution(
                containerName, dockerExec, timeout);

        // Add containers to dockerService list
        if (isDockerCompose) {
            List<Container> containersList = this.dockerService
                    .getContainersCreatedSinceId(
                            dockerExec.getAppContainerId());
            this.dockerService.getContainersByNamePrefixByGivenList(
                    containersList, sutPrefix, ContainersListActionEnum.ADD);
        } else {
            String containerId = this.dockerService
                    .getContainerIdByName(containerName);
            this.dockerService.insertCreatedContainer(containerId,
                    containerName);
        }
        return sutIp;
    }

    public String getCurrentExecSutMainServiceName(SutSpecification sut,
            DockerExecution dockerExec) {
        return dockerService.getSutPrefix(dockerExec) + "_"
                + sut.getMainService() + "_1";
    }

    public void startSutByDockerImage(DockerExecution dockerExec)
            throws TJobStoppedException {
        // Create container
        dockerService.createSutContainer(dockerExec);
        // Start container
        dockerService.startSutcontainer(dockerExec);
    }

    public void startSutByDockerCompose(DockerExecution dockerExec)
            throws Exception {
        SutSpecification sut = dockerExec.gettJobexec().getTjob().getSut();
        String mainService = sut.getMainService();
        String composeProjectName = dockerService.getSutName(dockerExec);
        // Because docker-compose-ui api removes underscores '_'
        String containerPrefix = composeProjectName.replaceAll("_", "");

        // TMP replace sut exec and logstash sut tcp
        String dockerComposeYml = sut.getSpecification();
        dockerComposeYml = setElasTestConfigToDockerComposeYml(dockerComposeYml,
                composeProjectName, dockerExec);
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

        // Create Containers
        boolean created = dockerComposeService.createProject(project);

        // Start Containers
        if (!created) {
            throw new Exception(
                    "Sut docker compose containers are not created");
        }

        dockerComposeService.startProject(composeProjectName);

        for (DockerContainer container : dockerComposeService
                .getContainers(composeProjectName).getContainers()) {
            String containerId = dockerService
                    .getContainerIdByName(container.getName());

            // Insert container into containers list
            dockerService.insertCreatedContainer(containerId,
                    container.getName());
            // If is main service container, set app id
            if (container.getName()
                    .equals(containerPrefix + "_" + mainService + "_1")) {
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
    public String setElasTestConfigToDockerComposeYml(String dockerComposeYml,
            String composeProjectName, DockerExecution dockerExec)
            throws Exception {
        YAMLFactory yf = new YAMLFactory();
        ObjectMapper mapper = new ObjectMapper(yf);
        Object object;
        try {
            object = mapper.readValue(dockerComposeYml, Object.class);

            Map<String, HashMap<String, HashMap>> dockerComposeMap = (HashMap) object;
            Map<String, HashMap> servicesMap = dockerComposeMap.get("services");
            for (HashMap.Entry<String, HashMap> service : servicesMap
                    .entrySet()) {
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

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public HashMap.Entry<String, HashMap> setLoggingToDockerComposeYmlService(
            HashMap.Entry<String, HashMap> service, String composeProjectName,
            DockerExecution dockerExec) {
        HashMap<String, HashMap> serviceContent = service.getValue();
        String loggingKey = "logging";
        // If service has logging, remove it
        if (serviceContent.containsKey(loggingKey)) {
            serviceContent.remove(loggingKey);
        }
        HashMap<String, Object> loggingContent = new HashMap<String, Object>();
        loggingContent.put("driver", "syslog");

        HashMap<String, Object> loggingOptionsContent = new HashMap<String, Object>();
        loggingOptionsContent.put("syslog-address",
                "tcp://" + dockerService.getContainerIpByNetwork(
                        etEtmLogstashContainerName, elastestDockerNetwork)
                        + ":5000");
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
        dockerService.removeSutVolumeFolder(dockerExec);
        // If it's Managed Sut, and container is created
        if (sut.getSutType() != SutTypeEnum.DEPLOYED) {
            updateSutExecDeployStatus(dockerExec, DeployStatusEnum.UNDEPLOYING);

            if (sut.getManagedDockerType() != ManagedDockerType.COMPOSE) {
                if (sut.isSutInNewContainer()) {
                    endSutInContainer(dockerExec);
                }
                dockerService
                        .endContainer(dockerService.getSutName(dockerExec));
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
        String composeProjectName = dockerService.getSutName(dockerExec);
        dockerComposeService.stopProject(composeProjectName);
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
            sutPrefix = this.dockerService.getSutPrefix(dockerExec);
            isDockerCompose = true;
        }
        // If is unique Docker image Sut
        else if (sut
                .getCommandsOption() == CommandsOptionEnum.IN_NEW_CONTAINER) {
            containerName = dockerService.getSutPrefix(dockerExec);
            sutPrefix = containerName;
        }

        // Add containers to dockerService list
        if (isDockerCompose) {
            List<Container> containersList = this.dockerService
                    .getContainersCreatedSinceId(
                            dockerExec.getAppContainerId());
            this.dockerService.getContainersByNamePrefixByGivenList(
                    containersList, sutPrefix, ContainersListActionEnum.REMOVE);
        } else {
            this.dockerService.endContainer(containerName);
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

    public void endCheckSutExec(DockerExecution dockerExec) {
        dockerService.endContainer(dockerService.getCheckName(dockerExec));
    }

    /******************/
    /**** Dockbeat ****/
    /******************/

    public void endDockbeatExec(DockerExecution dockerExec) {
        dockerService.endContainer(
                dockerService.getDockbeatContainerName(dockerExec));
    }

    /***************************/
    /**** TJob Exec Methods ****/
    /***************************/

    public void endTestExec(DockerExecution dockerExec) {
        dockerService.endContainer(dockerService.getTestName(dockerExec));
    }

    private void updateTJobExecResultStatus(TJobExecution tJobExec,
            ResultEnum result, String msg) {
        tJobExec.setResult(result);
        tJobExec.setResultMsg(msg);
        tJobExecRepositoryImpl.save(tJobExec);
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
                    // Remove parentheses
                    tCase.cleanNameAndSet(reportTestCase.getName());
                    tCase.setTime(reportTestCase.getTime());
                    tCase.setFailureDetail(reportTestCase.getFailureDetail());
                    tCase.setFailureErrorLine(
                            reportTestCase.getFailureErrorLine());
                    tCase.setFailureMessage(reportTestCase.getFailureMessage());
                    tCase.setFailureType(reportTestCase.getFailureType());
                    tCase.setTestSuite(tSuite);
                    try {
                        Date startDate = this.elasticsearchService
                                .findFirstMsgAndGetTimestamp(
                                        tJobExec.getMonitoringIndex(),
                                        this.tcStartMsgPrefix + tCase.getName()
                                                + " ",
                                        "test");
                        tCase.setStartDate(startDate);

                        Date endDate = this.elasticsearchService
                                .findFirstMsgAndGetTimestamp(
                                        tJobExec.getMonitoringIndex(),
                                        this.tcFinishMsgPrefix + tCase.getName()
                                                + " ",
                                        "test");
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
