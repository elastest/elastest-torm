package io.elastest.etm.api.ext;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonView;

import io.elastest.etm.api.model.Project;
import io.elastest.etm.api.model.TJob;
import io.elastest.etm.api.model.Project.BasicAttProject;
import io.elastest.etm.service.project.ProjectService;
import io.elastest.etm.service.tjob.TJobService;

@RestController
public class ExternalApiController implements ExternalApi {
	
	private ProjectService projectService;
	private TJobService tJobService;
	
	public ExternalApiController(ProjectService projectService, TJobService tJobService){
		this.projectService = projectService;
		this.tJobService = tJobService;
	}

//	@Override	
//	@CrossOrigin(origins = {"*"})
//	public ResponseEntity<TJob> createExternalTJob(TJob body) {
//		try{
//			return new ResponseEntity<Project>(projectService.createProject(body),HttpStatus.OK);
//		}catch(Exception e){
//			return new ResponseEntity<Project>(body, HttpStatus.CONFLICT);			
//		}		
//	}
}
