package io.elastest.etm.api;

import javax.validation.Valid;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import io.elastest.etm.model.ExternalJob;
import io.elastest.etm.service.ExternalService;
import io.elastest.etm.service.TJobService;
import io.swagger.annotations.ApiParam;

@RestController
public class ExternalApiController implements ExternalApi {

	private TJobService tJobService;
	private ExternalService externalService;

	public ExternalApiController(TJobService tJobService, ExternalService externalService) {
		this.tJobService = tJobService;
		this.externalService = externalService;
	}

	@Override	
	public ExternalJob createExternalTJob(
			@ApiParam(value = "ExternalJob object that needs to create", required = true) @Valid @RequestBody ExternalJob body) {
		
		return externalService.createElasTestEntitiesForExtJob(body);
	}

	@Override	
	public void finishExternalJob(
			@ApiParam(value = "ExternalJob configuration", required = true) @Valid @RequestBody ExternalJob body) {
		
		tJobService.finishTJobExecution(body.gettJobExecId());
	}
}
