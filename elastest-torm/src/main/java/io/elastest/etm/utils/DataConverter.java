package io.elastest.etm.utils;

import java.util.List;

import javax.xml.ws.http.HTTPException;

import io.elastest.etm.api.model.TJob;
import io.elastest.etm.api.model.TJobExecution;
import io.elastest.etm.api.model.TestService;
import io.elastest.etm.model.ElasEtmTjob;
import io.elastest.etm.model.ElasEtmTjobexec;

public class DataConverter {
	
	// TJob <-> ElasetmTjob
	public TJob etmTjobToApiTJob(ElasEtmTjob etmTjob) {
		Long id = etmTjob.getElasEtmTjobId();
		String name = etmTjob.getElasEtmTjobName();
		List<TestService> testServices = null; // etmTjob.getElasEtmTjobTserv();
		String imageName = etmTjob.getElasEtmTjobImname();
		Integer sut = etmTjob.getElasEtmTjobSut();
		
		TJob tjob = new TJob(id, name, testServices, imageName, sut);
		return tjob;
	}
	
	public ElasEtmTjob apiTjobToEtmTJob(TJob tjob) {
		Long id = tjob.getId() != null?tjob.getId():0;
		String name = tjob.getName();
		List<TestService> testServices = tjob.getTestServices();
		String imageName = tjob.getImageName();
		Integer sut = tjob.getSut();
		
		ElasEtmTjob etmTjob = new ElasEtmTjob();
		etmTjob.setElasEtmTjobId(id);
		etmTjob.setElasEtmTjobName(name);
//		etmTjob.setElasEtmTjobTserv(testServices);
		etmTjob.setElasEtmTjobImname(imageName);
		etmTjob.setElasEtmTjobSut(sut);
		
		return etmTjob;
	}
	
	// TJobExecution <-> ElasetmTjobexec
	public TJobExecution etmTjobexecToApiTJobExec(ElasEtmTjobexec etmTjobExec) {
		Long id = etmTjobExec.getElasEtmTjobexecId();
		Long duration = etmTjobExec.getElasEtmTjobexecDuration();
		
		TJobExecution tjobExec = new TJobExecution(id, duration);
		return tjobExec;
	}
	
	public ElasEtmTjobexec apiTjobToEtmTJob(TJobExecution tjobexec) {
		Long id = tjobexec.getId();
		Long duration = tjobexec.getDuration();
		
		ElasEtmTjobexec etmTjobExec = new ElasEtmTjobexec();
		etmTjobExec.setElasEtmTjobexecId(id);
		etmTjobExec.setElasEtmTjobexecDuration(duration);
		
		return etmTjobExec;
	}

	public int getHttpExceptionCode(Exception e){
		if(e instanceof HTTPException){
			return ((HTTPException) e).getStatusCode();
		}
		return -5;
	}
	


}
