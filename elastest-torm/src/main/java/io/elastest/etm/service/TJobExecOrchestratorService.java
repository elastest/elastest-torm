package io.elastest.etm.service;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.Future;

import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import io.elastest.etm.dao.TJobExecRepository;
import io.elastest.etm.dao.external.ExternalTJobExecutionRepository;
import io.elastest.etm.model.EusExecutionData;
import io.elastest.etm.model.Execution;
import io.elastest.etm.model.MultiConfig;
import io.elastest.etm.model.Parameter;
import io.elastest.etm.model.ServiceBindedPort;
import io.elastest.etm.model.SharedAsyncModel;
import io.elastest.etm.model.SupportServiceInstance;
import io.elastest.etm.model.SutExecution;
import io.elastest.etm.model.SutSpecification;
import io.elastest.etm.model.SutSpecification.SutTypeEnum;
import io.elastest.etm.model.TJobExecution;
import io.elastest.etm.model.TJobExecution.ResultEnum;
import io.elastest.etm.model.TJobExecution.TypeEnum;
import io.elastest.etm.model.TJobSupportService;
import io.elastest.etm.model.TestSuite;
import io.elastest.etm.model.external.ExternalTJobExecution;
import io.elastest.etm.platform.service.PlatformService;
import io.elastest.etm.service.exception.TJobStoppedException;
import io.elastest.etm.utils.UtilTools;
import io.elastest.etm.utils.UtilsService;

@Service
public class TJobExecOrchestratorService {
    private static final String EXEC_END_DATE_KEY = "execEndDate";

    private static final Logger logger = getLogger(lookup().lookupClass());

    @Value("${exec.mode}")
    public String execMode;
    @Value("${elastest.docker.network}")
    private String elastestDockerNetwork;
    @Value("${et.etm.lstcp.port}")
    private String logstashTcpPort;
    @Value("${et.etm.binded.lstcp.port}")
    public String bindedLsTcpPort;

    private final TJobExecRepository tJobExecRepositoryImpl;
    private DatabaseSessionManager dbmanager;
    private final EsmService esmService;
    private SutService sutService;
    private AbstractMonitoringService monitoringService;

    private EtmContextService etmContextService;
    private UtilsService utilsService;
    private PlatformService platformService;
    private EtmTestResultService etmTestResultService;
    private EimService eimService;

    private final ExternalTJobExecutionRepository externalTJobExecutionRepository;

    Map<String, Boolean> execsAreStopped = new HashMap<String, Boolean>();

    Map<String, SharedAsyncModel<Void>> asyncExternalElasticsearchSutExecs = new HashMap<String, SharedAsyncModel<Void>>();

    public TJobExecOrchestratorService(
            TJobExecRepository tJobExecRepositoryImpl,
            DatabaseSessionManager dbmanager, EsmService esmService,
            SutService sutService, AbstractMonitoringService monitoringService,
            EtmContextService etmContextService, UtilsService utilsService,
            ExternalTJobExecutionRepository externalTJobExecutionRepository,
            PlatformService platformService,
            EtmTestResultService etmTestResultService, EimService eimService) {
        super();
        this.tJobExecRepositoryImpl = tJobExecRepositoryImpl;
        this.dbmanager = dbmanager;
        this.esmService = esmService;
        this.sutService = sutService;
        this.monitoringService = monitoringService;
        this.etmContextService = etmContextService;
        this.utilsService = utilsService;
        this.externalTJobExecutionRepository = externalTJobExecutionRepository;
        this.platformService = platformService;
        this.etmTestResultService = etmTestResultService;
        this.eimService = eimService;
    }

    @PreDestroy
    private void destroy() {
        for (HashMap.Entry<String, SharedAsyncModel<Void>> asyncMap : asyncExternalElasticsearchSutExecs
                .entrySet()) {
            this.stopManageSutByExternalElasticsearch(asyncMap.getKey());
        }
    }

