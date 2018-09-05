package io.elastest.etm.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.xml.ws.http.HTTPException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.elastest.etm.api.model.ExternalJob;
import io.elastest.etm.api.model.ExternalJob.ExternalJobStatusEnum;
import io.elastest.etm.api.model.TestSupportServices;
import io.elastest.etm.dao.external.ExternalProjectRepository;
import io.elastest.etm.dao.external.ExternalTJobExecutionRepository;
import io.elastest.etm.dao.external.ExternalTJobRepository;
import io.elastest.etm.dao.external.ExternalTestCaseRepository;
import io.elastest.etm.dao.external.ExternalTestExecutionRepository;
import io.elastest.etm.model.EusExecutionData;
import io.elastest.etm.model.HelpInfo;
import io.elastest.etm.model.Project;
import io.elastest.etm.model.SupportService;
import io.elastest.etm.model.SutSpecification;
import io.elastest.etm.model.TJob;
import io.elastest.etm.model.TJobExecution;
import io.elastest.etm.model.TJobExecution.ResultEnum;
import io.elastest.etm.model.TJobExecutionFile;
import io.elastest.etm.model.VersionInfo;
import io.elastest.etm.model.external.ExternalProject;
import io.elastest.etm.model.external.ExternalProject.TypeEnum;
import io.elastest.etm.model.external.ExternalTJob;
import io.elastest.etm.model.external.ExternalTJobExecution;
import io.elastest.etm.model.external.ExternalTestCase;
import io.elastest.etm.model.external.ExternalTestExecution;
import io.elastest.etm.utils.UtilTools;
import io.elastest.etm.utils.UtilsService;

@Service
/**
 * This service implements the logic required for external clients.
 * 
 * @author frdiaz
 *
 */
public class ExternalService {
    private static final Logger logger = LoggerFactory
            .getLogger(ExternalService.class);

    private ProjectService projectService;
    private TJobService tJobService;
    private TJobExecOrchestratorService tJobExecOrchestratorService;

    @Value("${exec.mode}")
    public String execMode;

    @Value("${server.port}")
    private String serverPort;

    @Value("${et.etm.lshttp.port}")
    private String etEtmLsHttpPort;

    @Value("${et.public.host}")
    private String etPublicHost;

    @Value("${et.etm.api}")
    private String etEtmApi;

    @Value("${et.proxy.port}")
    private String etProxyPort;

    @Value("${et.in.prod}")
    public boolean etInProd;

    @Value("${et.etm.dev.gui.port}")
    public String etEtmDevGuiPort;

    @Value("${et.etm.internal.host}")
    private String etEtmInternalHost;

    private Map<Long, ExternalJob> runningExternalJobs;

    private final ExternalProjectRepository externalProjectRepository;
    private final ExternalTestCaseRepository externalTestCaseRepository;
    private final ExternalTestExecutionRepository externalTestExecutionRepository;
    private final ExternalTJobRepository externalTJobRepository;
    private final ExternalTJobExecutionRepository externalTJobExecutionRepository;

    private final EsmService esmService;
    private EtmContextService etmContextService;
    private LogstashService logstashService;

    private MonitoringServiceInterface monitoringService;
    private UtilsService utilsService;
    private SutService sutService;

    public ExternalService(ProjectService projectService,
            TJobService tJobService,
            ExternalProjectRepository externalProjectRepository,
            ExternalTestCaseRepository externalTestCaseRepository,
            ExternalTestExecutionRepository externalTestExecutionRepository,
            ExternalTJobRepository externalTJobRepository,
            ExternalTJobExecutionRepository externalTJobExecutionRepository,
            EsmService esmService, MonitoringServiceInterface monitoringService,
            EtmContextService etmContextService,
            LogstashService logstashService, UtilsService utilsService,
            TJobExecOrchestratorService tJobExecOrchestratorService,
            SutService sutService) {
        super();
        this.projectService = projectService;
        this.tJobService = tJobService;
        this.externalProjectRepository = externalProjectRepository;
        this.externalTestCaseRepository = externalTestCaseRepository;
        this.externalTestExecutionRepository = externalTestExecutionRepository;
        this.externalTJobRepository = externalTJobRepository;
        this.runningExternalJobs = new HashMap<>();
        this.externalTJobExecutionRepository = externalTJobExecutionRepository;
        this.esmService = esmService;
        this.monitoringService = monitoringService;
        this.etmContextService = etmContextService;
        this.logstashService = logstashService;
        this.utilsService = utilsService;
        this.tJobExecOrchestratorService = tJobExecOrchestratorService;
        this.sutService = sutService;
    }

