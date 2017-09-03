package io.elastest.etm.service;

import java.util.List;

import javax.xml.ws.http.HTTPException;

import org.springframework.stereotype.Service;

import io.elastest.etm.dao.TJobExecRepository;
import io.elastest.etm.dao.TJobRepository;
import io.elastest.etm.model.Parameter;
import io.elastest.etm.model.TJob;
import io.elastest.etm.model.TJobExecution;
import io.elastest.etm.model.TJobExecution.ResultEnum;

@Service
public class TJobService {

	private final TJobRepository tJobRepo;
	private final TJobExecRepository tJobExecRepositoryImpl;
	private final EpmIntegrationService epmIntegrationService;
	private final EsmService esmService;
	
	public TJobService(TJobRepository tJobRepo, TJobExecRepository tJobExecRepositoryImpl, EpmIntegrationService epmIntegrationService,
			EsmService esmService) {
		super();
		this.tJobRepo = tJobRepo;
		this.tJobExecRepositoryImpl = tJobExecRepositoryImpl;
		this.epmIntegrationService = epmIntegrationService;
		this.esmService = esmService;
	}

	public TJob createTJob(TJob tjob) {		
		return tJobRepo.save(tjob);
	}

	public void deleteTJob(Long tJobId) {
		TJob tJob = tJobRepo.findOne(tJobId);
		tJobRepo.delete(tJob);
	}

	public List<TJob> getAllTJobs() {
		return tJobRepo.findAll();
	}
	
	public TJobExecution executeTJob(Long tJobId, List<Parameter> parameters) {
		TJob tjob = tJobRepo.findOne(tJobId);
		TJobExecution tJobExec = new TJobExecution();
		tJobExec.setTjob(tjob);
		tJobExec.setParameters(parameters);
		tJobExec = tJobExecRepositoryImpl.save(tJobExec);
		
		//After first save, get real Id
		tJobExec.setLogIndex(tJobExec.getId().toString());
		tJobExec = tJobExecRepositoryImpl.save(tJobExec);
		
		if (!tjob.isExternal()){
			epmIntegrationService.executeTJob(tJobExec);
		}
		
		return tJobExec;
	}
	
	public void finishTJobExecution(Long tJobExecId){
		TJobExecution tJobExecution = tJobExecRepositoryImpl.findOne(tJobExecId);
		tJobExecution.setResult(ResultEnum.FINISHED);
		
		tJobExecRepositoryImpl.save(tJobExecution);
	}
	
	public void deleteTJobExec(Long tJobExecId) {
		TJobExecution tJobExec = tJobExecRepositoryImpl.findOne(tJobExecId);
		tJobExecRepositoryImpl.delete(tJobExec);
	}

	public TJob getTJobById(Long tJobId) {
		return tJobRepo.findOne(tJobId);
	}
	
	public TJob getTJobByName(String name){
		return tJobRepo.findByName(name);
	}

	public List<TJobExecution> getTJobsExecutionsByTJobId(Long tJobId) {
		TJob tJob = tJobRepo.findOne(tJobId);
		return getTJobsExecutionsByTJob(tJob);
	}

	public List<TJobExecution> getTJobsExecutionsByTJob(TJob tJob) {
		return tJobExecRepositoryImpl.findByTJob(tJob);
	}

	public TJobExecution getTJobsExecution(Long tJobId, Long tJobExecId) {
		TJob tJob = tJobRepo.findOne(tJobId);
		return tJobExecRepositoryImpl.findByIdAndTJob(tJobExecId, tJob);
	}

	public TJob modifyTJob(TJob tJob) throws RuntimeException {
		if (tJobRepo.findOne(tJob.getId()) != null) {
			return tJobRepo.save(tJob);
		} else {
			throw new HTTPException(405);
		}
	}

}