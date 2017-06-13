package io.elastest.etm.test.tjob;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.elastest.etm.api.model.Project;
import io.elastest.etm.api.model.TJob;

@Configuration
public class ElastestConfigTest {
	
	@Bean
	TJob tJob(){
		TJob tJob = new TJob();
		tJob.setId(0L);
		tJob.setImageName("edujgurjc/torm-test-01");
		tJob.setName("SimpleTest");
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