    public ExternalJob executeExternalTJob(ExternalJob externalJob) {
        logger.info("Executing TJob from external Job.");
        externalJob.setStatus(ExternalJobStatusEnum.STARTING);
        try {
            logger.debug("Creating TJob data structure.");
            TJob tJob = createElasTestEntitiesForExtJob(externalJob);

            logger.debug("Creating TJobExecution.");
            TJobExecution tJobExec = tJobService.executeTJob(tJob.getId(),
                    new ArrayList<>(), new ArrayList<>());

            externalJob.setExecutionUrl(
                    (etInProd ? "http://" + etPublicHost + ":" + etProxyPort
                            : "http://localhost" + ":" + etEtmDevGuiPort)
                            + "/#/projects/" + tJob.getProject().getId()
                            + "/tjob/" + tJob.getId() + "/tjob-exec/"
                            + tJobExec.getId() + "/dashboard");
            externalJob.setLogAnalyzerUrl(
                    (etInProd ? "http://" + etPublicHost + ":" + etProxyPort
                            : "http://localhost" + ":" + etEtmDevGuiPort)
                            + "/#/logmanager?indexName=" + tJobExec.getId());
            // externalJob.setEnvVars(tJobExec.getEnvVars());
            externalJob.setServicesIp((externalJob.isFromIntegratedJenkins()
                    && etPublicHost.equals("localhost")) ? etEtmInternalHost
                            : etPublicHost);
            externalJob.setLogstashPort((externalJob.isFromIntegratedJenkins()
                    && etPublicHost.equals("localhost")) ? etEtmLsHttpPort
                            : etProxyPort);
            externalJob.settJobExecId(tJobExec.getId());

            runningExternalJobs.put(externalJob.gettJobExecId(), externalJob);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Error message: " + e.getMessage());
            externalJob.setStatus(ExternalJobStatusEnum.ERROR);
            externalJob.setError(e.getMessage());
        }

        return externalJob;
    }

    public void endExtTJobExecution(ExternalJob externalJob) {
        tJobService.endExternalTJobExecution(externalJob.gettJobExecId(),
                externalJob.getResult(), externalJob.getTestResults());
        runningExternalJobs.remove(externalJob.gettJobExecId());

    }

    public ExternalJob isReadyTJobForExternalExecution(Long tJobExecId) {
        ExternalJob externalJob = runningExternalJobs.get(tJobExecId);
        TJobExecution tJobExecution = tJobService
                .getTJobExecById(externalJob.gettJobExecId());
        if (tJobExecution.getResult() != (ResultEnum.ERROR)) {
            if (externalJob.getTSServices() != null
                    && externalJob.getTSServices().size() > 0) {
                if (tJobExecution.getEnvVars() != null
                        && !tJobExecution.getEnvVars().isEmpty()) {
                    externalJob.setEnvVars(tJobExecution.getEnvVars());
                    externalJob.setReady(true);
                    externalJob.setStatus(ExternalJobStatusEnum.READY);

                } else {
                    externalJob.setReady(false);
                }
            } else {
                externalJob.setReady(true);
                externalJob.setStatus(ExternalJobStatusEnum.READY);
            }
        } else {
            externalJob.setReady(false);
            externalJob.setStatus(ExternalJobStatusEnum.ERROR);
            externalJob.setError(tJobExecution.getResultMsg());
        }
        return externalJob;
    }

    public String getElasTestVersion() {
        String version = "undefined";
        HelpInfo helpInfo = etmContextService.getHelpInfo();
        for (Map.Entry<String, VersionInfo> entry : helpInfo.getVersionsInfo()
                .entrySet()) {
            if (entry.getKey().split(":")[0].equals("elastest/platform")) {
                version = entry.getValue().getTag();
                logger.debug("ElasTest version {}", version);
                break;
            }
        }
        return version;
    }

