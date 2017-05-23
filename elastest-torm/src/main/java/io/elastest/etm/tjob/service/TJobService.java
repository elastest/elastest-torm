package io.elastest.etm.tjob.service;
import org.springframework.stereotype.Service;

import io.elastest.etm.docker.DockerExecution;
import io.elastest.etm.model.ElasEtmTjob;
import io.elastest.etm.model.ElasEtmTjobexec;

@Service
public class TJobService {
	private DockerExecution dockerExec;
	
	public ElasEtmTjobexec executeTJob(Long tJobId) {
//		ElasEtmTjob tJob = 
		//tJob = Get tJob from database
		/*TJobExecution tJobExec= */ dockerExec.executeTJob(/*tJob.getImage()*/); 
		return new ElasEtmTjobexec(); //Return tJobExec.getId()
	}
}