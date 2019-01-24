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
import javax.annotation.PreDestroy;

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
import io.elastest.epm.client.service.EpmService;
import io.elastest.etm.dao.TJobExecRepository;
import io.elastest.etm.dao.TestCaseRepository;
import io.elastest.etm.dao.TestSuiteRepository;
import io.elastest.etm.dao.external.ExternalTJobExecutionRepository;
import io.elastest.etm.model.EusExecutionData;
import io.elastest.etm.model.MultiConfig;
import io.elastest.etm.model.Parameter;
import io.elastest.etm.model.SocatBindedPort;
import io.elastest.etm.model.SupportServiceInstance;
import io.elastest.etm.model.SutExecution;
import io.elastest.etm.model.SutExecution.DeployStatusEnum;
import io.elastest.etm.model.SutSpecification;
import io.elastest.etm.model.SutSpecification.CommandsOptionEnum;
import io.elastest.etm.model.SutSpecification.ManagedDockerType;
import io.elastest.etm.model.SutSpecification.SutTypeEnum;
import io.elastest.etm.model.TJobExecution;
import io.elastest.etm.model.TJobExecution.ResultEnum;
import io.elastest.etm.model.TJobExecution.TypeEnum;
import io.elastest.etm.model.TJobSupportService;
import io.elastest.etm.model.TestCase;
import io.elastest.etm.model.TestSuite;
import io.elastest.etm.model.external.ExternalTJobExecution;
import io.elastest.etm.utils.UtilTools;
import io.elastest.etm.utils.UtilsService;

@Service
public class TJobExecOrchestratorService {
    private static final Logger logger = LoggerFactory
            .getLogger(TJobExecOrchestratorService.class);

    @Value("${exec.mode}")
    public String execMode;
    @Value("${elastest.docker.network}")
    private String elastestDockerNetwork;
    @Value("${et.etm.lstcp.port}")
    private String logstashTcpPort;
    @Value("${et.etm.binded.lstcp.port}")
    public String bindedLsTcpPort;

    public final DockerEtmService dockerEtmService;
    private final DockerComposeService dockerComposeService;
    private final TestSuiteRepository testSuiteRepo;
    private final TestCaseRepository testCaseRepo;

    private final TJobExecRepository tJobExecRepositoryImpl;

    private DatabaseSessionManager dbmanager;
    private final EsmService esmService;
    private SutService sutService;
    private MonitoringServiceInterface monitoringService;

    private EtmContextService etmContextService;
    private EpmService epmService;
    private UtilsService utilsService;

    private final ExternalTJobExecutionRepository externalTJobExecutionRepository;

    Map<String, Boolean> execsAreStopped = new HashMap<String, Boolean>();

    Map<String, Future<Void>> asyncExternalElasticsearchSutExecs = new HashMap<String, Future<Void>>();

    public TJobExecOrchestratorService(DockerEtmService dockerEtmService,
            TestSuiteRepository testSuiteRepo, TestCaseRepository testCaseRepo,
            TJobExecRepository tJobExecRepositoryImpl,
            DatabaseSessionManager dbmanager, EsmService esmService,
            SutService sutService, DockerComposeService dockerComposeService,
            MonitoringServiceInterface monitoringService,
            EtmContextService etmContextService, EpmService epmService,
            UtilsService utilsService,
            ExternalTJobExecutionRepository externalTJobExecutionRepository) {
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
        this.epmService = epmService;
        this.utilsService = utilsService;
        this.externalTJobExecutionRepository = externalTJobExecutionRepository;
    }

    @PostConstruct
    private void init() {
        dbmanager.bindSession();
        manageZombieJobs();
        dbmanager.unbindSession();
    }

