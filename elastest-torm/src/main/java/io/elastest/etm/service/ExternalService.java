package io.elastest.etm.service;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.elastest.etm.api.model.ExternalJob;
import io.elastest.etm.api.model.TestSupportServices;
import io.elastest.etm.model.Project;
import io.elastest.etm.model.TJob;
import io.elastest.etm.model.TJobExecution;
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
    
    public UtilTools utilTools;

    public ExternalService(ProjectService projectService,
            TJobService tJobService, UtilTools utilTools) {
        super();
        this.projectService = projectService;
        this.tJobService = tJobService;
    }

    public ExternalJob executeExternalTJob(ExternalJob externalJob) throws Exception {
        logger.info("Executing TJob from external Job.");
        try {
            logger.debug("Creating TJob data structure.");
            TJob tJob = createElasTestEntitiesForExtJob(externalJob);
            
            logger.debug("Creating TJobExecution.");
            TJobExecution tJobExec = tJobService.executeTJob(tJob.getId(),
                    new ArrayList<>());

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
            externalJob.setTSSEnvVars(tJobExec.getTssEnvVars());
            externalJob.setServicesIp(etPublicHost);
            externalJob
                    .setLogstashPort(etInProd ? etProxyPort : etEtmLsHttpPort);
            externalJob.settJobExecId(tJobExec.getId());

            logger.debug("TJobExecutino URL:" + externalJob.getExecutionUrl());
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Error message: " + e.getMessage());
            throw e;
        }

        return externalJob;
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

}
