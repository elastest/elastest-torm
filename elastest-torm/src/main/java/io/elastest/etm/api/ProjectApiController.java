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

import io.elastest.etm.model.Project;
import io.elastest.etm.model.Project.BasicAttProject;
import io.elastest.etm.service.ProjectService;
import io.elastest.etm.utils.UtilTools;
import io.swagger.annotations.ApiParam;

@RestController
public class ProjectApiController implements ProjectApi {

	@Autowired
	ProjectService projectService;

	private UtilTools utilTools = new UtilTools();

	public ResponseEntity<Project> createProject(
			@ApiParam(value = "Project object that needs to create", required = true) @Valid @RequestBody Project body) {

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
			@ApiParam(value = "ID of Project to delete.", required = true) @PathVariable("projectId") Long projectId) {

		projectService.deleteProject(projectId);
		return new ResponseEntity<Long>(projectId, HttpStatus.OK);
	}

}
