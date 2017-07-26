package io.elastest.etm.api.ext;

import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import io.elastest.etm.api.ext.model.ExternalJob;
import io.elastest.etm.service.ext.ExternalService;
import io.elastest.etm.service.project.ProjectService;
import io.elastest.etm.service.tjob.TJobService;
import io.swagger.annotations.ApiParam;

@RestController
public class ExternalApiController implements ExternalApi {
	
	private ProjectService projectService;
	private TJobService tJobService;
	private ExternalService externalService;
	
	public ExternalApiController(ProjectService projectService, TJobService tJobService, ExternalService externalService){
		this.projectService = projectService;
		this.tJobService = tJobService;
		this.externalService = externalService;
	}

	@Override	
	@CrossOrigin(origins = {"*"})
	public ResponseEntity<ExternalJob> createExternalTJob(
			@ApiParam(value = "ExternalJob object that needs to create", required = true) @Valid @RequestBody ExternalJob body) {		
		try{
			return new ResponseEntity<ExternalJob>(externalService.createElasTestEntitiesForExtJob(body),HttpStatus.OK);
		}catch(Exception e){
			return new ResponseEntity<ExternalJob>(body, HttpStatus.CONFLICT);			
		}		
	}
}