    @Async
    public void execFromExternalJob(TJobExecution tJobExec,
            boolean integratedJenkins) {
        dbmanager.bindSession();
        tJobExec = tJobExecRepositoryImpl.findById(tJobExec.getId()).get();
        monitoringService
                .createMonitoringIndex(tJobExec.getMonitoringIndicesList());

        String resultMsg = "Initializing";
        tJobExec.setResultMsg(resultMsg);
        tJobExecRepositoryImpl.save(tJobExec);
        Execution execution = new Execution(tJobExec);
        execution.addObserver(new StatusUpdater());

        try {
            initSupportServicesProvision(execution,
                    tJobExec.getTjob().getSelectedServices());
            setTJobExecEnvVars(tJobExec, true, false);
            tJobExec = tJobExecRepositoryImpl.save(tJobExec);
            execution.updateFromTJobExec(tJobExec);

            // Start SuT if it's necessary
            if (execution.isWithSut()) {
                // Enable monitoring
                resultMsg = "Starting Dockbeat to get metrics...";
                updateExecutionResultStatus(execution, ResultEnum.IN_PROGRESS,
                        resultMsg);
                platformService.enableServiceMetricMonitoring(execution);
                initSut(execution, !integratedJenkins);
                tJobExec.setSutExecution(execution.getSutExec());
            }

            setTJobExecEnvVars(tJobExec, true, false);

            // Start Test
            resultMsg = "Executing Test";
            updateTJobExecResultStatus(tJobExec, ResultEnum.EXECUTING_TEST,
                    resultMsg);
        } catch (Exception e) {
            logger.error("Error starting a TSS or a SUT", e);
            resultMsg = "Error starting services on ElasTest";
            updateTJobExecResultStatus(tJobExec, ResultEnum.ERROR, resultMsg);
        }
        dbmanager.unbindSession();
    }

