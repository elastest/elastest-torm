package io.elastest.etm.service;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.elastest.etm.model.ExternalJob;
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

    public ExternalService(ProjectService projectService,
            TJobService tJobService, UtilTools utilTools) {
        super();
        this.projectService = projectService;
        this.tJobService = tJobService;
    }

    public ExternalJob createElasTestEntitiesForExtJob(
            ExternalJob externalJob) {
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

            logger.info("Creating TJobExecution.");
            TJobExecution tJobExecution = tJobService.executeTJob(tJob.getId(),
                    new ArrayList<>());
            
            externalJob.setExecutionUrl((etInProd ? "http://" + etPublicHost + ":" + etProxyPort :
                     "http://localhost" + ":" + etEtmDevGuiPort) + "/#/projects/" + project.getId() + "/tjob/"
                    + tJob.getId() + "/tjob-exec/" + tJobExecution.getId()
                    + "/dashboard");
            externalJob.setLogAnalyzerUrl((etInProd ? "http://" + etPublicHost + ":" + etProxyPort :
                "http://localhost" + ":" + etEtmDevGuiPort) + "/#/logmanager?indexName="
                    + tJobExecution.getId());

            externalJob.setServicesIp(etPublicHost);
            externalJob.setLogstashPort(etInProd ? etProxyPort : etEtmLsHttpPort);
            externalJob.settJobExecId(tJobExecution.getId());

            logger.debug("TJobExecutino URL:" + externalJob.getExecutionUrl());
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Error message: " + e.getMessage());
            throw e;
        }

        return externalJob;
    }

}
