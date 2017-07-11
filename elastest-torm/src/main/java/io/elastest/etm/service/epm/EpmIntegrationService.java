package io.elastest.etm.service.epm;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
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
	
	@Value ("${elastest.elasticsearch.host}")
	private String elasticsearchHost;
	private final DockerService dockerService;
	private final LogRepository logRepo;

	private final TJobExecRepository tJobExecRepositoryImpl;

	private DatabaseSessionManager dbmanager;

	private RabbitmqService rabbitmqService;

	public EpmIntegrationService(DockerService dockerService, LogRepository logRepo,
			TJobExecRepository tJobExecRepositoryImpl, DatabaseSessionManager dbmanager,
			RabbitmqService rabbitmqService) {
		super();
		this.dockerService = dockerService;
		this.logRepo = logRepo;
		this.tJobExecRepositoryImpl = tJobExecRepositoryImpl;
		this.dbmanager = dbmanager;
		this.rabbitmqService = rabbitmqService;
	}

	@Async
	public void executeTJob(TJobExecution tJobExec) {

		dbmanager.bindSession();

		tJobExec = tJobExecRepositoryImpl.findOne(tJobExec.getId());
		
//		Log testLog = new Log();
//		testLog.setLogType(Log.LogTypeEnum.TESTLOG);
//		testLog.setLogUrl(elasticsearchHost + tJobExec.getId());
//		testLog.settJobExec(tJobExec);
//		logRepo.save(testLog);

		DockerExecution dockerExec = new DockerExecution(tJobExec);
		String testLogUrl = dockerExec.initializeLog();		

		try {
			// Create queues and load basic services			
			dockerService.loadBasicServices(dockerExec);

			// Start Test
			dockerService.startTest(tJobExec.getTjob().getImageName(), dockerExec);
			tJobExec.setResult(TJobExecution.ResultEnum.SUCCESS);

			// End and purge services
			dockerService.endAllExec(dockerExec);			
		} catch (Exception e) {
			e.printStackTrace();
			if (!e.getMessage().equals("end error")) { // TODO customize
														// exception
				tJobExec.setResult(TJobExecution.ResultEnum.FAILURE);
			}
		}

		// Setting execution data
		tJobExec.setSutExecution(dockerExec.getSutExec());

		// Saving execution data
		tJobExecRepositoryImpl.save(tJobExec);
		dbmanager.unbindSession();
	}

}
