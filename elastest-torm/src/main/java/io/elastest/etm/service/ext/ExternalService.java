package io.elastest.etm.service.ext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.elastest.etm.api.ext.model.ExternalJob;
import io.elastest.etm.api.model.Project;
import io.elastest.etm.api.model.TJob;
import io.elastest.etm.api.model.TJobExecution;
import io.elastest.etm.docker.DockerService;
import io.elastest.etm.service.project.ProjectService;
import io.elastest.etm.service.tjob.TJobService;
import io.elastest.etm.utils.UtilTools;

@Service
public class ExternalService {
	
	private static final Logger logger = LoggerFactory.getLogger(ExternalService.class);
	
	private ProjectService projectService;
	private TJobService tJobService;
	private UtilTools utilTools;
	
	@Value("${os.name}")
	private String windowsSO;
	
	@Value("${docker.host.port}")
	private String dockerHostPort;
	
	public ExternalService(ProjectService projectService, TJobService tJobService, UtilTools utilTools){
		super();
		this.projectService = projectService;
		this.tJobService = tJobService;
		this.utilTools = utilTools;
	}
	
	public ExternalJob createElasTestEntitiesForExtJob(ExternalJob externalJob){
		
		logger.info("Creating external job entities.");

		try{
		//Create ElasTest Project
		logger.info("Creating Project.");
		Project project = new Project();
		project.setId(0L);
		project.setName(externalJob.getJobName());
		project = projectService.createProject(project);
		
		//Create ElasTest TJob
		logger.info("Creating TJob.");
		TJob tJob = new TJob();
		tJob.setName(externalJob.getJobName());
		tJob = tJobService.createTJob(tJob);
		tJob.setExternal(true);
		
		//Run ElasTest TJob
		logger.info("Creating TJobExecution.");
		TJobExecution tJobExecution = tJobService.executeTJob(tJob.getId());
		
		String eslasTestHostIP = null;
		if (windowsSO.toLowerCase().contains("win")) {
			logger.info("Execute on Windows.");
			eslasTestHostIP = utilTools.getDockerHostIpOnWin();
		}else{
			logger.info("Execute on Linux.");
			eslasTestHostIP = utilTools.getHostIp();
		}
		//http://localhost:4200/#/projects/2/tjob/8/tjob-exec/18/dashboard
		externalJob.settJobExecId(tJobExecution.getId());
		externalJob.setExecutionUrl("http://" + eslasTestHostIP + ":" + dockerHostPort + "/#projects/" + project.getId() + "/tjob/" + tJob.getId() + "/tjob-exec/" + tJobExecution.getId() + "/dashboard");
		logger.info("TJobExecutino URL:" + externalJob.getExecutionUrl());
		}catch (Exception e){
			e.printStackTrace();
			logger.info("Error message: "+ e.getMessage());
		}
		return externalJob;
		
	}

}
