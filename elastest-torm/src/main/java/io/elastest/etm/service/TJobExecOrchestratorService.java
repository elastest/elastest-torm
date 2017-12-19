package io.elastest.etm.service;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.springframework.web.client.RestClientException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

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
import io.elastest.etm.service.ElasticsearchService.IndexAlreadyExistException;
import io.elastest.etm.model.SutExecution.DeployStatusEnum;
import io.elastest.etm.model.SutSpecification.ManagedDockerType;
import io.elastest.etm.model.SutSpecification.SutTypeEnum;

@Service
public class TJobExecOrchestratorService {
    private static final Logger logger = LoggerFactory
            .getLogger(TJobExecOrchestratorService.class);

    @Value("${et.edm.elasticsearch.api}")
    private String elasticsearchHost;

    private final DockerService2 dockerService;
    private final DockerComposeService dockerComposeService;
    private final TestSuiteRepository testSuiteRepo;
    private final TestCaseRepository testCaseRepo;

    private final TJobExecRepository tJobExecRepositoryImpl;

    private DatabaseSessionManager dbmanager;
    private final EsmService esmService;
    private SutService sutService;
    private ElasticsearchService elasticsearchService;

    public TJobExecOrchestratorService(DockerService2 dockerService,
            TestSuiteRepository testSuiteRepo, TestCaseRepository testCaseRepo,
            TJobExecRepository tJobExecRepositoryImpl,
            DatabaseSessionManager dbmanager, EsmService esmService,
            SutService sutService, DockerComposeService dockerComposeService,
            ElasticsearchService elasticsearchService) {
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
    }

    public TJobExecution executeExternalJob(TJobExecution tJobExec) {
        dbmanager.bindSession();
        tJobExec = tJobExecRepositoryImpl.findOne(tJobExec.getId());

        String resultMsg = "Initializing";
        tJobExec.setResultMsg(resultMsg);
        tJobExecRepositoryImpl.save(tJobExec);

        initTSS(tJobExec, tJobExec.getTjob().getSelectedServices());
        setTJobExecEnvVars(tJobExec);

        // Start Test
        resultMsg = "Executing Test";
        updateTJobExecResultStatus(tJobExec,
                TJobExecution.ResultEnum.EXECUTING_TEST, resultMsg);
        return tJobExec;
    }

