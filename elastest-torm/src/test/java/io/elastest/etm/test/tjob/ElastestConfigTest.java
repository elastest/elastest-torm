package io.elastest.etm.test.tjob;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.elastest.etm.model.Project;
import io.elastest.etm.model.TJob;

@Configuration
public class ElastestConfigTest {
	
	@Bean
	TJob tJob(){
		TJob tJob = new TJob();
		tJob.setId(0L);
		tJob.setImageName("elastest/test-etm-test1");
		tJob.setName("SimpleTest");
		tJob.setResultsPath("/app1TestJobsJenkins/target/surefire-reports/TEST-es.tfcfrd.app1TestJobsJenkins.AppTest.xml");
		Project project = new Project();
		project.setId(1L);
		project.setName("TestProject1");		
		tJob.setProject(project);
		return tJob;
	}
	
	
//	@Bean
//	Project project(){
//		Project project = new Project();
//		project.setName("TestProject1");
//		return project;
//	}
//	
//	@Bean
//	TJobService tJobService(){
//		TJobService tJobService = new TJobService();
//		return tJobService;
//	}
	
//	@Bean
//	DockerExecution dockerExecution(){
//		DockerExecution dockerExecution = new DockerExecution();
//		return dockerExecution;
//	}
//	
//	@Bean
//	StompMessageSenderService stompMessageSenderService(){
//		StompMessageSenderService stompMessageSenderService = new StompMessageSenderService();
//		return stompMessageSenderService;
//	}	
}
