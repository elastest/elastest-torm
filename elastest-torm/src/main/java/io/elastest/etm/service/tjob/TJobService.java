package io.elastest.etm.service.tjob;

import java.util.List;

import javax.xml.ws.http.HTTPException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import io.elastest.etm.api.model.Log;
import io.elastest.etm.api.model.TJob;
import io.elastest.etm.api.model.TJobExecution;
import io.elastest.etm.dao.LogRepository;
import io.elastest.etm.dao.TJobExecRepository;
import io.elastest.etm.dao.TJobRepository;
import io.elastest.etm.docker.DockerExecution;
import io.elastest.etm.docker.DockerService;
import io.elastest.etm.docker.utils.DatabaseSessionManager;

@Service
public class TJobService {

	private final DockerService dockerService;
	private final TJobRepository tJobRepo;
	private final TJobExecRepository tJobExecRepositoryImpl;
	private final LogRepository logRepo;
	
	@Autowired
	DatabaseSessionManager dbmanager;

	public TJobService(DockerService dockerService, TJobRepository tJobRepo, TJobExecRepository tJobExecRepositoryImpl,
			LogRepository logRepo) {
		super();
		this.dockerService = dockerService;
		this.tJobRepo = tJobRepo;
		this.tJobExecRepositoryImpl = tJobExecRepositoryImpl;
		this.logRepo = logRepo;
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
		TJobExecution tJobExcution = new TJobExecution();
		tJobExcution.setTjob(tjob);
		tJobExcution = tJobExecRepositoryImpl.save(tJobExcution);		
		return tJobExcution;
	}
	
		
	@Async	
	public TJobExecution executeTJob(TJobExecution tJobExec){
		dbmanager.bindSession();		
		
		tJobExec = tJobExecRepositoryImpl.findOne(tJobExec.getId());		

		DockerExecution dockerExec = new DockerExecution(tJobExec);		
		String testLogUrl = dockerExec.initializeLog();

		try {
			dockerService.loadBasicServices(dockerExec);
		} catch (Exception e) {
			e.printStackTrace();
			throw new HTTPException(500);
		}		

		try {
			dockerService.startTest(tJobExec.getTjob().getImageName(), dockerExec);
			dockerService.endAllExec(dockerExec);
			tJobExec.setResult(TJobExecution.ResultEnum.SUCCESS);
		} catch (Exception e) {
			e.printStackTrace();
			tJobExec.setResult(TJobExecution.ResultEnum.FAILURE);
		}
		
				
		tJobExec.setSutExecution(dockerExec.getSutExec());
		Log testLog = new Log();
		testLog.setLogType(Log.LogTypeEnum.TESTLOG);
		testLog.setLogUrl(testLogUrl);
		testLog.settJobExec(tJobExec);
		logRepo.save(testLog);
				
		TJobExecution tJExecOut = tJobExecRepositoryImpl.save(tJobExec);
		dbmanager.unbindSession();
		return tJExecOut;
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