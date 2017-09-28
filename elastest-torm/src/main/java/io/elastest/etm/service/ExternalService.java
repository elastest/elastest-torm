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
 * @author frdiaz
 *
 */
public class ExternalService {	
	private static final Logger logger = LoggerFactory.getLogger(ExternalService.class);
	
	private ProjectService projectService;
	private TJobService tJobService;
	private UtilTools utilTools;
	
	@Value("${os.name}")
	private String windowsSO;
	
	@Value("${server.port}")
	private String serverPort;
	
	@Value("${et.edm.elasticsearch.api}")
	private String elasticsearchUrl;
	
	@Value("${elastest.torm-gui.port}")
	private String tormGuiPort;
	
	@Value("${logstash.input.http.port}")
	private String logstashInputHttpPort;
	
	@Value("${services.ip}")
	private String servicesIp;
	
	public ExternalService(ProjectService projectService, TJobService tJobService, UtilTools utilTools){
		super();
		this.projectService = projectService;
		this.tJobService = tJobService;
		this.utilTools = utilTools;
	}
	
	public ExternalJob createElasTestEntitiesForExtJob(ExternalJob externalJob) {		
		logger.info("Creating external job entities.");

		try {
			logger.debug("Creating Project.");
			Project project = projectService.getProjectByName(externalJob.getJobName());
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
			TJobExecution tJobExecution = tJobService.executeTJob(tJob.getId(), new ArrayList<>());
			if (windowsSO.toLowerCase().contains("win")) {
				logger.debug("Execute on Windows.");
				String elasTestHostIP = utilTools.getElasTestHostOnWin();
				externalJob.setExecutionUrl(
						"http://" + elasTestHostIP + ":" + tormGuiPort + "/#projects/" + project.getId() + "/tjob/"
								+ tJob.getId() + "/tjob-exec/" + tJobExecution.getId() + "/dashboard");
				externalJob.setLogAnalyzerUrl("http://" + elasTestHostIP + ":" + tormGuiPort + "#/logmanager?indexName="
						+ tJobExecution.getId());
			} else {
				logger.debug("Execute on Linux.");
				externalJob.setExecutionUrl(":" + tormGuiPort + "/#projects/" + project.getId() + "/tjob/"
						+ tJob.getId() + "/tjob-exec/" + tJobExecution.getId() + "/dashboard");
				externalJob.setLogAnalyzerUrl(":" + tormGuiPort + "#/logmanager?indexName=" + tJobExecution.getId());
			}

			externalJob.setServicesIp(servicesIp);
			externalJob.setLogstashPort(logstashInputHttpPort);
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