    private TJob createElasTestEntitiesForExtJob(ExternalJob externalJob)
            throws Exception {
        logger.info("Creating external job entities.");

        try {
            logger.debug("Creating Project.");
            Project project = projectService
                    .getProjectByName(externalJob.getJobName());
            if (project == null) {
                project = new Project();
                project.setId(0L);
                project.setName(externalJob.getJobName());
                project = projectService.createProject(project);
            }

            // If a SUT is required, retrieved to associate it with both the
            // Project and the TJob
            SutSpecification sutAux = null;
            if (externalJob.getSut() != null
                    && externalJob.getSut().getId() != null) {
                logger.debug("Sut id requested by the Jenkins' Job {}",
                        externalJob.getSut().getId());
                boolean sutExists = false;
                for (SutSpecification sutSpec : project.getSuts()) {
                    if (sutSpec.getId() == externalJob.getSut().getId()) {
                        sutExists = true;
                        sutAux = sutSpec;
                    }
                }

                if (!sutExists) {
                    try {
                        sutAux = sutService
                                .getSutSpecById(externalJob.getSut().getId());
                        project.getSuts().add(sutAux);
                        projectService.createProject(project);
                    } catch (Exception e) {
                        throw new Exception(
                                "There isn't Sut with the provided id: "
                                        + externalJob.getSut().getId());
                    }
                }
            }

            logger.debug("Creating TJob.");
            TJob tJob = tJobService.getTJobByName(externalJob.getJobName());
            if (tJob == null) {
                tJob = new TJob();
                tJob.setName(externalJob.getJobName());
                tJob.setProject(project);
                tJob.setExternal(true);
            }

            if (sutAux != null) {
                tJob.setSut(sutAux);
            }

            tJob = tJobService.createTJob(tJob);

            if (externalJob.getTSServices() != null
                    && externalJob.getTSServices().size() > 0) {
                tJob.setSelectedServices("[");

                for (TestSupportServices tSService : externalJob
                        .getTSServices()) {
                    tJob.setSelectedServices(tJob.getSelectedServices()
                            + tSService.toJsonString());
                }

                tJob.setSelectedServices(tJob.getSelectedServices() + "]");
            }

            return tJob;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Error message: " + e.getMessage());
            throw e;
        }
    }

    public Map<Long, ExternalJob> getRunningExternalJobs() {
        return runningExternalJobs;
    }

    public void setRunningExternalJobs(
            Map<Long, ExternalJob> runningExternalJobs) {
        this.runningExternalJobs = runningExternalJobs;
    }

    /* *************************************************/
    /* *************** ExternalProject *************** */
    /* *************************************************/

    public List<ExternalProject> getAllExternalProjects() {
        return this.externalProjectRepository.findAll();
    }

    public List<ExternalProject> getAllExternalProjectsByType(TypeEnum type) {
        return this.externalProjectRepository.findAllByType(type);
    }

    public ExternalProject getExternalProjectById(Long id) {
        Optional<ExternalProject> exProject = this.externalProjectRepository
                .findById(id);
        return exProject.isPresent() ? exProject.get() : null;
    }

    public ExternalProject getExternalProjectByExternalIdAndSystemId(
            String externalId, String externalSystemId) {
        return this.externalProjectRepository
                .findByExternalIdAndExternalSystemId(externalId,
                        externalSystemId);
    }

    /* **************************************************/
    /* ***************** ExternalTJob ***************** */
    /* **************************************************/

    public List<ExternalTJob> getAllExternalTJobs() {
        return this.externalTJobRepository.findAll();
    }

    public ExternalTJob getExternalTJobById(Long tjobId) {
        Optional<ExternalTJob> exTJob = this.externalTJobRepository
                .findById(tjobId);
        return exTJob.isPresent() ? exTJob.get() : null;
    }

    public ExternalTJob getExternalTJobByExternalIdAndSystemId(
            String externalId, String externalSystemId) {
        return this.externalTJobRepository.findByExternalIdAndExternalSystemId(
                externalId, externalSystemId);
    }

    public ExternalTJob createExternalTJob(ExternalTJob body) {
        return this.externalTJobRepository.save(body);
    }

