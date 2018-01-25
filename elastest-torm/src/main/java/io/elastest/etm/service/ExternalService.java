package io.elastest.etm.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.elastest.etm.api.model.ExternalJob;
import io.elastest.etm.api.model.TestSupportServices;
import io.elastest.etm.dao.external.ExternalProjectRepository;
import io.elastest.etm.dao.external.ExternalTestCaseRepository;
import io.elastest.etm.dao.external.ExternalTestExecutionRepository;
import io.elastest.etm.model.Project;
import io.elastest.etm.model.TJob;
import io.elastest.etm.model.TJobExecution;
import io.elastest.etm.model.external.ExternalId;
import io.elastest.etm.model.external.ExternalProject;
import io.elastest.etm.model.external.ExternalProject.TypeEnum;
import io.elastest.etm.model.external.ExternalTestCase;
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

    private Map<Long, ExternalJob> runningExternalJobs;

    public UtilTools utilTools;

    private final ExternalProjectRepository externalProjectRepository;
    private final ExternalTestCaseRepository externalTestCaseRepository;
    private final ExternalTestExecutionRepository externalTestExecutionRepository;

    public ExternalService(ProjectService projectService,
            TJobService tJobService,
            ExternalProjectRepository externalProjectRepository,
            ExternalTestCaseRepository externalTestCaseRepository,
            ExternalTestExecutionRepository externalTestExecutionRepository) {
        super();
        this.projectService = projectService;
        this.tJobService = tJobService;
        this.externalProjectRepository = externalProjectRepository;
        this.externalTestCaseRepository = externalTestCaseRepository;
        this.externalTestExecutionRepository = externalTestExecutionRepository;
        this.runningExternalJobs = new HashMap<>();
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
                            + "/#/logmanager?indexName="
                            + tJobExec.getId());
            //externalJob.setEnvVars(tJobExec.getEnvVars());
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
        tJobService.endExternalTJobExecution(externalJob.gettJobExecId(), externalJob.getResult());
        runningExternalJobs.remove(externalJob).gettJobExecId();
    }
    
    public ExternalJob isReadyTJobForExternalExecution (Long tJobExecId) {
        ExternalJob externalJob = runningExternalJobs.get(tJobExecId);
        if (externalJob.getTSServices() != null && externalJob.getTSServices().size() > 0) {
            TJobExecution tJobExecution = tJobService.getTJobExecById(externalJob.gettJobExecId());
            if (tJobExecution.getEnvVars() != null && !tJobExecution.getEnvVars().isEmpty()) {
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

    private TJob createElasTestEntitiesForExtJob(ExternalJob externalJob) throws Exception {
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

    /* *************************************************/
    /* *************** ExternalProject *************** */
    /* *************************************************/

    public List<ExternalProject> getAllExternalProjects() {
        return this.externalProjectRepository.findAll();
    }

    public List<ExternalProject> getAllExternalProjectsByType(TypeEnum type) {
        return this.externalProjectRepository.findAllByType(type);
    }

    public ExternalProject getExternalProjectById(ExternalId id) {
        return this.externalProjectRepository.findById(id);
    }

    /* **************************************************/
    /* *************** ExternalTestCase *************** */
    /* **************************************************/

    public List<ExternalTestCase> getAllExternalTestCases() {
        return this.externalTestCaseRepository.findAll();
    }

    public ExternalTestCase getExternalTestCaseById(ExternalId id) {
        return this.externalTestCaseRepository.findById(id);
    }

      public Map<Long, ExternalJob> getRunningExternalJobs() {
        return runningExternalJobs;
    }

    public void setRunningExternalJobs(Map<Long, ExternalJob> runningExternalJobs) {
        this.runningExternalJobs = runningExternalJobs;
    }

}
