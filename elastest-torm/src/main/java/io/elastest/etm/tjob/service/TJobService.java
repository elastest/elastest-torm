package io.elastest.etm.tjob.service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.elastest.etm.dao.TJobExecRepository;
import io.elastest.etm.dao.TJobRepository;
import io.elastest.etm.docker.DockerExecution;
import io.elastest.etm.model.ElasEtmTjob;
import io.elastest.etm.model.ElasEtmTjobexec;

@Service
public class TJobService {
	private DockerExecution dockerExec;
	
	@Autowired
	private TJobRepository tJobRepo;
	
	@Autowired
	private TJobExecRepository tJobExecRepo;
	
	public ElasEtmTjob createTJob(ElasEtmTjob tjob){
		return tJobRepo.save(tjob);
	}
	
	public ElasEtmTjobexec executeTJob(Long tJobId) {
		ElasEtmTjob tjob = tJobRepo.findOne(tJobId);
		ElasEtmTjobexec tJobExec=  dockerExec.executeTJob(tjob.getElasEtmTjobImname()); 
		return tJobExec;
	}
}