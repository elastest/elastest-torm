package io.elastest.etm.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.ws.http.HTTPException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

import io.elastest.etm.api.model.ExternalJob;
import io.elastest.etm.api.model.TestSupportServices;
import io.elastest.etm.dao.external.ExternalProjectRepository;
import io.elastest.etm.dao.external.ExternalTJobExecutionRepository;
import io.elastest.etm.dao.external.ExternalTJobRepository;
import io.elastest.etm.dao.external.ExternalTestCaseRepository;
import io.elastest.etm.dao.external.ExternalTestExecutionRepository;
import io.elastest.etm.model.HelpInfo;
import io.elastest.etm.model.Project;
import io.elastest.etm.model.SupportService;
import io.elastest.etm.model.TJob;
import io.elastest.etm.model.TJobExecution;
import io.elastest.etm.model.TJobExecutionFile;
import io.elastest.etm.model.VersionInfo;
import io.elastest.etm.model.external.ExternalProject;
import io.elastest.etm.model.external.ExternalProject.TypeEnum;
import io.elastest.etm.service.ElasticsearchService.IndexAlreadyExistException;
import io.elastest.etm.model.external.ExternalTJob;
import io.elastest.etm.model.external.ExternalTJobExecution;
import io.elastest.etm.model.external.ExternalTestCase;
import io.elastest.etm.model.external.ExternalTestExecution;
import io.elastest.etm.utils.UtilTools;

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

    @Value("${server.port}")
    private String serverPort;

    @Value("${et.edm.elasticsearch.api}")
    private String elasticsearchUrl;

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

    @Value("${et.edm.elasticsearch.api}")
    private String elasticsearchHost;

    private Map<Long, ExternalJob> runningExternalJobs;

    public UtilTools utilTools;

    private final ExternalProjectRepository externalProjectRepository;
    private final ExternalTestCaseRepository externalTestCaseRepository;
    private final ExternalTestExecutionRepository externalTestExecutionRepository;
    private final ExternalTJobRepository externalTJobRepository;
    private final ExternalTJobExecutionRepository externalTJobExecutionRepository;

    private final EsmService esmService;
    private ElasticsearchService elasticsearchService;
    private EtmContextService etmContextService;
    private LogstashService logstashService;

    public ExternalService(UtilTools utilTools, ProjectService projectService,
            TJobService tJobService,
            ExternalProjectRepository externalProjectRepository,
            ExternalTestCaseRepository externalTestCaseRepository,
            ExternalTestExecutionRepository externalTestExecutionRepository,
            ExternalTJobRepository externalTJobRepository,
            ExternalTJobExecutionRepository externalTJobExecutionRepository,
            EsmService esmService, ElasticsearchService elasticsearchService,
            EtmContextService etmContextService,
            LogstashService logstashService) {
        super();
        this.utilTools = utilTools;
        this.projectService = projectService;
        this.tJobService = tJobService;
        this.externalProjectRepository = externalProjectRepository;
        this.externalTestCaseRepository = externalTestCaseRepository;
        this.externalTestExecutionRepository = externalTestExecutionRepository;
        this.externalTJobRepository = externalTJobRepository;
        this.runningExternalJobs = new HashMap<>();
        this.externalTJobExecutionRepository = externalTJobExecutionRepository;
        this.esmService = esmService;
        this.elasticsearchService = elasticsearchService;
        this.etmContextService = etmContextService;
        this.logstashService = logstashService;
    }

    public ExternalJob executeExternalTJob(ExternalJob externalJob)
            throws Exception {
        logger.info("Executing TJob from external Job.");
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
            externalJob.setServicesIp(etPublicHost);
            externalJob
                    .setLogstashPort(etInProd ? etProxyPort : etEtmLsHttpPort);
            externalJob.settJobExecId(tJobExec.getId());

            runningExternalJobs.put(externalJob.gettJobExecId(), externalJob);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Error message: " + e.getMessage());
            throw e;
        }

        return externalJob;
    }

    public void endExtTJobExecution(ExternalJob externalJob) {
        tJobService.endExternalTJobExecution(externalJob.gettJobExecId(),
                externalJob.getResult());
        runningExternalJobs.remove(externalJob.gettJobExecId());
    }

    public ExternalJob isReadyTJobForExternalExecution(Long tJobExecId) {
        ExternalJob externalJob = runningExternalJobs.get(tJobExecId);
        if (externalJob.getTSServices() != null
                && externalJob.getTSServices().size() > 0) {
            TJobExecution tJobExecution = tJobService
                    .getTJobExecById(externalJob.gettJobExecId());
            if (tJobExecution.getEnvVars() != null
                    && !tJobExecution.getEnvVars().isEmpty()) {
                externalJob.setEnvVars(tJobExecution.getEnvVars());
                externalJob.setReady(true);
                return externalJob;
            } else {
                externalJob.setReady(false);
                return externalJob;
            }
        } else {
            externalJob.setReady(true);
            return externalJob;
        }
    }

    public String getElasTestVersion() {
        String version = "undefined";
        HelpInfo helpInfo = etmContextService.getHelpInfo();
        for (Map.Entry<String, VersionInfo> entry : helpInfo.getVersionsInfo()
                .entrySet()) {
            if (entry.getKey().split(":")[0].equals("elastest/platform")) {
                version = entry.getValue().getName();
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

            logger.debug("Creating TJob.");
            TJob tJob = tJobService.getTJobByName(externalJob.getJobName());
            if (tJob == null) {
                tJob = new TJob();
                tJob.setName(externalJob.getJobName());
                tJob = tJobService.createTJob(tJob);
                tJob.setProject(project);
                tJob.setExternal(true);
            }

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
        return this.externalProjectRepository.findById(id);
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
        return this.externalTJobRepository.findById(tjobId);
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
        if (externalTJobRepository.findOne(externalTJob.getId()) != null) {
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
        ExternalTJob exTJob = this.externalTJobRepository.findById(tJobId);
        return this.externalTJobExecutionRepository.findByExTJob(exTJob);
    }

    public ExternalTJobExecution getExternalTJobExecById(Long tJobExecId) {
        return this.externalTJobExecutionRepository.findById(tJobExecId);
    }

    public ExternalTJobExecution createExternalTJobExecution(
            ExternalTJobExecution exec) {
        exec = this.externalTJobExecutionRepository.save(exec);
        if (exec.getMonitoringIndex().isEmpty()
                || "".equals(exec.getMonitoringIndex())) {
            exec.generateMonitoringIndex();
            exec = this.externalTJobExecutionRepository.save(exec);
        }

        SupportService eus = this.startEus();

        if (eus != null) {
            String instanceId = utilTools.generateUniqueId();
            esmService.provisionExternalTJobExecServiceInstanceAsync(
                    eus.getId(), exec, instanceId);
            exec.getEnvVars().put("EUS_ID", eus.getId());
            exec.getEnvVars().put("EUS_INSTANCE_ID", instanceId);
        }

        this.createMonitoringIndex(exec);

        return exec;
    }

    public ExternalTJobExecution createExternalTJobExecutionByExternalTJobId(
            Long exTJobId) {
        ExternalTJob exTJob = this.externalTJobRepository.findById(exTJobId);
        ExternalTJobExecution exec = new ExternalTJobExecution();
        exec.setExTJob(exTJob);
        exec.setStartDate(new Date());

        this.externalTJobExecutionRepository.save(exec);
        exec.generateMonitoringIndex();
        exec = this.externalTJobExecutionRepository.save(exec);

        SupportService eus = this.startEus();

        if (eus != null) {
            String instanceId = utilTools.generateUniqueId();
            esmService.provisionExternalTJobExecServiceInstanceAsync(
                    eus.getId(), exec, instanceId);
            exec.getEnvVars().put("EUS_ID", eus.getId());
            exec.getEnvVars().put("EUS_INSTANCE_ID", instanceId);
            exec = this.externalTJobExecutionRepository.save(exec);
        }

        this.createMonitoringIndex(exec);

        return exec;
    }

    public ExternalTJobExecution modifyExternalTJobExec(
            ExternalTJobExecution externalTJobExec) {
        if (externalTJobExecutionRepository
                .findOne(externalTJobExec.getId()) != null) {
            return externalTJobExecutionRepository.save(externalTJobExec);
        } else {
            throw new HTTPException(405);
        }
    }

    public SupportService startEus() {
        List<SupportService> tssList = esmService.getRegisteredServices();
        SupportService eus = null;
        for (SupportService tss : tssList) {
            if ("eus".equals(tss.getName().toLowerCase())) {
                eus = tss;
                break;
            }
        }
        return eus;
    }

    public List<TJobExecutionFile> getExternalTJobExecutionFilesUrls(
            Long exTJobExecId) throws InterruptedException {
        ExternalTJobExecution exTJobExec = externalTJobExecutionRepository
                .findById(exTJobExecId);
        return esmService.getExternalTJobExecutionFilesUrls(
                exTJobExec.getExTJob().getId(), exTJobExecId);
    }

    public void createMonitoringIndex(ExternalTJobExecution exec) { // TODO
                                                                    // Refactor
                                                                    // Duplicated
        logger.info("Creating ES indices...");
        String[] indicesList = exec.getMonitoringIndicesList();
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
                logger.info("Index {} created", index);
            } catch (IndexAlreadyExistException e) {
                logger.error("Index {} already exist", index);
            } catch (HttpClientErrorException e) {
                logger.error("Index {} already exist", index);
            } catch (RestClientException e) {
                logger.error("Error creating index {}", index, e);
            } finally {
                // Enable Fielddata for components, streams and levels
                enableESFieldData(index, url, "component");
                enableESFieldData(index, url, "stream");
                enableESFieldData(index, url, "level");
                enableESFieldData(index, url, "type");
            }
        }
        logger.info("ES indices created!");
    }

    public void enableESFieldData(String index, String url, String field) {
        logger.info("Enabling FieldData for {} in index {}", field, index);
        elasticsearchService.enableFieldData(url, field);
    }

    public List<ExternalTestExecution> getTJobExecTestExecutions(
            Long tJobExecId) {
        ExternalTJobExecution tJobExec = externalTJobExecutionRepository
                .findById(tJobExecId);
        return externalTestExecutionRepository.findByExTJobExec(tJobExec);
    }

    /* **************************************************/
    /* *************** ExternalTestCase *************** */
    /* **************************************************/

    public List<ExternalTestCase> getAllExternalTestCases() {
        return this.externalTestCaseRepository.findAll();
    }

    public ExternalTestCase getExternalTestCaseById(Long id) {
        return this.externalTestCaseRepository.findById(id);
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
        return this.externalTestExecutionRepository.findById(id);
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
                .findById(exTJobExecId);
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
        if (externalTestExecutionRepository.findOne(exec.getId()) != null) {
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
                .findById(exTJobExecId);
        ExternalTestExecution exTestExec = this.externalTestExecutionRepository
                .findByExternalIdAndExternalSystemId(execId.toString(),
                        exTJobExec.getExTJob().getExternalSystemId());
        exTestExec.setExTJobExec(exTJobExec);
        return this.externalTestExecutionRepository.save(exTestExec);
    }
}