    @Async
    public Future<Void> executeTJob(TJobExecution tJobExec,
            String tJobServices) {
        dbmanager.bindSession();
        tJobExec.setResult(ResultEnum.IN_PROGRESS);
        String resultMsg = "Initializing";
        tJobExec = tJobExecRepositoryImpl.save(tJobExec);
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

        Execution execution = new Execution(tJobExec);
        execution.addObserver(new StatusUpdater());

        if (tJobExec.isMultiExecutionParent()) { // Parent execution
            resultMsg = "Executing Test";
            updateExecutionResultStatus(execution,
                    TJobExecution.ResultEnum.EXECUTING_TEST, resultMsg);

            for (TJobExecution childExec : tJobExec.getExecChilds()) {
                // If parent not stopped, execute
                if (execsAreStopped.containsKey(tJobExecMapName)
                        && !execsAreStopped.get(tJobExecMapName)) {
                    this.executeTJob(childExec, tJobServices);
                }
            }

            tJobExec.setEndDate(new Date());

            saveMultiParentFinishStatus(tJobExec, execution);

        } else { // Simple/child execution

            try {
                initSupportServicesProvision(new Execution(tJobExec),
                        tJobServices);
                setTJobExecEnvVars(tJobExec, false, false);
                tJobExec = tJobExecRepositoryImpl.save(tJobExec);
                execution.updateFromTJobExec(tJobExec);

                // Create queues and load basic services
                resultMsg = "Starting Dockbeat to get metrics...";
                updateExecutionResultStatus(execution, ResultEnum.IN_PROGRESS,
                        resultMsg);

                // Enable monitoring
                platformService.enableServiceMetricMonitoring(execution);

                // Start SuT if it's necessary
                if (execution.isWithSut()) {
                    initSut(execution);
                }

                // Run Test
                resultMsg = "Preparing Test";
                updateExecutionResultStatus(execution,
                        ResultEnum.EXECUTING_TEST, resultMsg);
                
                platformService.deployAndRunTJobExecution(execution);
                
                // Saving test suites
                if (!(execution.getTJobExec().getTestSuites().size() > 0)
                        && execution.getReportTestSuite() != null) {
                    logger.debug("Saving test suitest from a TJob on docker");
                    etmTestResultService.saveTestResults(
                            execution.getReportTestSuite(), tJobExec);
                } else if (execution.getReportTestSuite() == null) {
                    logger.debug("Saving test suitest from a TJob on k8s");
                    dbmanager.reloaEntityFromDb(tJobExec);
                }
                
                tJobExec.setEndDate(new Date());

                // Only if using External ES
                updateManageSutByExtESEndDate(tJobExec.getEndDate(), tJobExec);

                logger.info("Ending Execution {}...", tJobExec.getId());
                saveFinishStatus(tJobExec, execution);
                // End and purge services
                endAllExecs(execution);

            } catch (TJobStoppedException e) {
                // Stop exception
                logger.warn("TJob Exec {} stopped!", tJobExec.getId());
            } catch (Exception e) {
                logger.error("Error during TJob Execution {}", tJobExec.getId(),
                        e);
                if (!"end error".equals(e.getMessage())) {
                    resultMsg = "Internal Error: " + e.getMessage();
                    updateExecutionResultStatus(execution, ResultEnum.ERROR,
                            resultMsg);

                    tJobExec.setEndDate(new Date());
                    // Only if using External ES
                    updateManageSutByExtESEndDate(tJobExec.getEndDate(),
                            tJobExec);

                    try {
                        endAllExecs(execution);
                    } catch (Exception e1) {
                        logger.error("Error on end TJob Exec {}",
                                tJobExec.getId(), e1);
                    }
                }
            } finally {
                if (tJobServices != null && tJobServices != "") {
                    try {
                        deprovisionServices(new Execution(tJobExec));
                    } catch (Exception e) {
                        logger.error(
                                "TJob Exec {} => Exception on deprovision TSS: {}",
                                tJobExec.getId(), e.getMessage());
                        // TODO Customize Exception
                    }
                }
            }

            // Setting execution data
            tJobExec.setSutExecution(execution.getSutExec());
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
                updateTJobExecResultStatus(childExec, ResultEnum.STOPPED,
                        resultMsg);
            }
        }

        Execution execution = new Execution(tJobExec);
        execution.addObserver(new StatusUpdater());
        try {
            endAllExecs(execution, true);
        } catch (TJobStoppedException e) {
            // Stop exception
        } catch (Exception e) {
            logger.error("Exception during force end execution", e);
        }

        // Deprovision all TSS associated
        logger.debug("Requesting the TSS deprovision.");
        try {
            deprovisionServices(new Execution(tJobExec));
        } catch (Exception e) {
            logger.error("Exception during the deprovision of services");
        }

        if (tJobExec.getTjob().isExternal()) {
            String resultMsg = "Success";
            updateExecutionResultStatus(execution, ResultEnum.SUCCESS,
                    resultMsg);
        } else {
            String resultMsg = "Stopped";
            updateExecutionResultStatus(execution, ResultEnum.STOPPED,
                    resultMsg);
        }