    public ExternalTJob modifyExternalTJob(ExternalTJob externalTJob) {
        if (externalTJobRepository.findById(externalTJob.getId()) != null) {
            return externalTJobRepository.save(externalTJob);
        } else {
            throw new HTTPException(405);
        }
    }
    /* **************************************************/
    /* *************** ExternalTJobExec *************** */
    /* **************************************************/

    public List<ExternalTJobExecution> getAllExternalTJobExecs() {
        return this.externalTJobExecutionRepository.findAll();
    }

    public List<ExternalTJobExecution> getExternalTJobExecsByExternalTJobId(
            Long tJobId) {
        ExternalTJob exTJob = this.externalTJobRepository.findById(tJobId)
                .get();
        if (exTJob != null) {
            return this.externalTJobExecutionRepository.findByExTJob(exTJob);
        } else {
            return null;
        }

    }

    public ExternalTJobExecution getExternalTJobExecById(Long tJobExecId) {
        Optional<ExternalTJobExecution> exTJobExec = this.externalTJobExecutionRepository
                .findById(tJobExecId);
        return exTJobExec.isPresent() ? exTJobExec.get() : null;
    }

    public ExternalTJobExecution createExternalTJobExecution(
            ExternalTJobExecution exec) {
        exec = this.externalTJobExecutionRepository.save(exec);
        if (exec.getMonitoringIndex().isEmpty()
                || "".equals(exec.getMonitoringIndex())) {
            exec.generateMonitoringIndex();
            exec = this.externalTJobExecutionRepository.save(exec);
        }

        exec = startEus(exec);

        monitoringService
                .createMonitoringIndex(exec.getMonitoringIndicesList());

        return exec;
    }

    public ExternalTJobExecution createExternalTJobExecutionByExternalTJobId(
            Long exTJobId) {
        ExternalTJob exTJob = this.externalTJobRepository.findById(exTJobId)
                .get();
        ExternalTJobExecution exec = new ExternalTJobExecution();
        exec.setExTJob(exTJob);
        exec.setStartDate(new Date());

        exec = this.externalTJobExecutionRepository.save(exec);
        exec.generateMonitoringIndex();
        exec = this.externalTJobExecutionRepository.save(exec);

        //

        if (exTJob.getExProject().getType().equals(TypeEnum.TESTLINK)) {
            exec = startEus(exec);
        }

        tJobExecOrchestratorService.executeExternalTJob(exec);

        monitoringService
                .createMonitoringIndex(exec.getMonitoringIndicesList());

        return exec;
    }

    // Modify or end ExternalTJobExecution
    public ExternalTJobExecution modifyExternalTJobExec(
            ExternalTJobExecution externalTJobExec) {
        if (externalTJobExecutionRepository
                .findById(externalTJobExec.getId()) != null) {
            externalTJobExec = externalTJobExecutionRepository
                    .save(externalTJobExec);

            // End if is finished
            tJobExecOrchestratorService.endExternalTJob(externalTJobExec);

            return externalTJobExec;
        } else {
            throw new HTTPException(405);
        }
    }

    public ExternalTJobExecution startEus(ExternalTJobExecution exec) {

        List<SupportService> tssList = esmService.getRegisteredServices();
        SupportService eus = null;
        for (SupportService tss : tssList) {
            if ("eus".equals(tss.getName().toLowerCase())) {
                eus = tss;
                break;
            }
        }

        if (eus != null) {
            String instanceId = UtilTools.generateUniqueId();

            if (utilsService.isElastestMini()) { // use started instance
                instanceId = esmService
                        .provisionExternalTJobExecServiceInstanceSync(
                                eus.getId(), exec);

                // Get new EUS API
                String etEusApiKey = "ET_EUS_API";
                EusExecutionData eusExecutionDate = new EusExecutionData(exec,
                        "");
                String eusApi = esmService.getSharedTssInstance(instanceId)
                        .getApiUrlIfExist();
                eusApi = eusApi.endsWith("/") ? eusApi : eusApi + "/";
                eusApi += "/execution/" + eusExecutionDate.getKey() + "/";
                exec.getEnvVars().put(etEusApiKey, eusApi);

                String eusHost = esmService.getSharedTssInstance(instanceId)
                        .getServiceIp();
                exec.getEnvVars().put("ET_EUS_HOST", eusHost);

                String eusPort = Integer.toString(esmService
                        .getSharedTssInstance(instanceId).getServicePort());
                exec.getEnvVars().put("ET_EUS_PORT", eusPort);

            } else { // Start new EUS instance
                esmService.provisionExternalTJobExecServiceInstanceAsync(
                        eus.getId(), exec, instanceId);
            }
            exec.getEnvVars().put("EUS_ID", eus.getId());
            exec.getEnvVars().put("EUS_INSTANCE_ID", instanceId);
            exec = this.externalTJobExecutionRepository.save(exec);
        }

        return exec;
    }