    @PreDestroy
    private void destroy() {
        for (HashMap.Entry<String, Future<Void>> asyncMap : asyncExternalElasticsearchSutExecs
                .entrySet()) {
            this.stopManageSutByExternalElasticsearch(asyncMap.getKey());
        }
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
                            ResultEnum.STOPPED, resultMsg);
                }
            }
        }
        logger.info("End Manage zombie Jobs");
    }

    @Async
    public void executeExternalJob(TJobExecution tJobExec,
            boolean integratedJenkins) {
        dbmanager.bindSession();
        tJobExec = tJobExecRepositoryImpl.findById(tJobExec.getId()).get();
        monitoringService
                .createMonitoringIndex(tJobExec.getMonitoringIndicesList());

        String resultMsg = "Initializing";
        tJobExec.setResultMsg(resultMsg);
        tJobExecRepositoryImpl.save(tJobExec);
        DockerExecution dockerExec = new DockerExecution(tJobExec);

        try {
            initTSS(tJobExec, tJobExec.getTjob().getSelectedServices());
            setTJobExecEnvVars(tJobExec, true, false);
            tJobExec = tJobExecRepositoryImpl.save(tJobExec);
            dockerExec.updateFromTJobExec(tJobExec);

            // Create queues and load basic services
            dockerEtmService.loadBasicServices(dockerExec);
            resultMsg = "Starting Dockbeat to get metrics...";
            dockerEtmService.updateExecutionResultStatus(dockerExec,
                    ResultEnum.IN_PROGRESS, resultMsg);

            // Start SuT if it's necessary
            if (dockerExec.isWithSut()) {
                // Start Dockbeat
                dockerEtmService.startDockbeat(dockerExec);
                initSut(dockerExec, !integratedJenkins);
                tJobExec.setSutExecution(dockerExec.getSutExec());
            }

            setTJobExecEnvVars(tJobExec, true, false);

            // Start Test
            resultMsg = "Executing Test";
            dockerEtmService.updateTJobExecResultStatus(tJobExec,
                    ResultEnum.EXECUTING_TEST, resultMsg);
        } catch (Exception e) {
            logger.error("Error starting a TSS or a SUT", e);
            resultMsg = "Error starting services on ElasTest";
            dockerEtmService.updateTJobExecResultStatus(tJobExec,
                    ResultEnum.ERROR, resultMsg);
        }
        dbmanager.unbindSession();
    }

    @Async
    public Future<Void> executeTJob(TJobExecution tJobExec,
            String tJobServices) {
        dbmanager.bindSession();
        tJobExec.setResult(ResultEnum.IN_PROGRESS);
        String resultMsg = "Initializing";
        Date startDate = new Date();
        tJobExec = tJobExecRepositoryImpl.findById(tJobExec.getId()).get();

        String tJobExecMapName = getMapNameByTJobExec(tJobExec);
        execsAreStopped.put(tJobExecMapName, false);

        monitoringService
                .createMonitoringIndex(tJobExec.getMonitoringIndicesList());
        tJobExec = tJobExecRepositoryImpl.save(tJobExec);

        if (tJobExec.isMultiExecutionParent()) {
            tJobExec = initMultiTJobExecution(tJobExec);
        } else if (tJobExec.isMultiExecutionChild()) {
            tJobExec.setStartDate(startDate);
        } else {
            tJobExec.setResultMsg(resultMsg);
            tJobExec.setResult(ResultEnum.IN_PROGRESS);
            tJobExec = tJobExecRepositoryImpl.save(tJobExec);
        }

        DockerExecution dockerExec = new DockerExecution(tJobExec);

        if (tJobExec.isMultiExecutionParent()) { // Parent execution
            resultMsg = "Executing Test";
            dockerEtmService.updateExecutionResultStatus(dockerExec,
                    TJobExecution.ResultEnum.EXECUTING_TEST, resultMsg);

            for (TJobExecution childExec : tJobExec.getExecChilds()) {
                // If parent not stopped, execute
                if (execsAreStopped.containsKey(tJobExecMapName)
                        && !execsAreStopped.get(tJobExecMapName)) {
                    this.executeTJob(childExec, tJobServices);
                }
            }

            tJobExec.setEndDate(new Date());

            saveMultiParentFinishStatus(tJobExec, dockerExec);

        } else { // Simple/child execution

            try {
                initTSS(tJobExec, tJobServices);
                setTJobExecEnvVars(tJobExec, false, false);
                tJobExec = tJobExecRepositoryImpl.save(tJobExec);
                dockerExec.updateFromTJobExec(tJobExec);

                // Create queues and load basic services
                dockerEtmService.loadBasicServices(dockerExec);

                resultMsg = "Starting Dockbeat to get metrics...";
                dockerEtmService.updateExecutionResultStatus(dockerExec,
                        ResultEnum.IN_PROGRESS, resultMsg);

                // Start Dockbeat
                dockerEtmService.startDockbeat(dockerExec);

                // Start SuT if it's necessary
                if (dockerExec.isWithSut()) {
                    initSut(dockerExec);
                }

                // Start Test
                resultMsg = "Preparing Test";
                dockerEtmService.updateExecutionResultStatus(dockerExec,
                        ResultEnum.EXECUTING_TEST, resultMsg);
                List<ReportTestSuite> testSuites = dockerEtmService
                        .createAndStartTestContainer(dockerExec);

                // Test Results
                resultMsg = "Waiting for Test Results";
                dockerEtmService.updateExecutionResultStatus(dockerExec,
                        ResultEnum.WAITING, resultMsg);
                saveTestResults(testSuites, tJobExec);

                tJobExec.setEndDate(new Date());

                logger.info("Ending Execution {}...", tJobExec.getId());
                // End and purge services
                endAllExecs(dockerExec);
                saveFinishStatus(tJobExec, dockerExec);
            } catch (TJobStoppedException e) {
                // Stop exception
                logger.warn("TJob Exec {} stopped!", tJobExec.getId());
            } catch (Exception e) {
                logger.error("Error during TJob Execution {}", tJobExec.getId(),
                        e);
                if (!"end error".equals(e.getMessage())) {
                    resultMsg = "Internal Error: " + e.getMessage();
                    dockerEtmService.updateExecutionResultStatus(dockerExec,
                            ResultEnum.ERROR, resultMsg);

                    tJobExec.setEndDate(new Date());
                    try {
                        endAllExecs(dockerExec);
                    } catch (Exception e1) {
                        logger.error("Error on end TJob Exec {}",
                                tJobExec.getId(), e1);
                    }
                } else {
                    saveFinishStatus(tJobExec, dockerExec);
                }
            } finally {
                if (tJobServices != null && tJobServices != "") {
                    try {
                        deprovisionServices(tJobExec);
                    } catch (Exception e) {
                        logger.error(
                                "TJob Exec {} => Exception on deprovision TSS: {}",
                                tJobExec.getId(), e.getMessage());
                        // TODO Customize Exception
                    }
                }
            }

            // Setting execution data
            tJobExec.setSutExecution(dockerExec.getSutExec());
        }
        // Saving execution data
        tJobExecRepositoryImpl.save(tJobExec);

        if (!tJobExec.isMultiExecutionChild()) {
            execsAreStopped.remove(tJobExecMapName);
        }

        dbmanager.unbindSession();
        return new AsyncResult<Void>(null);
    }

    private TJobExecution initMultiTJobExecution(TJobExecution tJobExec) {
        try {
            List<String> namesList = new ArrayList<String>();
            List<List<String>> valuesList = new ArrayList<List<String>>();

            int numOfChilds = 0;
            // TODO refactor this method and createChildTJobExecs(recursive
            // method)
            for (MultiConfig currentMultiConfig : tJobExec
                    .getMultiConfigurations()) {
                namesList.add(currentMultiConfig.getName());
                valuesList.add(currentMultiConfig.getValues());

                if (numOfChilds == 0) {
                    numOfChilds = 1;
                }

                numOfChilds *= currentMultiConfig.getValues().size();
            }
            if (numOfChilds > 0) {
                tJobExec = createChildTJobExecs(tJobExec,
                        tJobExec.getParameters(), namesList, valuesList);
            }

            tJobExec = tJobExecRepositoryImpl.save(tJobExec);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return tJobExec;
    }

    public TJobExecution createChildTJobExecs(TJobExecution parentTJobExec,
            List<Parameter> parametersList, List<String> namesList,
            List<List<String>> valuesList) {
        if (namesList.size() == 0 || valuesList.size() == 0) {
            return parentTJobExec;
        }

        if (parametersList == null) {
            parametersList = new ArrayList<>();
        }

        // Extract first name and values
        String currentName = namesList.get(0);
        if (namesList.size() > 1) {
            namesList = new ArrayList<>(namesList.subList(1, namesList.size()));
        } else {
            namesList = new ArrayList<>();
        }

        List<String> currentValueList = valuesList.get(0);
        if (valuesList.size() > 1) {
            valuesList = new ArrayList<>(
                    valuesList.subList(1, valuesList.size()));
        } else {
            valuesList = new ArrayList<>();
        }

        for (String currentValue : currentValueList) {
            List<Parameter> receivedParametersList = new ArrayList<>(
                    parametersList);

            Parameter currentParam = new Parameter(currentName, currentValue,
                    true);
            receivedParametersList.add(currentParam);
            if (namesList.size() > 0 && currentValueList.size() > 0) {
                parentTJobExec = createChildTJobExecs(parentTJobExec,
                        receivedParametersList, namesList, valuesList);
            }
            // Last
            else {
                TJobExecution childTJobExec = new TJobExecution();
                childTJobExec.setType(TypeEnum.CHILD);
                childTJobExec.setMonitoringStorageType(
                        parentTJobExec.getMonitoringStorageType());

                childTJobExec.setTjob(parentTJobExec.getTjob());
                childTJobExec.setExecParent(parentTJobExec);
                childTJobExec.setParameters(receivedParametersList);

                childTJobExec.setResult(ResultEnum.WAITING);
                childTJobExec.setResultMsg("Waiting to start");

                childTJobExec = tJobExecRepositoryImpl.save(childTJobExec);
                childTJobExec.generateMonitoringIndex();

                childTJobExec = tJobExecRepositoryImpl.save(childTJobExec);

                if (parentTJobExec.getExecChilds() == null) {
                    parentTJobExec.setExecChilds(new ArrayList<>());
                }
                parentTJobExec.getExecChilds().add(childTJobExec);

                parentTJobExec = tJobExecRepositoryImpl.save(parentTJobExec);
            }
        }

        return parentTJobExec;
    }

    public TJobExecution forceEndExecution(TJobExecution tJobExec)
            throws Exception {
        tJobExec = tJobExecRepositoryImpl.findById(tJobExec.getId()).get();

        if (!tJobExec.isMultiExecutionChild()) {
            execsAreStopped.put(getMapNameByTJobExec(tJobExec), true);
        }

        if (tJobExec.isMultiExecutionParent()) {
            for (TJobExecution childExec : tJobExec.getExecChilds()) {
                forceEndExecution(childExec);
                String resultMsg = "Stopped";
                dockerEtmService.updateTJobExecResultStatus(childExec,
                        ResultEnum.STOPPED, resultMsg);
            }
        }

        DockerExecution dockerExec = new DockerExecution(tJobExec);
        dockerEtmService.configureDocker(dockerExec);
        try {
            endAllExecs(dockerExec, true);
        } catch (TJobStoppedException e) {
            // Stop exception
        } catch (Exception e) {
            logger.error("Exception during force end execution", e);
        }

        // Deprovision all TSS associated
        logger.debug("Requesting the TSS deprovision.");
        try {
            deprovisionServices(tJobExec);
        } catch (Exception e) {
            logger.error("Exception during the deprovision of services");
        }

        if (tJobExec.getTjob().isExternal()) {
            String resultMsg = "Success";
            dockerEtmService.updateExecutionResultStatus(dockerExec,
                    ResultEnum.SUCCESS, resultMsg);
        } else {
            String resultMsg = "Stopped";
            dockerEtmService.updateExecutionResultStatus(dockerExec,
                    ResultEnum.STOPPED, resultMsg);
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
        dockerEtmService.updateExecutionResultStatus(dockerExec, finishStatus,
                resultMsg);
    }

    public void saveMultiParentFinishStatus(TJobExecution tJobExec,
            DockerExecution dockerExec) {
        ResultEnum finishStatus = ResultEnum.SUCCESS;

        for (TJobExecution childExec : tJobExec.getExecChilds()) {
            if (childExec.getResult() == ResultEnum.FAIL
                    || childExec.getResult() == ResultEnum.ERROR
                    || childExec.getResult() == ResultEnum.STOPPED) { // Else
                finishStatus = childExec.getResult();
                break;
            }
        }
        String tJobExecMapName = getMapNameByTJobExec(tJobExec);

        if (execsAreStopped.containsKey(tJobExecMapName)
                && execsAreStopped.get(tJobExecMapName)) {
            finishStatus = ResultEnum.STOPPED;
        }

        String resultMsg = "Finished: " + finishStatus;
        dockerEtmService.updateExecutionResultStatus(dockerExec, finishStatus,
                resultMsg);
    }

    public void endAllExecs(DockerExecution dockerExec, boolean force)
            throws Exception {
        try {
            if (dockerExec.isExternal()) {

            } else {
                endTestExec(dockerExec, force);
            }
            if (dockerExec.isWithSut()) {
                endSutExec(dockerExec, force);
                stopManageSutByExternalElasticsearch(dockerExec.getTJobExec());
            }
            endDockbeatExec(dockerExec, force);
        } catch (Exception e) {
            logger.error("Error on end all execs");
            throw new Exception("end error", e); // TODO Customize Exception
        }
    }

    public void endAllExecs(DockerExecution dockerExec) throws Exception {
        endAllExecs(dockerExec, false);
    }
    /* ******************* */
    /* *** TSS methods *** */
    /* ******************* */

    private void initTSS(TJobExecution tJobExec, String tJobServices)
            throws Exception {
        try {
            if (tJobServices != null && tJobServices != "") {
                provideServices(tJobServices, tJobExec);

                // Wait only if not is mini. (In mini is already waiting for
                // them individually in provideService)
                if (!utilsService.isElastestMini()) {
                    esmService.waitForTJobExecServicesAreReady(tJobExec);
                }
            } else {
                logger.info("There aren't TSSs to be provided");
            }
        } catch (TJobStoppedException e) {
            throw e;
        } catch (Exception e) {
            throw new Exception("Exception on init Test Support Services", e);
        }
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
                            ResultEnum.STARTING_TSS,
                            resultMsg + service.getName());

                    this.provideService(service, tJobExec);
                }
            }
        } catch (IOException e) {
            throw new Exception("Error on provide TSS", e);
        }
    }

    private String provideService(TJobSupportService service,
            TJobExecution tJobExec) {
        String instanceId = "";

        // If normal/experimental-lite mode, provision async and show pulling
        // information
        String tssId = service.getId();
        if (utilsService.isElastestMini()) {
            instanceId = esmService.generateNewOrGetInstanceId(tssId);
            if (esmService.isSharedTssInstanceByServiceId(tssId)) {
                // If is shared, is started
                esmService.provisionTJobExecSharedTSSSync(tssId, tJobExec,
                        instanceId);
            } else {
                // Else provision async and wait after for tss
                esmService.provisionTJobExecServiceInstanceAsync(tssId,
                        tJobExec, instanceId);
            }

            String serviceName = esmService.getServiceNameByServiceId(tssId)
                    .toUpperCase();

            esmService.waitForTssStartedInMini(tJobExec, instanceId,
                    serviceName);
        } else { // Sync provision
            instanceId = esmService.provisionTJobExecServiceInstanceSync(tssId,
                    tJobExec);
        }

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
        SupportServiceInstance ssi = esmService
                .getTJobServiceInstancesById(tSSInstanceId);
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
            boolean externalTJob, boolean withPublicPrefix) throws Exception {
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

        // In mini mode, tjobs make use of started EUS
        String etEusApiKey = "ET_EUS_API";
        if (utilsService.isElastestMini() && envVars.containsKey(etEusApiKey)) {
            String eusApi = envVars.get(etEusApiKey);
            if (eusApi != null) {
                logger.info("This is the EUS's API URL: {}", eusApi);

                // If is Jenkins, config EUS to start browsers at sut network
                boolean useSutNetwork = tJobExec.getTjob().isExternal();
                String sutContainerPrefix = dockerEtmService
                        .getSutPrefixBySuffix(tJobExec.getId().toString());

                EusExecutionData eusExecutionDate = new EusExecutionData(
                        tJobExec, "", useSutNetwork, sutContainerPrefix);
                eusApi = eusApi.endsWith("/") ? eusApi : eusApi + "/";
                eusApi += "execution/" + eusExecutionDate.getKey() + "/";
                envVars.put(etEusApiKey, eusApi);
            }
        }

        // Add env var for the SUT host if a SUT is required
        logger.debug(
                "Below the SUT host ip will displayed if there is SUT execution");
        SutExecution sutExec = tJobExec.getSutExecution();
        if (sutExec != null) {
            SutSpecification sut = tJobExec.getSutExecution()
                    .getSutSpecification();
            Long sutPublicPortLong = sutExec.getPublicPort();
            String sutPublicPort = sutPublicPortLong != null
                    ? sutPublicPortLong.toString()
                    : null;

            // Sut HOST
            String sutHostKey = "ET_SUT_HOST";
            envVars.put(sutHostKey,
                    (sutPublicPort != null
                            && !utilsService.isDefaultEtPublicHost())
                                    ? utilsService.getEtPublicHostValue()
                                    : sutExec.getIp());
            logger.debug("{}: {}", sutHostKey, envVars.get(sutHostKey));

            // Sut PORT
            String sutPort = sut.getPort();
            if (sutPort != null && !sutPort.isEmpty()) {
                String sutPortKey = "ET_SUT_PORT";
                envVars.put(sutPortKey,
                        sutPublicPort != null
                                && !utilsService.isDefaultEtPublicHost()
                                        ? sutPublicPort
                                        : sut.getPort());
                logger.debug("{}: {}", sutPortKey, envVars.get(sutPortKey));
            }

            // Sut PROTOCOL
            String sutProtocol = sut.getProtocol().toString();
            if (sutProtocol != null && !sutProtocol.isEmpty()) {
                String sutProtocolKey = "ET_SUT_PROTOCOL";
                envVars.put(sutProtocolKey, sutProtocol);
                logger.debug("{}: {}", sutProtocolKey, sutProtocol);
            }
        }

        envVars.put("ET_NETWORK", elastestDockerNetwork);

        // Setting SUT name for external Job
        envVars.put("ET_SUT_CONTAINER_NAME", dockerEtmService
                .getSutPrefixBySuffix(tJobExec.getId().toString()));

        tJobExec.setEnvVars(envVars);
    }

    public void deprovisionServices(TJobExecution tJobExec) {
        Long execId = tJobExec.getId();
        logger.info("TJob Exec {} => Start the services deprovision.", execId);
        List<String> instancesAux = new ArrayList<String>();
        if (tJobExec.getServicesInstances().size() > 0) {
            logger.debug(
                    "TJob Exec {} => Deprovisioning TJob's TSSs stored in the TJob object",
                    execId);
            instancesAux = tJobExec.getServicesInstances();
        } else if (esmService.gettSSIByTJobExecAssociated()
                .get(execId) != null) {
            logger.debug(
                    "TJob Exec {} => Deprovisioning TJob's TSSs stored in the EsmService",
                    execId);
            instancesAux = esmService.gettSSIByTJobExecAssociated().get(execId);
        }

        logger.debug("TJob Exec {} => TSS list size: {}", execId, instancesAux);
        for (String instanceId : instancesAux) {
            esmService.deprovisionTJobExecServiceInstance(instanceId, execId);
            logger.debug("TJob Exec {} => TSS Instance id to deprovision: {}",
                    execId, instanceId);
        }
    }

    /* ********************* */
    /* *** External TJob *** */
    /* ********************* */

    @Async
    public Future<Void> executeExternalTJob(ExternalTJobExecution exec) {
        dbmanager.bindSession();
        exec = externalTJobExecutionRepository.findById(exec.getId()).get();

        DockerExecution dockerExec = new DockerExecution(exec);
        try {
            dockerEtmService.loadBasicServices(dockerExec);

            dockerEtmService.updateExecutionResultStatus(dockerExec,
                    ResultEnum.IN_PROGRESS, "Initializing execution...");

            if (dockerExec.isWithSut()) {
                dockerEtmService.startDockbeat(dockerExec);
                this.initSut(dockerExec);

            }
            String resultMsg = "Executing Test";
            dockerEtmService.updateExecutionResultStatus(dockerExec,
                    ResultEnum.EXECUTING_TEST, resultMsg);
        } catch (Exception e) {
            e.printStackTrace();
        }

        dbmanager.unbindSession();
        return new AsyncResult<Void>(null);
    }

    @Async
    public Future<Void> endExternalTJob(ExternalTJobExecution exec) {
        dbmanager.bindSession();
        ExternalTJobExecution externalTJobExec = externalTJobExecutionRepository
                .findById(exec.getId()).get();

        // If withSut, end containers
        if (externalTJobExec.isWithSut() && ResultEnum
                .isFinishedOrNotExecuted(externalTJobExec.getResult())) {

            DockerExecution dockerExec = new DockerExecution(externalTJobExec);
            try {
                dockerEtmService.loadBasicServices(dockerExec);

                endDockbeatExec(dockerExec, false);
                endSutExec(dockerExec, false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        dbmanager.unbindSession();
        return new AsyncResult<Void>(null);
    }

    /* ******************* */
    /* *** SuT Methods *** */
    /* ******************* */

    public void initSut(DockerExecution dockerExec) throws Exception {
        initSut(dockerExec, false);
    }

    public void initSut(DockerExecution dockerExec, boolean publicSut)
            throws Exception {
        try {
            SutSpecification sut = dockerExec.getSut();
            SutExecution sutExec;

            // If it's SuT DEPLOYED outside ElasTest
            if (sut.getSutType() == SutTypeEnum.DEPLOYED) {
                logger.info("Using SUT deployed outside ElasTest");
                TJobExecution tJobExec = dockerExec.getTJobExec();
                sutExec = startSutDeployedOutside(tJobExec);

            }
            // If it's MANAGED SuT
            else {
                logger.info("Using SUT deployed by ElasTest");
                try {
                    sutExec = startManagedSut(dockerExec);
                    if (publicSut) {
                        SocatBindedPort socatBindedPortObj = dockerEtmService
                                .bindingPort(sutExec.getIp(),
                                        "sut_" + sutExec.getId(),
                                        dockerExec.getSut().getPort(),
                                        elastestDockerNetwork,
                                        epmService.etMasterSlaveMode);
                        sutExec.setPublicPort(Long
                                .parseLong(socatBindedPortObj.getBindedPort()));
                        sutService.modifySutExec(sutExec);
                    }
                } catch (TJobStoppedException e) {
                    throw e;
                }
            }

            dockerExec.setSutExec(sutExec);

            if (dockerExec.isExternal()) {
                dockerExec.getExternalTJobExec().getEnvVars().put("ET_SUT_URL",
                        sutExec.getUrl());
            }
        } catch (TJobStoppedException e) {
            throw e;
        } catch (Exception e) {
            new Exception("Exception on init/start Sut", e);
        }
    }

    public SutExecution startSutDeployedOutside(TJobExecution tJobExec)
            throws Exception {
        SutSpecification sut = tJobExec.getTjob().getSut();
        SutExecution sutExec;
        String sutIP = "";

        try {
            String resultMsg = "Preparing External SuT";
            dockerEtmService.updateTJobExecResultStatus(tJobExec,
                    ResultEnum.WAITING_SUT, resultMsg);

            // Sut instrumented by EIM
            if (sut.isInstrumentedByElastest() && sut.isInstrumentalized()) {
                dockerEtmService.updateTJobExecResultStatus(tJobExec,
                        ResultEnum.WAITING_SUT, "Deploying beats");
                logger.debug("TJob Exec {} => Deploy Beats", tJobExec.getId());
                sut = sutService.deployEimSutBeats(sut, false);
            }

            Long currentSutExecId = sut.getCurrentSutExec();
            sutExec = sutService.getSutExecutionById(currentSutExecId);
            sutIP = sut.getSpecification();
            String sutUrl = sut.getSutUrlByGivenIp(sutIP);
            sutExec.setUrl(sutUrl);
            sutExec.setIp(sutIP);

            // Sut logs from External Elasticsearch
            if (sut.isUsingExternalElasticsearch()) {
                Future<Void> asyncExec = sutService
                        .manageSutExecutionUsingExternalElasticsearch(sut,
                                sutExec.getSutExecMonitoringIndex());

                asyncExternalElasticsearchSutExecs
                        .put(getMapNameByTJobExec(tJobExec), asyncExec);
            }

            return sutExec;
        } catch (Exception e) {
            throw new Exception("Error preparing the Sut to use in a test: "
                    + e.getMessage() + ".");
        }
    }

    private SutExecution startManagedSut(DockerExecution dockerExec)
            throws Exception {
        SutSpecification sut = dockerExec.getSut();

        String resultMsg = "Preparing dockerized SuT";
        dockerEtmService.updateExecutionResultStatus(dockerExec,
                ResultEnum.EXECUTING_SUT, resultMsg);
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

            String sutIp;
            if (EpmService.etMasterSlaveMode) {
                logger.info("Sut main service name: {}",
                        ("/" + dockerEtmService.getSutName(dockerExec)
                                .replaceAll("_", "") + "_"
                                + sut.getMainService() + "_1"));
                sutIp = epmService.getRemoteServiceIpByVduName(
                        "/" + dockerEtmService.getSutName(dockerExec) + "_"
                                + sut.getMainService() + "_1");
                logger.info("Sut main service ip: {}", sutIp);
            } else {
                String sutContainerId = dockerExec.getAppContainerId();
                sutIp = dockerEtmService.getContainerIpWithDockerExecution(
                        sutContainerId, dockerExec);
            }

            // If port is defined, wait for SuT ready
            if (sut.getPort() != null && !"".equals(sut.getPort())) {
                String sutPort = sut.getPort();
                resultMsg = "Waiting for dockerized SuT";
                logger.info(resultMsg);
                dockerEtmService.updateExecutionResultStatus(dockerExec,
                        ResultEnum.WAITING_SUT, resultMsg);

                // If is Sut In new Container
                if (sut.isSutInNewContainer()) {
                    sutIp = this.waitForSutInContainer(dockerExec, 480000); // 8min
                }

                // Wait for SuT started
                resultMsg = "Waiting for SuT service ready at ip " + sutIp
                        + " and port " + sutPort;
                logger.debug(resultMsg);
                dockerEtmService.checkSut(dockerExec, sutIp, sutPort);
                endCheckSutExec(dockerExec);
            }

            // Save SuT Url and Ip into sutexec
            String sutUrl = sut.getSutUrlByGivenIp(sutIp);
            sutExec.setUrl(sutUrl);
            sutExec.setIp(sutIp);

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
        SutSpecification sut = dockerExec.getSut();
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
        SutSpecification sut = dockerExec.getSut();
        String mainService = sut.getMainService();
        logger.debug("The main service saved in DB is: {}", mainService);
        String composeProjectName = dockerEtmService.getSutName(dockerExec);

        // TMP replace sut exec and logstash sut tcp
        String dockerComposeYml = sut.getSpecification();

        // Set logging, network, labels and do pull of images
        dockerComposeYml = prepareElasTestConfigInDockerComposeYml(
                dockerComposeYml, composeProjectName, dockerExec, mainService);

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
        dockerEtmService.updateExecutionResultStatus(dockerExec,
                ResultEnum.EXECUTING_SUT, resultMsg);
        logger.info(resultMsg + " " + dockerExec.getExecutionId());

        // Create Containers
        String pathToSaveTmpYml = "";
        if (dockerExec.isExternal()) {
            pathToSaveTmpYml = esmService.getExternalTJobExecFolderPath(
                    dockerExec.getExternalTJobExec());
        } else {
            pathToSaveTmpYml = esmService
                    .getTJobExecFolderPath(dockerExec.getTJobExec());
        }
        boolean created = dockerComposeService.createProject(project,
                pathToSaveTmpYml, false, false, false);

        // Start Containers
        if (!created) {
            throw new Exception(
                    "Sut docker compose containers are not created");
        }

        dockerComposeService.startProject(composeProjectName, false);

        if (!EpmService.etMasterSlaveMode) {
            for (DockerContainer container : dockerComposeService
                    .getContainers(composeProjectName).getContainers()) {
                String containerId = dockerEtmService.dockerService
                        .getContainerIdByName(container.getName());

                // Insert container into containers list
                dockerEtmService.insertCreatedContainer(containerId,
                        container.getName());
                // If is main service container, set app id
                if (container.getName().equals(
                        composeProjectName + "_" + mainService + "_1")) {
                    dockerExec.setAppContainerId(containerId);
                }

                if (dockerExec.getAppContainerId() == null
                        || dockerExec.getAppContainerId().isEmpty()) {
                    throw new Exception(
                            "Main Sut service from docker compose not started");
                }
            }
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public String prepareElasTestConfigInDockerComposeYml(
            String dockerComposeYml, String composeProjectName,
            DockerExecution dockerExec, String mainService) throws Exception {
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
                if (!etmContextService.etMasterSlaveMode) {
                    this.pullDockerComposeYmlService(service, dockerExec);
                }

                // Set Logging
                service = this.setLoggingToDockerComposeYmlService(service,
                        composeProjectName, dockerExec);

                // Set Elastest Network
                service = this.setNetworkToDockerComposeYmlService(service,
                        composeProjectName, dockerExec);

                // Set Elastest Labels
                service = this.setETLabelsToDockerComposeYmlService(service,
                        composeProjectName, dockerExec);

                // Binding port of the main service if ElasTest is running in
                // Master/Slave mode
                if (EpmService.etMasterSlaveMode
                        && service.getKey().equals(mainService)) {
                    service = setBindingPortYmlService(service,
                            composeProjectName);
                }
            }

            dockerComposeMap = this.setNetworkToDockerComposeYmlRoot(
                    dockerComposeMap, composeProjectName, dockerExec);

            StringWriter writer = new StringWriter();

            yf.createGenerator(writer).writeObject(object);
            dockerComposeYml = writer.toString();

            writer.close();
        } catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
            throw new Exception("Error modifying the docker-compose file");
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
        String port = EpmService.etMasterSlaveMode ? bindedLsTcpPort
                : logstashTcpPort;

        if (dockerEtmService.isEMSSelected(dockerExec)) {
            host = dockerExec.getTJobExec().getEnvVars()
                    .get("ET_EMS_TCP_SUTLOGS_HOST");
            port = dockerExec.getTJobExec().getEnvVars()
                    .get("ET_EMS_TCP_SUTLOGS_PORT");
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
        loggingOptionsContent.put("syslog-format", "rfc5424micro");

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

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public HashMap.Entry<String, HashMap> setETLabelsToDockerComposeYmlService(
            HashMap.Entry<String, HashMap> service, String composeProjectName,
            DockerExecution dockerExec) {

        HashMap serviceContent = service.getValue();
        String labelsKey = "labels";
        // If service has networks, remove it
        if (serviceContent.containsKey(labelsKey)) {
            serviceContent.remove(labelsKey);
        }

        Map<String, String> labelsMap = dockerEtmService.getEtLabels(dockerExec,
                "sut", service.getKey());

        serviceContent.put(labelsKey,
                dockerComposeService.mapAsList(labelsMap));

        return service;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public HashMap.Entry<String, HashMap> setBindingPortYmlService(
            HashMap.Entry<String, HashMap> service, String composeProjectName)
            throws TJobStoppedException {
        logger.info("Binding the port of the SUT");
        HashMap serviceContent = service.getValue();
        String portsKey = "ports";
        String exposeKey = "expose";

        List<String> bindingPorts = new ArrayList<>();
        String servicePort = ((List<Integer>) serviceContent.get(exposeKey))
                .get(0).toString();
        logger.info("Service port: {}", servicePort);
        bindingPorts.add(servicePort + ":" + servicePort);
        serviceContent.put(portsKey, bindingPorts);

        return service;
    }

    public void endSutExec(DockerExecution dockerExec, boolean force)
            throws Exception {
        SutSpecification sut = dockerExec.getSut();
        dockerEtmService.removeSutVolumeFolder(dockerExec);
        // If it's Managed Sut, and container is created
        if (sut.getSutType() != SutTypeEnum.DEPLOYED) {
            updateSutExecDeployStatus(dockerExec, DeployStatusEnum.UNDEPLOYING);
            try {
                if (sut.getManagedDockerType() != ManagedDockerType.COMPOSE) {
                    if (sut.isSutInNewContainer()) {
                        endSutInContainer(dockerExec);
                    }
                    String sutContainerName = dockerEtmService
                            .getSutName(dockerExec);
                    if (force) {
                        dockerEtmService.endContainer(sutContainerName, 1);
                    } else {
                        dockerEtmService.endContainer(sutContainerName);
                    }
                } else {
                    endComposedSutExec(dockerExec);
                }
                updateSutExecDeployStatus(dockerExec,
                        DeployStatusEnum.UNDEPLOYED);
            } finally {
                if (dockerExec.getSutExec().getPublicPort() != null) {
                    logger.debug("Removing sut socat container: {}",
                            "socat_sut_" + dockerExec.getSutExec().getId());
                    dockerEtmService.endContainer(
                            "socat_sut_" + dockerExec.getSutExec().getId());
                }
            }
        } else {
            logger.info("SuT not ended by ElasTest -> Deployed SuT");
            // Sut instrumented by EIM
            if (sut.isInstrumentedByElastest() && sut.isInstrumentalized()) {
                logger.debug("TJob Exec {} => Undeploying Beats",
                        dockerExec.getTJobExec().getId());
                sut = sutService.undeployEimSutBeats(sut, false);
            }
        }
        endCheckSutExec(dockerExec);
    }

    public void endComposedSutExec(DockerExecution dockerExec)
            throws Exception {
        String composeProjectName = dockerEtmService.getSutName(dockerExec);
        dockerComposeService.stopAndRemoveProject(composeProjectName);
    }

    public void endSutInContainer(DockerExecution dockerExec) throws Exception {
        SutSpecification sut = dockerExec.getSut();
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

    public void endDockbeatExec(DockerExecution dockerExec, boolean force)
            throws Exception {
        String containerName = dockerEtmService
                .getDockbeatContainerName(dockerExec);
        if (force) {
            dockerEtmService.endContainer(containerName, 1);
        } else {
            dockerEtmService.endContainer(containerName);
        }
    }

    /* ************************* */
    /* *** TJob Exec Methods *** */
    /* ************************* */

    public void endTestExec(DockerExecution dockerExec, boolean force)
            throws Exception {
        String testContainerName = dockerEtmService.getTestName(dockerExec);
        if (force) {
            dockerEtmService.endContainer(testContainerName, 1);
        } else {
            dockerEtmService.endContainer(testContainerName);
        }
    }

    public void saveTestResults(List<ReportTestSuite> testSuites,
            TJobExecution tJobExec) {
        logger.info("Saving TJob Execution {} results", tJobExec.getId());

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

    public String getMapNameByTJobExec(TJobExecution tJobExec) {
        return tJobExec.getTjob().getId() + "_" + tJobExec.getId();
    }

    public void stopManageSutByExternalElasticsearch(TJobExecution tJobExec) {
        this.stopManageSutByExternalElasticsearch(
                getMapNameByTJobExec(tJobExec));
    }

    public void stopManageSutByExternalElasticsearch(String mapKey) {
        if (!asyncExternalElasticsearchSutExecs.containsKey(mapKey)) {
            return;
        }
        Future<Void> asyncExec = asyncExternalElasticsearchSutExecs.get(mapKey);

        try {
            asyncExec.cancel(true);
            asyncExternalElasticsearchSutExecs.remove(mapKey);
        } catch (Exception e) {
            logger.info(
                    "Error during stop Manage Sut by external Elasticsearch",
                    e);
        }

    }
}