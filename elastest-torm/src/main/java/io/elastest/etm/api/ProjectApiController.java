package io.elastest.etm.api;

import java.util.List;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonView;

import io.elastest.etm.model.Project;
import io.elastest.etm.model.Project.BasicAttProject;
import io.elastest.etm.service.DockerService2;
import io.elastest.etm.service.ProjectService;
import io.swagger.annotations.ApiParam;

@RestController
public class ProjectApiController implements ProjectApi {
	private static final Logger logger = LoggerFactory.getLogger(ProjectApiController.class);

	@Autowired
	ProjectService projectService;

	public ResponseEntity<Project> createProject(
			@ApiParam(value = "Object with the data of the project to be created.", required = true) @Valid @RequestBody Project body) {		

		return new ResponseEntity<Project>(projectService.createProject(body), HttpStatus.OK);
	}

	@JsonView(BasicAttProject.class)
	public ResponseEntity<List<Project>> getAllProjects() {

		return new ResponseEntity<List<Project>>(projectService.getAllProjects(), HttpStatus.OK);
	}

	@JsonView(BasicAttProject.class)
	public ResponseEntity<Project> getProject(@PathVariable("id") Long id) {

		return new ResponseEntity<Project>(projectService.getProjectById(id), HttpStatus.OK);
	}

	@JsonView(BasicAttProject.class)
	public ResponseEntity<Long> deleteProject(
			@ApiParam(value = "ID of Project to delete.", required = true) @PathVariable("id") Long id) {
		projectService.deleteProject(id);
		return new ResponseEntity<Long>(id, HttpStatus.OK);
	}

}