        tJobExec = tJobExecRepositoryImpl.findById(tJobExec.getId()).get();
        return tJobExec;
    }

    public void saveMultiParentFinishStatus(TJobExecution tJobExec,
            Execution execution) {
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
        updateExecutionResultStatus(execution, finishStatus, resultMsg);
    }

    public void endAllExecs(Execution execution, boolean force)
            throws Exception {
        try {
            if (execution.isExternal()) {

            } else {
                platformService.undeployTJob(execution, force);
            }
            if (execution.isWithSut()) {
                platformService.undeploySut(execution, force);
                if (force) {
                    // Only stop if force, else stops automatically when ends
                    stopManageSutByExternalElasticsearch(
                            execution.getTJobExec());
                }
            }
            platformService.disableMetricMonitoring(execution, force);
        } catch (Exception e) {
            logger.error("Error on end all execs");
            throw new Exception("end error", e); // TODO Customize Exception
        }
    }

    public void endAllExecs(Execution execution) throws Exception {
        endAllExecs(execution, false);
    }

    public void releaseResourcesFromExtExecution(TJobExecution tJobExec) {
        try {
            deprovisionServices(new Execution(tJobExec));
        } catch (Exception e) {
            logger.error(
                    "Exception deprovisioning TSSs associated with an external execution.");
        }
        if (tJobExec.isWithSut()) {
            try {
                Execution execution = new Execution(tJobExec);
                execution.addObserver(new StatusUpdater());
                execution.setSutExec(tJobExec.getSutExecution());
                platformService.disableMetricMonitoring(execution, true);
                platformService.undeploySut(execution, false);
                stopManageSutByExternalElasticsearch(execution.getTJobExec());
            } catch (Exception e) {
                logger.error(
                        "Error deprovisioning a SUT used from an external execution.");
            }
        }

    }
    /* ******************* */
    /* *** TSS methods *** */
    /* ******************* */

    public void initSupportServicesProvision(Execution execution,
            String tJobServices) throws Exception {
        try {
            if (tJobServices != null && tJobServices != "") {
                provideServices(tJobServices, execution);

                // Wait only if not is mini. (In mini is already waiting for
                // them individually in provideService)
                if (!utilsService.isElastestMini()) {
                    esmService.waitForExecutionServicesAreReady(execution);
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

    private void provideServices(String tJobServices, Execution execution)
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
                    services, execution);

            for (TJobSupportService service : servicesWithoutEMS) {
                if (service.isSelected()) {
                    updateExecutionResultStatus(execution,
                            ResultEnum.STARTING_TSS,
                            resultMsg + service.getName());

                    this.provideService(service, execution);
                }
            }
        } catch (IOException e) {
            throw new Exception("Error on provide TSS", e);
        }
    }

    private String provideService(TJobSupportService service,
            Execution execution) {
        String instanceId = "";

        // If mini mode, provision async and show pulling
        // information
        String tssId = service.getId();
        if (utilsService.isElastestMini()) {
            instanceId = esmService.generateNewOrGetInstanceId(tssId);
            if (esmService.isSharedTssInstanceByServiceId(tssId)) {
                // If is shared, is started
                esmService.provisionExecutionSharedTSSSync(tssId, execution,
                        instanceId);
            } else {
                // Else provision async and wait after for tss
                esmService.provisionExecutionServiceInstanceAsync(tssId,
                        execution, instanceId);
            }

            String serviceName = esmService.getServiceNameByServiceId(tssId)
                    .toUpperCase();

            esmService.waitForTssStartedInMini(execution, instanceId,
                    serviceName);
        } else { // Sync provision
            instanceId = esmService.provisionExecutionServiceInstanceSync(tssId,
                    execution);
        }

        logger.debug("{} {} = > Adding TSS instance id {} to execution",
                execution.getTJobExecType(), execution.getExecutionId(),
                instanceId);

        if (execution.isExternal()) {
            execution.getExternalTJobExec().getServicesInstances()
                    .add(instanceId);
        } else {
            execution.getTJobExec().getServicesInstances().add(instanceId);
        }
        return instanceId;
    }

    public List<TJobSupportService> provideEmsTssIfSelected(
            List<TJobSupportService> services, Execution execution)
            throws JsonParseException, JsonMappingException, IOException {
        List<TJobSupportService> servicesWithoutEMS = new ArrayList<>(services);
        int pos = 0;
        for (TJobSupportService service : services) {
            if (service.getName().toLowerCase().equals("ems")
                    && service.isSelected()) {
                String instanceId = this.provideService(service, execution);
                servicesWithoutEMS.remove(pos);
                this.setExecutionTssEnvVars(execution, false, instanceId);
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
                .getTJobServiceInstanceById(tSSInstanceId);
        Map<String, String> tssInstanceEnvVars = esmService
                .getTSSInstanceEnvVars(ssi, externalTJob, withPublicPrefix);

        return tssInstanceEnvVars;
    }

    /*
     * Gets the Env vars of given External TJob TSS Instance
     */
    private Map<String, String> getExternalTJobExecTssEnvVars(
            boolean publicEnvVars, boolean withPublicPrefix,
            String tSSInstanceId) {
        SupportServiceInstance ssi = esmService
                .getExternalTJobServiceInstanceById(tSSInstanceId);
        Map<String, String> tssInstanceEnvVars = esmService
                .getTSSInstanceEnvVars(ssi, publicEnvVars, withPublicPrefix);

        return tssInstanceEnvVars;
    }

    /*
     * Sets the Env vars of given TSS Instance into generic execution
     */

    private void setExecutionTssEnvVars(Execution execution,
            boolean withPublicPrefix, String tSSInstanceId) {
        if (execution.isExternal()) {
            this.setExternalTJobExecTssEnvVars(execution.getExternalTJobExec(),
                    withPublicPrefix, tSSInstanceId);
        } else {
            this.setTJobExecTssEnvVars(execution.getTJobExec(),
                    execution.gettJob().isExternal(), false, tSSInstanceId);
        }
    }

    /*
     * Sets the Env vars of given TSS Instance into External TJobExec
     */
    private void setExternalTJobExecTssEnvVars(ExternalTJobExecution exTJobExec,
            boolean withPublicPrefix, String tSSInstanceId) {
        Map<String, String> envVars = new HashMap<>();
        envVars.putAll(exTJobExec.getEnvVars());
        envVars.putAll(this.getExternalTJobExecTssEnvVars(false,
                withPublicPrefix, tSSInstanceId));
        exTJobExec.setEnvVars(envVars);
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

        // Get EIM URL Env Var
        if (eimService.isStarted()) {
            envVars.put("ET_EIM_API", eimService.getEimApiUrl());
        }

        /*
         * Test File Attachments Api URL TODO build url in Context, not here
         */
        String attachmentsApiURL = etmContextService.getContextInfo()
                .getEtmApiUrl() + "tjob/exec/" + tJobExec.getId()
                + "/attachment";
        envVars.put("ET_ETM_TJOB_ATTACHMENT_API", attachmentsApiURL);

        // Generate the SUT name
        String sutContainerName = platformService.generateContainerName(
                PlatformService.ContainerPrefix.SUT, new Execution(tJobExec));
        // In tjobs make use of started EUS
        String etEusApiKey = "ET_EUS_API";
        if (envVars.containsKey(etEusApiKey)) {
            String eusApi = envVars.get(etEusApiKey);
            if (eusApi != null) {
                logger.info("This is the EUS's API URL: {}", eusApi);

                // If is Jenkins, config EUS to start browsers at sut network
                boolean useSutNetwork = tJobExec.getTjob().isExternal();
                EusExecutionData eusExecutionDate = new EusExecutionData(
                        tJobExec, "", useSutNetwork, sutContainerName);
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
        envVars.put("ET_SUT_CONTAINER_NAME", sutContainerName);

        tJobExec.setEnvVars(envVars);
    }

    public void deprovisionServices(Execution execution) {
        Long execId = execution.getExecutionId();
        String execType = execution.getTJobExecType();
        logger.info("{} {} => Start the services deprovision.", execType,
                execId);
        List<String> instancesAux = new ArrayList<String>();

        List<String> servicesInstances = esmService
                .getServicesInstancesByExecution(execution);

        if (servicesInstances != null && servicesInstances.size() > 0) {
            logger.debug(
                    "{} {} => Deprovisioning TJob's TSSs stored in the TJob object",
                    execType, execId);
            instancesAux = servicesInstances;
        } else if (esmService.gettSSIByExecutionAssociated(execution)
                .get(execId) != null) {
            logger.debug(
                    "{} {} => Deprovisioning TJob's TSSs stored in the EsmService",
                    execType, execId);
            instancesAux = esmService.gettSSIByExecutionAssociated(execution)
                    .get(execId);
        }

        logger.debug("{} {} => TSS list size: {}", execType, execId,
                instancesAux);
        for (String instanceId : instancesAux) {
            esmService.deprovisionExecutionServiceInstance(instanceId,
                    execution);
            logger.debug("{} {} => TSS Instance id to deprovision: {}",
                    execType, execId, instanceId);
        }
    }

    /* ********************* */
    /* *** External TJob *** */
    /* ********************* */

    @Async
    public Future<Void> executeExternalTJob(ExternalTJobExecution exec) {
        dbmanager.bindSession();
        exec = externalTJobExecutionRepository.findById(exec.getId()).get();

        Execution execution = new Execution(exec);
        execution.addObserver(new StatusUpdater());
        try {
            updateExecutionResultStatus(execution, ResultEnum.IN_PROGRESS,
                    "Initializing execution...");

            // TSS
            initSupportServicesProvision(execution,
                    exec.getExTJob().getSelectedServices());

            // SUT
            if (execution.isWithSut()) {
                this.initSut(execution);
            }

            String resultMsg = "Executing Test";
            updateExecutionResultStatus(execution, ResultEnum.EXECUTING_TEST,
                    resultMsg);
        } catch (Exception e) {
            logger.error("Error on execute External TJob {}", exec.getId(), e);
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

            Execution execution = new Execution(externalTJobExec);
            execution.addObserver(new StatusUpdater());
            try {
                platformService.disableMetricMonitoring(execution, false);
                platformService.undeploySut(execution, false);
                SutSpecification sut = execution.getSut();
                if (sut.getSutType() == SutTypeEnum.DEPLOYED) {
                    logger.info("SuT not ended by ElasTest -> Deployed SuT");
                    // Sut instrumented by EIM
                    if (sut.isInstrumentedByElastest()
                            && sut.isInstrumentalized()) {
                        logger.debug("TJob Exec {} => Undeploying Beats",
                                execution.getTJobExec().getId());
                        sut = sutService.undeployEimSutBeats(sut, false);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        if (exec.getExTJob().getSelectedServices() != null
                && exec.getExTJob().getSelectedServices() != "") {
            try {
                deprovisionServices(new Execution(exec));
            } catch (Exception e) {
                logger.error("TJob Exec {} => Exception on deprovision TSS: {}",
                        exec.getId(), e.getMessage());
                // TODO Customize Exception
            }
        }

        dbmanager.unbindSession();
        return new AsyncResult<Void>(null);
    }

    /* ******************* */
    /* *** SuT Methods *** */
    /* ******************* */

    public void initSut(Execution execution) throws Exception {
        initSut(execution, false);
    }

    public void initSut(Execution execution, boolean publicSut)
            throws Exception {
        try {
            SutSpecification sut = execution.getSut();
            SutExecution sutExec;

            // If it's SuT DEPLOYED outside ElasTest
            if (sut.getSutType() == SutTypeEnum.DEPLOYED) {
                logger.info("Using SUT deployed outside ElasTest");
                TJobExecution tJobExec = execution.getTJobExec();
                sutExec = startSutDeployedOutside(execution);

            }
            // If it's MANAGED SuT
            else {
                logger.info("Using SUT deployed by ElasTest");
                try {
                    if (execution.isExternal()) {
                        // If external start Dockbeat (for internal is already
                        // started)
                        platformService
                                .enableServiceMetricMonitoring(execution);
                    }

                    sutExec = startManagedSut(execution);
                    if (publicSut) {
                        ServiceBindedPort socatBindedPortObj = platformService
                                .getBindingPort(sutExec.getIp(),
                                        "sut_" + sutExec.getId(),
                                        execution.getSut().getPort(),
                                        elastestDockerNetwork);
                        sutExec.setPublicPort(Long
                                .parseLong(socatBindedPortObj.getBindedPort()));
                        sutService.modifySutExec(sutExec);
                    }
                } catch (TJobStoppedException e) {
                    throw e;
                }
            }

            execution.setSutExec(sutExec);

            if (execution.isExternal()) {
                execution.getExternalTJobExec().getEnvVars().put("ET_SUT_URL",
                        sutExec.getUrl());
            }
        } catch (TJobStoppedException e) {
            throw e;
        } catch (Exception e) {
            new Exception("Exception on init/start Sut", e);
        }
    }

    public SutExecution startSutDeployedOutside(Execution execution)
            throws Exception {
        SutSpecification sut = execution.getSut();
        SutExecution sutExec;
        String sutIP = "";

        try {
            String resultMsg = "Preparing External SuT";
            execution.updateTJobExecutionStatus(ResultEnum.WAITING_SUT,
                    resultMsg);

            // Sut instrumented by EIM
            if (sut.isInstrumentedByElastest() && sut.isInstrumentalized()) {
                execution.updateTJobExecutionStatus(ResultEnum.WAITING_SUT,
                        "Deploying beats in Sut");

                if (execution.isExternal()) {
                    logger.debug("External TJob Exec {} => Deploy Beats",
                            execution.getExternalTJobExec().getId());
                } else {
                    logger.debug("TJob Exec {} => Deploy Beats",
                            execution.getTJobExec().getId());
                }
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
                String key = getMapNameByExec(execution);
                Date startDate;
                if (execution.isExternal()) {
                    startDate = execution.getExternalTJobExec().getStartDate();
                } else {
                    startDate = execution.getTJobExec().getStartDate();
                }

                if (startDate == null) {
                    startDate = new Date();
                }

                Future<Void> asyncExElasticsearch = sutService
                        .manageSutExecutionUsingExternalElasticsearch(sut,
                                sutExec.getSutExecMonitoringIndex(), startDate,
                                asyncExternalElasticsearchSutExecs, key,
                                EXEC_END_DATE_KEY);

                SharedAsyncModel<Void> sharedExElasticsearchAsync = new SharedAsyncModel<>(
                        asyncExElasticsearch);

                asyncExternalElasticsearchSutExecs.put(key,
                        sharedExElasticsearchAsync);
            }

            return sutExec;
        } catch (Exception e) {
            throw new Exception("Error preparing the Sut to use in a test: "
                    + e.getMessage() + ".");
        }
    }

    private SutExecution startManagedSut(Execution execution) throws Exception {
        String resultMsg = "Preparing dockerized SuT";
        updateExecutionResultStatus(execution, ResultEnum.EXECUTING_SUT,
                resultMsg);
        logger.info(resultMsg + " " + execution.getExecutionId());

        SutExecution sutExec = sutService
                .createSutExecutionBySut(execution.getSut());
        execution.setSutExec(sutExec);
        try {
            platformService.deploySut(execution);
        } catch (TJobStoppedException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Exception during TJob execution", e);
            sutExec.setDeployStatus(SutExecution.DeployStatusEnum.ERROR);
            try {
                sutService.modifySutExec(execution.getSutExec());
            } catch (Exception e1) {

            }
            throw e;
        }
        return sutExec;
    }

    /* ************************* */
    /* *** TJob Exec Methods *** */
    /* ************************* */

    public String getMapNameByTJobExec(TJobExecution tJobExec) {
        return tJobExec.getTjob().getId() + "_" + tJobExec.getId();
    }

    public String getMapNameByExec(Execution execution) {
        if (execution.isExternal()) {
            return getMapNameByExternalTJobExec(
                    execution.getExternalTJobExec());
        } else {
            return getMapNameByTJobExec(execution.getTJobExec());
        }
    }

    public String getMapNameByExternalTJobExec(
            ExternalTJobExecution exTJobExec) {
        return exTJobExec.getExTJob().getId() + "_" + exTJobExec.getId();
    }

    public void updateManageSutByExtESEndDate(Date endDate,
            TJobExecution tJobExec) {
        this.updateManageSutByExtESEndDate(endDate,
                getMapNameByTJobExec(tJobExec));
    }

    public void updateManageSutByExtESEndDate(Date endDate, String mapKey) {
        if (!asyncExternalElasticsearchSutExecs.containsKey(mapKey)) {
            return;
        }

        asyncExternalElasticsearchSutExecs.get(mapKey).getData()
                .put(EXEC_END_DATE_KEY, endDate);
    }

    public void stopManageSutByExternalElasticsearch(TJobExecution tJobExec) {
        this.stopManageSutByExternalElasticsearch(
                getMapNameByTJobExec(tJobExec));
    }

    public void stopManageSutByExternalElasticsearch(String mapKey) {
        sutService.stopManageSutByExternalElasticsearch(
                asyncExternalElasticsearchSutExecs, mapKey);
    }

    public void updateExecutionResultStatus(Execution execution,
            ResultEnum result, String msg) {
        if (execution.isExternal()) {
            ExternalTJobExecution externalTJobExec = execution
                    .getExternalTJobExec();
            updateExternalTJobExecResultStatus(externalTJobExec, result, msg);
        } else {
            TJobExecution tJobExec = execution.getTJobExec();
            updateTJobExecResultStatus(tJobExec, result, msg);
        }
    }

    public void updateExecutionResultStatus(Execution execution) {
        if (execution.isExternal()) {
            ExternalTJobExecution externalTJobExec = execution
                    .getExternalTJobExec();
            updateExternalTJobExecResultStatus(externalTJobExec,
                    externalTJobExec.getResult(),
                    externalTJobExec.getResultMsg());
        } else {
            TJobExecution tJobExec = execution.getTJobExec();
            updateTJobExecResultStatus(tJobExec, tJobExec.getResult(),
                    tJobExec.getResultMsg());
        }
    }

    public TJobExecution updateTJobExecResultStatus(TJobExecution tJobExec,
            ResultEnum result, String msg) {
        tJobExec.setResult(result);
        tJobExec.setResultMsg(msg);
        return tJobExecRepositoryImpl.save(tJobExec);
    }

    public void updateExternalTJobExecResultStatus(
            ExternalTJobExecution externalTJobExec, ResultEnum result,
            String msg) {
        externalTJobExec.setResult(result);
        externalTJobExec.setResultMsg(msg);
        externalTJobExecutionRepository.save(externalTJobExec);
    }

    public void updateGenericExecResultStatus(Execution exection,
            ResultEnum result, String msg) {
        if (exection.isExternal()) {
            updateExternalTJobExecResultStatus(exection.getExternalTJobExec(),
                    result, msg);
        } else {
            updateTJobExecResultStatus(exection.getTJobExec(), result, msg);
        }
    }
    
    protected void saveFinishStatus(TJobExecution tJobExec, Execution execution) {
        logger.debug("Updating test results.");
        String resultMsg = "";
        ResultEnum finishStatus = ResultEnum.SUCCESS;
        tJobExec = execution.getTJobExec();
        tJobExec.setEndDate(new Date());

        if (tJobExec.getTestSuites() != null
                && tJobExec.getTestSuites().size() > 0) {
            logger.debug("There are Test Suites");
            for (TestSuite testSuite : tJobExec.getTestSuites()) {
                if (testSuite.getFinalStatus() == ResultEnum.FAIL) {
                    finishStatus = testSuite.getFinalStatus();
                    break;
                }
            }

        } else {
            if (execution.getExitCode() != 0) {
                finishStatus = ResultEnum.FAIL;
            }
        }

        resultMsg = "Finished: " + finishStatus;
        execution.updateTJobExecutionStatus(finishStatus, resultMsg);
    }

    public class StatusUpdater implements Observer {
        @Override
        public void update(Observable o, Object arg) {
            Execution execution = (Execution) arg;
            updateExecutionResultStatus(execution);
        }
    }
}