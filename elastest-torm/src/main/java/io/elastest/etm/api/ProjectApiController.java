package io.elastest.etm.api;

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;

import io.elastest.etm.api.model.Project;
import io.elastest.etm.service.project.ProjectService;
import io.swagger.annotations.ApiParam;

@Controller
public class ProjectApiController implements ProjectApi {
	
	@Autowired
	ProjectService projectService;

	@Override
	public ResponseEntity<Project> createProject(@ApiParam(value = "Project object that needs to create" ,required=true )@Valid @RequestBody Project body) {

		try{
			return new ResponseEntity<Project>(projectService.createProject(body),HttpStatus.OK);
		}catch(Exception e){
			return new ResponseEntity<Project>(body, HttpStatus.CONFLICT);			
		}		
	}

	@Override
	public ResponseEntity<List<Project>> getAllProjects() {		
		try{
			return new ResponseEntity<List<Project>>(projectService.getAllProjects(),HttpStatus.OK);
		}catch(Exception e){
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);			
		}		
	}

	@Override
	public ResponseEntity<Project> getProject(Long id) {
		try{
			return new ResponseEntity<Project>(projectService.getProjectById(id),HttpStatus.OK);
		}catch(Exception e){
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);			
		}		
	}

}
