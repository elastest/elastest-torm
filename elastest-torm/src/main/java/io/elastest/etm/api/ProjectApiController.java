package io.elastest.etm.api;

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonView;

import io.elastest.etm.api.model.Project;
import io.elastest.etm.api.model.Project.BasicAttProject;
import io.elastest.etm.service.project.ProjectService;
import io.swagger.annotations.ApiParam;

@RestController
public class ProjectApiController implements ProjectApi {
	
	@Autowired
	ProjectService projectService;

	@CrossOrigin(origins = {"http://localhost:4200"})		
	public ResponseEntity<Project> createProject(@ApiParam(value = "Project object that needs to create" ,required=true )@Valid @RequestBody Project body) {

		try{
			return new ResponseEntity<Project>(projectService.createProject(body),HttpStatus.OK);
		}catch(Exception e){
			return new ResponseEntity<Project>(body, HttpStatus.CONFLICT);			
		}		
	}

	
	@JsonView(BasicAttProject.class)
	@CrossOrigin(origins = {"http://localhost:4200"})
	public ResponseEntity<List<Project>> getAllProjects() {		
		try{
			return new ResponseEntity<List<Project>>(projectService.getAllProjects(),HttpStatus.OK);
		}catch(Exception e){
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);			
		}		
	}

	
	@CrossOrigin(origins = {"http://localhost:4200"})
	@JsonView(BasicAttProject.class)
	public ResponseEntity<Project> getProject(@PathVariable("id") Long id) {
		try{
			System.out.println("INPUT ID:"+ id);
			return new ResponseEntity<Project>(projectService.getProjectById(id),HttpStatus.OK);
		}catch(Exception e){
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);			
		}		
	}

}
