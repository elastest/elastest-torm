package io.elastest.etm.api;

import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import io.elastest.etm.api.model.ExternalJob;
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
	public ExternalJob executeExternalTJob(
			@ApiParam(value = "ExternalJob object that needs to create", required = true) @Valid @RequestBody ExternalJob body) {
		
		try {
            return externalService.executeExternalTJob(body);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
	}

	@Override	
	public void finishExternalJob(
			@ApiParam(value = "ExternalJob configuration", required = true) @Valid @RequestBody ExternalJob body) {
		
		tJobService.finishExternalTJobExecution(body);
	}
}
