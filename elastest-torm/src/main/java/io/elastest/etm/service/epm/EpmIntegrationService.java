package io.elastest.etm.service.epm;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import io.elastest.etm.api.model.Log;
import io.elastest.etm.api.model.TJobExecution;
import io.elastest.etm.dao.LogRepository;
import io.elastest.etm.dao.TJobExecRepository;
import io.elastest.etm.docker.DockerExecution;
import io.elastest.etm.docker.DockerService;
import io.elastest.etm.docker.utils.DatabaseSessionManager;
import io.elastest.etm.rabbitmq.service.RabbitmqService;

@Service
public class EpmIntegrationService {
	private final DockerService dockerService;
	private final LogRepository logRepo;

	private final TJobExecRepository tJobExecRepositoryImpl;

	@Autowired
	DatabaseSessionManager dbmanager;

	@Autowired
	private RabbitmqService rabbitmqService;

	public EpmIntegrationService(DockerService dockerService, LogRepository logRepo,
			TJobExecRepository tJobExecRepositoryImpl, DatabaseSessionManager dbmanager) {
		super();
		this.dockerService = dockerService;
		this.logRepo = logRepo;
		this.tJobExecRepositoryImpl = tJobExecRepositoryImpl;
		this.dbmanager = dbmanager;
	}

	@Async
	public void executeTJob(TJobExecution tJobExec) {
		dbmanager.bindSession();

		tJobExec = tJobExecRepositoryImpl.findOne(tJobExec.getId());

		DockerExecution dockerExec = new DockerExecution(tJobExec);
		String testLogUrl = dockerExec.initializeLog();
		Map<String, String> rabbitMap;

		try {
			// Create queues and load basic services
			rabbitMap = rabbitmqService.startRabbitmq(dockerExec.getExecutionId(), dockerExec.isWithSut());
			dockerService.loadBasicServices(dockerExec);

			// Start Test
			dockerService.startTest(tJobExec.getTjob().getImageName(), dockerExec);
			tJobExec.setResult(TJobExecution.ResultEnum.SUCCESS);

			// End and purge services
			dockerService.endAllExec(dockerExec);
			rabbitmqService.purgeRabbitmq(rabbitMap, dockerExec.getExecutionId());
		} catch (Exception e) {
			e.printStackTrace();
			if (!e.getMessage().equals("end error")) { //TODO customize exception
				tJobExec.setResult(TJobExecution.ResultEnum.FAILURE);
			}
		}

		// Setting execution data
		tJobExec.setSutExecution(dockerExec.getSutExec());
		Log testLog = new Log();
		testLog.setLogType(Log.LogTypeEnum.TESTLOG);
		testLog.setLogUrl(testLogUrl);
		testLog.settJobExec(tJobExec);
		logRepo.save(testLog);

		// Saving execution data
		tJobExecRepositoryImpl.save(tJobExec);
		dbmanager.unbindSession();
	}
}