    @Async
    public Future<Void> executeTJob(TJobExecution tJobExec,
            String tJobServices) {
        dbmanager.bindSession();
        tJobExec = tJobExecRepositoryImpl.findOne(tJobExec.getId());

        createESIndex(tJobExec);

        String resultMsg = "Initializing";
        tJobExec.setResultMsg(resultMsg);
        tJobExecRepositoryImpl.save(tJobExec);

        initTSS(tJobExec, tJobServices);

        setTJobExecEnvVars(tJobExec);

        DockerExecution dockerExec = new DockerExecution(tJobExec);

        try {
            // Create queues and load basic services
            dockerService.loadBasicServices(dockerExec);

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

            logger.info("Ending Execution...");
            // End and purge services
            endAllExecs(dockerExec);

            saveFinishStatus(tJobExec, dockerExec);
        } catch (TJobStoppedException e) {
            // Stop exception
        } catch (Exception e) {
            logger.error("Error during Test execution", e);
            if (!"end error".equals(e.getMessage())) {
                resultMsg = "Error";
                updateTJobExecResultStatus(tJobExec,
                        TJobExecution.ResultEnum.ERROR, resultMsg);
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
                    logger.error("Exception deprovisino TSS: {}",
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

        if (tJobExec.getTestSuite() != null) {
            finishStatus = tJobExec.getTestSuite().getFinalStatus();

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

                    String instanceId = esmService.provisionServiceInstanceSync(
                            service.get("id").toString().replaceAll("\"", ""),
                            tJobExec);

                    tJobExec.getServicesInstances().add(instanceId);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setTJobExecEnvVars(TJobExecution tJobExec) {
        for (String tSSInstanceId : tJobExec.getServicesInstances()) {
            SupportServiceInstance ssi = esmService.gettJobServicesInstances()
                    .get(tSSInstanceId);
            tJobExec.getTssEnvVars()
                    .putAll(esmService.getTSSInstanceEnvVars(ssi, false));
        }
    }

    /**
     * 
     * @param tJobExec
     */
    private void deprovideServices(TJobExecution tJobExec) {
        logger.info("Start the service deprovision.");
        List<String> instancesAux = new ArrayList<String>();
        if (tJobExec.getServicesInstances().size() > 0) {
            instancesAux = new ArrayList<String>(
                    tJobExec.getServicesInstances());
        } else if (esmService.gettSSIByTJobExecAssociated()
                .get(tJobExec.getId()) != null) {
            instancesAux = new ArrayList<String>(esmService
                    .gettSSIByTJobExecAssociated().get(tJobExec.getId()));
        }

        logger.debug("TSS list size: {}", esmService
                .gettSSIByTJobExecAssociated().get(tJobExec.getId()).size());
        for (String instanceId : instancesAux) {
            esmService.deprovisionServiceInstance(instanceId, tJobExec.getId());
            logger.debug("TSS Instance id to deprovide: {}", instanceId);
        }

        logger.info("Start the services check.");
        while (!tJobExec.getServicesInstances().isEmpty()) {
            for (String instanceId : instancesAux) {
                if (!esmService.isInstanceUp(instanceId)) {
                    tJobExec.getServicesInstances().remove(instanceId);
                    logger.info("Service {} removed from TJob.", instanceId);
                }
            }
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
            if (sut.getManagedDockerType() == ManagedDockerType.IMAGE) {
                startSutByDockerImage(dockerExec);
            }
            // By Docker Compose
            else {
                startSutByDockerCompose(dockerExec);
            }
            sutExec.setDeployStatus(SutExecution.DeployStatusEnum.DEPLOYED);

            String sutContainerId = dockerExec.getAppContainerId();
            String sutIP = dockerService.getContainerIp(sutContainerId,
                    dockerExec);

            // If port is defined, wait for SuT ready
            if (sut.getPort() != null) {
                String sutPort = sut.getPort();

                resultMsg = "Waiting for SuT service ready in port " + sutPort;
                logger.info(resultMsg);
                updateTJobExecResultStatus(tJobExec,
                        TJobExecution.ResultEnum.WAITING_SUT, resultMsg);

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
        dockerComposeYml = setLoggingToDockerComposeYml(dockerComposeYml,
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
                    .getContainerIdByName(container.getName(), dockerExec);

            try {
                // Insert into ElasTest network
                dockerService.insertIntoNetwork(dockerExec.getNetwork(),
                        containerId);
            } catch (Exception e) {
                logger.warn("Cannot insert container {} into network {}",
                        containerId, dockerExec.getNetwork(), e);
            }
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

    public String setLoggingToDockerComposeYml(String dockerComposeYml,
            String composeProjectName, DockerExecution dockerExec)
            throws Exception {
        YAMLFactory yf = new YAMLFactory();
        ObjectMapper mapper = new ObjectMapper(yf);
        Object object;
        try {
            object = mapper.readValue(dockerComposeYml, Object.class);

            Map<String, HashMap<String, HashMap<String, HashMap>>> dockerComposeMap = (HashMap) object;
            Map<String, HashMap<String, HashMap>> servicesMap = dockerComposeMap
                    .get("services");
            for (HashMap.Entry<String, HashMap<String, HashMap>> service : servicesMap
                    .entrySet()) {

                HashMap<String, HashMap> serviceContent = service.getValue();
                String loggingKey = "logging";
                if (serviceContent.containsKey(loggingKey)) {
                    serviceContent.remove(loggingKey);
                }
                HashMap<String, Object> loggingContent = new HashMap<String, Object>();
                loggingContent.put("driver", "syslog");

                HashMap<String, Object> loggingOptionsContent = new HashMap<String, Object>();
                loggingOptionsContent.put("syslog-address", "tcp://"
                        + dockerService.getLogstashHost(dockerExec) + ":5001");
                loggingOptionsContent.put("tag",
                        composeProjectName + "_" + service.getKey() + "_exec");

                loggingContent.put("options", loggingOptionsContent);

                serviceContent.put(loggingKey, loggingContent);
            }

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

    public void endSutExec(DockerExecution dockerExec) throws Exception {
        SutSpecification sut = dockerExec.gettJobexec().getTjob().getSut();

        // If it's Managed Sut, and container is created
        if (sut.getSutType() != SutTypeEnum.DEPLOYED) {
            updateSutExecDeployStatus(dockerExec, DeployStatusEnum.UNDEPLOYING);

            if (sut.getManagedDockerType() == ManagedDockerType.IMAGE) {
                dockerService.endContainer(dockerExec,
                        dockerService.getSutName(dockerExec));
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

    public void updateSutExecDeployStatus(DockerExecution dockerExec,
            DeployStatusEnum status) {
        SutExecution sutExec = dockerExec.getSutExec();

        if (sutExec != null) {
            sutExec.setDeployStatus(status);
        }
        dockerExec.setSutExec(sutExec);
    }

    public void endCheckSutExec(DockerExecution dockerExec) {
        dockerService.endContainer(dockerExec,
                dockerService.getCheckName(dockerExec));
    }

    /***************************/
    /**** TJob Exec Methods ****/
    /***************************/

    public void endTestExec(DockerExecution dockerExec) {
        dockerService.endContainer(dockerExec,
                dockerService.getTestName(dockerExec));
    }

    private void updateTJobExecResultStatus(TJobExecution tJobExec,
            ResultEnum result, String msg) {
        tJobExec.setResult(result);
        tJobExec.setResultMsg(msg);
        tJobExecRepositoryImpl.save(tJobExec);
    }

    public void saveTestResults(List<ReportTestSuite> testSuites,
            TJobExecution tJobExec) {
        System.out.println("Saving test results " + tJobExec.getId());

        TestSuite tSuite;
        TestCase tCase;
        if (testSuites != null && testSuites.size() > 0) {
            ReportTestSuite reportTestSuite = testSuites.get(0);
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
                tCase.setName(reportTestCase.getName());
                tCase.setTime(reportTestCase.getTime());
                tCase.setFailureDetail(reportTestCase.getFailureDetail());
                tCase.setFailureErrorLine(reportTestCase.getFailureErrorLine());
                tCase.setFailureMessage(reportTestCase.getFailureMessage());
                tCase.setFailureType(reportTestCase.getFailureType());
                tCase.setTestSuite(tSuite);

                testCaseRepo.save(tCase);
            }

            testSuiteRepo.save(tSuite);
            tJobExec.setTestSuite(tSuite);
        }
    }

    public void createESIndex(TJobExecution tJobExec) {
        logger.info("Creating ES indices...");
        String[] indicesList = tJobExec.getLogIndicesList();
        for (String index : indicesList) {
            // Create Index
            String url = elasticsearchHost + "/" + index;
            logger.info("Creating index: {}", index);

            String body = "{ \"mappings\": {"
                    + "\"components\": { \"properties\": { \"component\": { \"type\": \"text\", \"fields\": { \"keyword\": { \"type\": \"keyword\" } } } } },"
                    + "\"streams\": { \"properties\": { \"stream\": { \"type\": \"text\", \"fields\": { \"keyword\": { \"type\": \"keyword\" } } } } },"
                    + "\"levels\": { \"properties\": { \"level\": { \"type\": \"text\", \"fields\": { \"keyword\": { \"type\": \"keyword\" } } } } },"
                    + "\"types\": { \"properties\": { \"type\": { \"type\": \"text\", \"fields\": { \"keyword\": { \"type\": \"keyword\" } } } } }"
                    + "} }";

            try {
                elasticsearchService.putCall(url, body);
            } catch (IndexAlreadyExistException e) {
                logger.error("Index {} already exist", index, e);
            } catch (RestClientException e) {
                logger.error("Error creating index {}", index, e);
            } finally {
                // Enable Fielddata for components, streams and levels
                enableESFieldData(index, url, "component");
                enableESFieldData(index, url, "stream");
                enableESFieldData(index, url, "level");
                enableESFieldData(index, url, "type");
            }
            logger.info("Index {} created", index);
        }
        logger.info("ES indices created!");
    }

    public void enableESFieldData(String index, String url, String field) {
        logger.info("Enabling FieldData for {} in index {}", field, index);
        elasticsearchService.enableFieldData(url, field);
    }
}
