package io.elastest.etm.tjob.service;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.elastest.etm.api.model.TJobExecution;
import io.elastest.etm.dao.TJobExecRepository;
import io.elastest.etm.dao.TJobRepository;
import io.elastest.etm.docker.DockerExecution;
import io.elastest.etm.model.ElasEtmTjob;
import io.elastest.etm.model.ElasEtmTjobexec;

@Service
public class TJobService {
	@Autowired
	private DockerExecution dockerExec;
	
	@Autowired
	private TJobRepository tJobRepo;
	
	@Autowired
	private TJobExecRepository tJobExecRepo;
	
	public ElasEtmTjob createTJob(ElasEtmTjob tjob){
		return tJobRepo.save(tjob);
	}
	
	public void deleteTJob(Long tJobId){
		ElasEtmTjob tJob=  tJobRepo.findOne(tJobId); 
		tJobRepo.delete(tJob);
	}
	
	public List<ElasEtmTjob> getAllTJobs(){
		return tJobRepo.findAll();		
	}
	
	public ElasEtmTjobexec executeTJob(Long tJobId) {
		ElasEtmTjob tjob = tJobRepo.findOne(tJobId);
		ElasEtmTjobexec tJobExec=  dockerExec.executeTJob(tjob); 
		return tJobExec;
	}
	
	public void deleteTJobExec(Long tJobExecId){
		ElasEtmTjobexec tJobExec=  tJobExecRepo.findOne(tJobExecId); 
		tJobExecRepo.delete(tJobExec);
	}
	
	public ElasEtmTjob getTJobById(Long tJobId){
		return tJobRepo.findOne(tJobId);
	}
	
	public List<ElasEtmTjobexec> getTJobsExecutionsByTJob(Long tJobId){
		ElasEtmTjob tJob = tJobRepo.findOne(tJobId);
		return tJobExecRepo.getByTjobId(tJob);
	}
	
	public ElasEtmTjobexec getTJobsExecution(Long tJobId, Long tJobExecId){
		ElasEtmTjob tJob = tJobRepo.findOne(tJobId);
		return tJobExecRepo.getTjobExec(tJob, tJobExecId);
	}
}