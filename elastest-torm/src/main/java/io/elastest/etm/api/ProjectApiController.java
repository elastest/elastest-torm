package io.elastest.etm.api;

import java.util.List;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonView;

import io.elastest.etm.config.EtSampleDataLoader;
import io.elastest.etm.model.Project;
import io.elastest.etm.model.Project.ProjectCompleteView;
import io.elastest.etm.service.ProjectService;
import io.swagger.annotations.ApiParam;

@RestController
public class ProjectApiController implements ProjectApi {
    private static final Logger logger = LoggerFactory
            .getLogger(ProjectApiController.class);

    @Autowired
    ProjectService projectService;

    @Autowired
    EtSampleDataLoader etSampleDataLoader;

    public ResponseEntity<Project> createProject(
            @ApiParam(value = "Object with the data of the project to be created.", required = true) @Valid @RequestBody Project body) {

        return new ResponseEntity<Project>(projectService.saveProject(body),
                HttpStatus.OK);
    }

    public MappingJacksonValue getAllProjects(
            @RequestParam(value = "viewType", required = false) String viewType) {
        List<Project> projects = projectService.getAllProjects();

        return projectService.getMappingJacksonValue(projects, viewType);
    }

    public MappingJacksonValue getProject(@PathVariable("id") Long id,
            @RequestParam(value = "viewType", required = false) String viewType) {
        Project pj = projectService.getProjectById(id);
        return projectService.getMappingJacksonValue(pj, viewType);

    }

    @JsonView(ProjectCompleteView.class)
    public ResponseEntity<Long> deleteProject(
            @ApiParam(value = "ID of Project to delete.", required = true) @PathVariable("id") Long id) {
        projectService.deleteProject(id);
        return new ResponseEntity<Long>(id, HttpStatus.OK);
    }

    @JsonView(ProjectCompleteView.class)
    public ResponseEntity<Boolean> restoreDemoProjects() {
        Boolean createdOrUpdated = etSampleDataLoader.createData(true);
        return new ResponseEntity<Boolean>(createdOrUpdated, HttpStatus.OK);
    }
}
