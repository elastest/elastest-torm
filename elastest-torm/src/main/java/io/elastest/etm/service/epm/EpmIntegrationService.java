package io.elastest.etm.service.epm;

import javax.xml.ws.http.HTTPException;

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

@Service
public class EpmIntegrationService {
	private final DockerService dockerService;
	private final LogRepository logRepo;
	
	private final TJobExecRepository tJobExecRepositoryImpl;
	
	@Autowired
	DatabaseSessionManager dbmanager;

	public EpmIntegrationService(DockerService dockerService, LogRepository logRepo,
			TJobExecRepository tJobExecRepositoryImpl, DatabaseSessionManager dbmanager) {
		super();
		this.dockerService = dockerService;
		this.logRepo = logRepo;
		this.tJobExecRepositoryImpl = tJobExecRepositoryImpl;
		this.dbmanager = dbmanager;
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
}
