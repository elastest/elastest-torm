package io.elastest.etm.service.tjob;

import java.util.List;

import javax.xml.ws.http.HTTPException;
import org.springframework.stereotype.Service;

import io.elastest.etm.api.model.TJob;
import io.elastest.etm.api.model.TJobExecution;
import io.elastest.etm.dao.TJobExecRepository;
import io.elastest.etm.dao.TJobRepository;
import io.elastest.etm.service.epm.EpmIntegrationService;

@Service
public class TJobService {

	private final TJobRepository tJobRepo;
	private final TJobExecRepository tJobExecRepositoryImpl;
	private final EpmIntegrationService epmIntegrationService;
	
	public TJobService(TJobRepository tJobRepo, TJobExecRepository tJobExecRepositoryImpl, EpmIntegrationService epmIntegrationService) {
		super();
		this.tJobRepo = tJobRepo;
		this.tJobExecRepositoryImpl = tJobExecRepositoryImpl;
		this.epmIntegrationService = epmIntegrationService;
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

	
	public TJobExecution executeTJob(Long tJobId) {
		TJob tjob = tJobRepo.findOne(tJobId);
		TJobExecution tJobExec = new TJobExecution();
		tJobExec.setTjob(tjob);
		tJobExec = tJobExecRepositoryImpl.save(tJobExec);	
		
		epmIntegrationService.executeTJob(tJobExec);
		return tJobExec;
	}
	

	public void deleteTJobExec(Long tJobExecId) {
		TJobExecution tJobExec = tJobExecRepositoryImpl.findOne(tJobExecId);
		tJobExecRepositoryImpl.delete(tJobExec);
	}

	public TJob getTJobById(Long tJobId) {
		return tJobRepo.findOne(tJobId);
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

	public TJob modifyTJob(TJob tJob) throws Exception {
		if (tJobRepo.findOne(tJob.getId()) != null) {
			return tJobRepo.save(tJob);
		} else {
			throw new HTTPException(405);
		}
	}

}