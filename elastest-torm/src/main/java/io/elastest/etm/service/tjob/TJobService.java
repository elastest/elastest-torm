package io.elastest.etm.service.tjob;

import java.util.List;

import javax.xml.ws.http.HTTPException;
import org.springframework.stereotype.Service;

import io.elastest.etm.api.model.Log;
import io.elastest.etm.api.model.SutExecution;
import io.elastest.etm.api.model.TJob;
import io.elastest.etm.api.model.TJobExecution;
import io.elastest.etm.dao.LogRepository;
import io.elastest.etm.dao.TJobExecRepository;
import io.elastest.etm.dao.TJobRepository;
import io.elastest.etm.docker.DockerExecution;
import io.elastest.etm.docker.DockerService;
import io.elastest.etm.service.sut.SutService;

@Service
public class TJobService {

	private final DockerService dockerService;
	private final TJobRepository tJobRepo;
	private final TJobExecRepository tJobExecRepo;
	private final LogRepository logRepo;
	private final SutService sutService;

	public TJobService(DockerService dockerService, TJobRepository tJobRepo, TJobExecRepository tJobExecRepo,
			LogRepository logRepo, SutService sutService) {
		super();
		this.dockerService = dockerService;
		this.tJobRepo = tJobRepo;
		this.tJobExecRepo = tJobExecRepo;
		this.logRepo = logRepo;
		this.sutService = sutService;
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

	public TJobExecution executeTJob(Long tJobId) {//TODO Refactor
		TJob tjob = tJobRepo.findOne(tJobId);

		if (tjob.getSut() == null) {
			return executeTJobWithoutSut(tJobId);
		} else {
			TJobExecution tjobExec = tJobExecRepo.save(new TJobExecution());
			tjobExec.setTjob(tjob);

			SutExecution sutExec = sutService.createSutExecutionBySut(tjob.getSut());

			DockerExecution dockerExec = new DockerExecution();
			dockerExec.settJobexec(tjobExec);
			String testLogUrl = dockerExec.initializeLog();
			
			try {
				dockerService.loadBasicServices(dockerExec);
			} catch (Exception e) {
				e.printStackTrace();
				throw new HTTPException(500);
			}
			
			try {
				sutExec = dockerService.startSut(sutExec,dockerExec);
				dockerService.startTest(tjob.getImageName(),dockerExec);
				sutExec.deployStatus(SutExecution.DeployStatusEnum.UNDEPLOYING);
				dockerService.endAllExec(dockerExec);
				sutExec.deployStatus(SutExecution.DeployStatusEnum.UNDEPLOYED);
				tjobExec.setResult(TJobExecution.ResultEnum.SUCCESS);

			} catch (Exception e) {
				e.printStackTrace();
				tjobExec.setResult(TJobExecution.ResultEnum.FAILURE);
			}

			sutService.modifySutExec(sutExec);
			tjobExec.setSutExecution(sutExec);

			Log testLog = new Log();
			testLog.setLogType(Log.LogTypeEnum.TESTLOG);
			testLog.setLogUrl(testLogUrl);
			testLog.settJobExec(tjobExec);
			logRepo.save(testLog);

			return tJobExecRepo.save(tjobExec);
		}
	}

	public TJobExecution executeTJobWithoutSut(Long tJobId) {//TODO Refactor
		TJob tjob = tJobRepo.findOne(tJobId);
		TJobExecution tjobExec = tJobExecRepo.save(new TJobExecution());
		tjobExec.setTjob(tjob);

		DockerExecution dockerExec = new DockerExecution();
		dockerExec.settJobexec(tjobExec);
		String testLogUrl = dockerExec.initializeLog();
		
		try {
			dockerService.loadBasicServices(dockerExec);
		} catch (Exception e) {
			e.printStackTrace();
			throw new HTTPException(500);
		}

		try {
			dockerService.startTest(tjob.getImageName(),dockerExec);
			dockerService.endExec(dockerExec);
			tjobExec.setResult(TJobExecution.ResultEnum.SUCCESS);

		} catch (Exception e) {
			e.printStackTrace();
			tjobExec.setResult(TJobExecution.ResultEnum.FAILURE);
		}

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