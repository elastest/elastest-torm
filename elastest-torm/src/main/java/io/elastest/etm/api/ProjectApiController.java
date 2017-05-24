package io.elastest.etm.api;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;

import io.elastest.etm.api.model.Project;
import io.elastest.etm.tjob.service.ProjectService;
import io.swagger.annotations.ApiParam;

@Controller
public class ProjectApiController implements ProjectApi {
	
	@Autowired
	ProjectService projectService;

	@Override
	public ResponseEntity<Project> createProject(@ApiParam(value = "Project object that needs to create" ,required=true )@Valid @RequestBody Project body) {
		// TODO Auto-generated method stub
		return new ResponseEntity<Project>(projectService.createProject(body),HttpStatus.OK);
	}

}
