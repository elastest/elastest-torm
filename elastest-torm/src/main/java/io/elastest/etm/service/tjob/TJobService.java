package io.elastest.etm.service.tjob;

import java.util.List;

import javax.xml.ws.http.HTTPException;
import org.springframework.stereotype.Service;

import io.elastest.etm.api.model.Log;
import io.elastest.etm.api.model.TJob;
import io.elastest.etm.api.model.TJobExecution;
import io.elastest.etm.dao.LogRepository;
import io.elastest.etm.dao.TJobExecRepository;
import io.elastest.etm.dao.TJobRepository;
import io.elastest.etm.docker.DockerExecution;
import io.elastest.etm.docker.DockerService;

@Service
public class TJobService {

	private final DockerService dockerService;
	private final TJobRepository tJobRepo;
	private final TJobExecRepository tJobExecRepo;
	private final LogRepository logRepo;

	public TJobService(DockerService dockerService, TJobRepository tJobRepo, TJobExecRepository tJobExecRepo,
			LogRepository logRepo) {
		super();
		this.dockerService = dockerService;
		this.tJobRepo = tJobRepo;
		this.tJobExecRepo = tJobExecRepo;
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
		TJobExecution tjobExec = tJobExecRepo.save(new TJobExecution());
		
		Runnable r1 = () -> { executeTJob(tjobExec, tjob);};
		new Thread(r1).start();
		
		return tjobExec;
	}
	
	
	private TJobExecution executeTJob(TJobExecution tjobExec, TJob tjob){
		tjobExec.setTjob(tjob);

		DockerExecution dockerExec = new DockerExecution(tjobExec);		
		String testLogUrl = dockerExec.initializeLog();

		try {
			dockerService.loadBasicServices(dockerExec);
		} catch (Exception e) {
			e.printStackTrace();
			throw new HTTPException(500);
		}

		try {
			dockerService.startTest(tjob.getImageName(), dockerExec);
			dockerService.endAllExec(dockerExec);
			tjobExec.setResult(TJobExecution.ResultEnum.SUCCESS);
		} catch (Exception e) {
			e.printStackTrace();
			tjobExec.setResult(TJobExecution.ResultEnum.FAILURE);
		}
		
		tjobExec.setSutExecution(dockerExec.getSutExec());
		Log testLog = new Log();
		testLog.setLogType(Log.LogTypeEnum.TESTLOG);
		testLog.setLogUrl(testLogUrl);
		testLog.settJobExec(tjobExec);
		logRepo.save(testLog);
		
		return tJobExecRepo.save(tjobExec);
	}

	public void deleteTJobExec(Long tJobExecId) {
		TJobExecution tJobExec = tJobExecRepo.findOne(tJobExecId);
		tJobExecRepo.delete(tJobExec);
	}

	public TJob getTJobById(Long tJobId) {
		return tJobRepo.findOne(tJobId);
	}

	public List<TJobExecution> getTJobsExecutionsByTJobId(Long tJobId) {
		TJob tJob = tJobRepo.findOne(tJobId);
		return getTJobsExecutionsByTJob(tJob);
	}

	public List<TJobExecution> getTJobsExecutionsByTJob(TJob tJob) {
		return tJobExecRepo.findByTJob(tJob);
	}

	public TJobExecution getTJobsExecution(Long tJobId, Long tJobExecId) {
		TJob tJob = tJobRepo.findOne(tJobId);
		return tJobExecRepo.findByIdAndTJob(tJobExecId, tJob);
	}

	public TJob modifyTJob(TJob tJob) throws Exception {
		if (tJobRepo.findOne(tJob.getId()) != null) {
			return tJobRepo.save(tJob);
		} else {
			throw new HTTPException(405);
		}
	}

}