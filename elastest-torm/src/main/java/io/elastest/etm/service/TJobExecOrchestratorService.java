package io.elastest.etm.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.plugins.surefire.report.ReportTestCase;
import org.apache.maven.plugins.surefire.report.ReportTestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.elastest.etm.dao.TJobExecRepository;
import io.elastest.etm.dao.TestCaseRepository;
import io.elastest.etm.dao.TestSuiteRepository;
import io.elastest.etm.model.SupportServiceInstance;
import io.elastest.etm.model.SutExecution;
import io.elastest.etm.model.SutSpecification;
import io.elastest.etm.model.TJobExecution;
import io.elastest.etm.model.TJobExecution.ResultEnum;
import io.elastest.etm.model.TestCase;
import io.elastest.etm.model.TestSuite;
import io.elastest.etm.model.SutSpecification.SutTypeEnum;

@Service
public class TJobExecOrchestratorService {
    private static final Logger logger = LoggerFactory
            .getLogger(TJobExecOrchestratorService.class);

    @Value("${et.edm.elasticsearch.api}")
    private String elasticsearchHost;

    @Value("${elastest.execution.mode}")
    public String ELASTEST_EXECUTION_MODE;

    private final DockerService2 dockerService;
    private final TestSuiteRepository testSuiteRepo;
    private final TestCaseRepository testCaseRepo;

    private final TJobExecRepository tJobExecRepositoryImpl;

    private DatabaseSessionManager dbmanager;
    private final EsmService esmService;
    private SutService sutService;

    public TJobExecOrchestratorService(DockerService2 dockerService,
            TestSuiteRepository testSuiteRepo, TestCaseRepository testCaseRepo,
            TJobExecRepository tJobExecRepositoryImpl,
            DatabaseSessionManager dbmanager, EsmService esmService,
            SutService sutService) {
        super();
        this.dockerService = dockerService;
        this.testSuiteRepo = testSuiteRepo;
        this.testCaseRepo = testCaseRepo;
        this.tJobExecRepositoryImpl = tJobExecRepositoryImpl;
        this.dbmanager = dbmanager;
        this.esmService = esmService;
        this.sutService = sutService;
    }

    @Async
    public void executeTJob(TJobExecution tJobExec, String tJobServices) {
        dbmanager.bindSession();
        tJobExec = tJobExecRepositoryImpl.findOne(tJobExec.getId());

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
                initSut(dockerExec, tJobExec);
            }

            List<ReportTestSuite> testSuites;
            // Start Test
            resultMsg = "Executing Test";
            updateTJobExecResultStatus(tJobExec,
                    TJobExecution.ResultEnum.EXECUTING_TEST, resultMsg);
            testSuites = dockerService.executeTest(dockerExec);

            resultMsg = "Waiting for Test Results";
            updateTJobExecResultStatus(tJobExec,
                    TJobExecution.ResultEnum.WAITING, resultMsg);
            saveTestResults(testSuites, tJobExec);

            // End and purge services
            dockerService.endAllExec(dockerExec);
            if (tJobServices != null && tJobServices != "") {
                deprovideServices(tJobExec);
            }
            resultMsg = "Finished";
            updateTJobExecResultStatus(tJobExec,
                    TJobExecution.ResultEnum.FINISHED, resultMsg);
        } catch (Exception e) {
            e.printStackTrace();
            if (!e.getMessage().equals("end error")) {
                resultMsg = "Failure";
                updateTJobExecResultStatus(tJobExec,
                        TJobExecution.ResultEnum.FAILURE, resultMsg);
            }
        }

        // Setting execution data
        tJobExec.setSutExecution(dockerExec.getSutExec());

        // Saving execution data
        tJobExecRepositoryImpl.save(tJobExec);
        dbmanager.unbindSession();
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

    /**** TSS methods ****/

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
                            resultMsg + service.get("short-name").toString()
                                    .replaceAll("\"", ""));

                    String instanceId = esmService.provisionServiceInstanceSync(
                            service.get("id").toString().replaceAll("\"", ""),
                            tJobExec.getId(), tJobExec.getTjob().getId());

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
        List<String> instancesAux = new ArrayList<String>(
                tJobExec.getServicesInstances());
        for (String instanceId : tJobExec.getServicesInstances()) {
            esmService.deprovisionServiceInstance(instanceId, true);
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

    /**** SuT Methods ****/

    public void initSut(DockerExecution dockerExec, TJobExecution tJobExec) {
//        String resultMsg = "Executing dockerized SuT";
//        updateTJobExecResultStatus(tJobExec,
//                TJobExecution.ResultEnum.EXECUTING_SUT, resultMsg);
//
//        resultMsg = "Waiting for SuT service ready";
//        updateTJobExecResultStatus(tJobExec,
//                TJobExecution.ResultEnum.WAITING_SUT, resultMsg);
        this.startSut(dockerExec);
    }

    public void startSut(DockerExecution dockerExec) {
        SutSpecification sut = dockerExec.gettJobexec().getTjob().getSut();
        SutExecution sutExec;

        String sutIP = "";

        // If it's MANAGED SuT
        if (sut.getSutType() != SutTypeEnum.DEPLOYED) {
            logger.info("Starting sut " + dockerExec.getExecutionId());
            sutExec = sutService.createSutExecutionBySut(sut);
            try {
                // Create container
                dockerService.createSutContainer(dockerExec);
                sutExec.setDeployStatus(SutExecution.DeployStatusEnum.DEPLOYED);

                // Start container
                String sutContainerId = dockerExec.getAppContainerId();
                dockerService.startSutcontainer(dockerExec);

                sutIP = dockerService.getContainerIp(sutContainerId,
                        dockerExec);

                if (sut.getPort() != null) {
                    String sutPort = sut.getPort();
                    logger.info("Start testing if SuT is ready checking port "
                            + sutPort);
                    dockerService.checkSut(dockerExec, sutIP, sutPort);
                }
                // Wait for Sut started

            } catch (Exception e) {
                e.printStackTrace();
                sutExec.setDeployStatus(SutExecution.DeployStatusEnum.ERROR);
                dockerService.endSutExec(dockerExec);
                sutService.modifySutExec(dockerExec.getSutExec());
            }
        } else { // If it's DEPLOYED SuT
            Long currentSutExecId = sut.getCurrentSutExec();
            sutExec = sutService.getSutExecutionById(currentSutExecId);
            sutIP = sut.getSpecification();
        }

        String sutUrl = "http://" + sutIP + ":"
                + (sut.getPort() != null ? sut.getPort() : "");
        sutExec.setUrl(sutUrl);
        sutExec.setIp(sutIP);

        dockerExec.setSutExec(sutExec);
    }

    /**** TJob Exec Methods ****/

    private void updateTJobExecResultStatus(TJobExecution tJobExec,
            ResultEnum result, String msg) {
        tJobExec.setResult(result);
        tJobExec.setResultMsg(msg);
        tJobExecRepositoryImpl.save(tJobExec);
    }

}