    public List<TJobExecutionFile> getExternalTJobExecutionFilesUrls(
            Long exTJobExecId) throws InterruptedException {
        ExternalTJobExecution exTJobExec = externalTJobExecutionRepository
                .findById(exTJobExecId).get();
        return esmService.getExternalTJobExecutionFilesUrls(
                exTJobExec.getExTJob().getId(), exTJobExecId);
    }

    public List<ExternalTestExecution> getTJobExecTestExecutions(
            Long tJobExecId) {
        ExternalTJobExecution tJobExec = externalTJobExecutionRepository
                .findById(tJobExecId).get();
        return externalTestExecutionRepository.findByExTJobExec(tJobExec);
    }

    /* **************************************************/
    /* *************** ExternalTestCase *************** */
    /* **************************************************/

    public List<ExternalTestCase> getAllExternalTestCases() {
        return this.externalTestCaseRepository.findAll();
    }

    public ExternalTestCase getExternalTestCaseById(Long id) {
        return this.externalTestCaseRepository.findById(id).get();
    }

    public ExternalTestCase getExternalTestCaseByExternalIdAndSystemId(
            String externalId, String externalSystemId) {
        return this.externalTestCaseRepository
                .findByExternalIdAndExternalSystemId(externalId,
                        externalSystemId);
    }

    /* *************************************************/
    /* ************ ExternalTestExecution ************ */
    /* *************************************************/

    public List<ExternalTestExecution> getAllExternalTestExecutions() {
        return this.externalTestExecutionRepository.findAll();
    }

    public ExternalTestExecution getExternalTestExecutionById(Long id) {
        return this.externalTestExecutionRepository.findById(id).get();
    }

    public ExternalTestExecution getExternalTestExecByExternalIdAndSystemId(
            String externalId, String externalSystemId) {
        return this.externalTestExecutionRepository
                .findByExternalIdAndExternalSystemId(externalId,
                        externalSystemId);
    }

    public List<ExternalTestExecution> getExternalTestExecutionsByExternalTJobExec(
            Long exTJobExecId) {
        ExternalTJobExecution exTJobExec = this.externalTJobExecutionRepository
                .findById(exTJobExecId).get();
        return this.externalTestExecutionRepository
                .findByExTJobExec(exTJobExec);
    }

    public ExternalTestExecution createExternalTestExecution(
            ExternalTestExecution exec) {
        this.logstashService.sendStartTestLogtrace(
                exec.getTestMonitoringIndex(), exec.getExTestCase().getName());
        return this.externalTestExecutionRepository.save(exec);
    }

    public ExternalTestExecution modifyExternalTestExecution(
            ExternalTestExecution exec) {
        if (externalTestExecutionRepository.findById(exec.getId()) != null) {
            this.logstashService.sendFinishTestLogtrace(
                    exec.getTestMonitoringIndex(),
                    exec.getExTestCase().getName());
            return externalTestExecutionRepository.save(exec);
        } else {
            throw new HTTPException(405);
        }
    }

    public ExternalTestExecution setExternalTJobExecToTestExecutionByExecutionId(
            Integer execId, Long exTJobExecId) {
        ExternalTJobExecution exTJobExec = this.externalTJobExecutionRepository
                .findById(exTJobExecId).get();
        ExternalTestExecution exTestExec = this.externalTestExecutionRepository
                .findByExternalIdAndExternalSystemId(execId.toString(),
                        exTJobExec.getExTJob().getExternalSystemId());
        exTestExec.setExTJobExec(exTJobExec);
        return this.externalTestExecutionRepository.save(exTestExec);
    }
}
