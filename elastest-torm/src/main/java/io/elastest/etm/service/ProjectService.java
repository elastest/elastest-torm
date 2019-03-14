package io.elastest.etm.service;

import java.util.List;

import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.stereotype.Service;

import io.elastest.etm.dao.ProjectRepository;
import io.elastest.etm.model.Project;
import io.elastest.etm.model.Project.ProjectCompleteView;
import io.elastest.etm.model.Project.ProjectMediumView;
import io.elastest.etm.model.Project.ProjectMinimalView;

@Service
public class ProjectService {

    private ProjectRepository projectRepository;
    private AbstractMonitoringService monitoringService;

    public ProjectService(ProjectRepository projectRepository,
            AbstractMonitoringService monitoringService) {
        this.projectRepository = projectRepository;
        this.monitoringService = monitoringService;
    }

    public Project saveProject(Project project) {
        return projectRepository.save(project);
    }

    public Project getProjectById(Long id) {
        return projectRepository.findById(id).get();
    }

    public Project getProjectByName(String name) {
        return projectRepository.findByName(name);
    }

    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    public void deleteProject(Long projectId) {
        Project project = projectRepository.findById(projectId).get();
        monitoringService.deleteMonitoringDataByIndices(
                project.getAllMonitoringIndices());
        projectRepository.delete(project);
    }

    public Class<? extends ProjectMinimalView> getView(String viewType) {
        Class<? extends ProjectMinimalView> view = ProjectCompleteView.class;

        if (viewType != null) {
            if ("minimal".equals(viewType)) {
                view = ProjectMinimalView.class;
            } else if ("medium".equals(viewType)) {
                view = ProjectMediumView.class;
            }
        }
        return view;
    }

    public MappingJacksonValue getMappingJacksonValue(Object obj,
            String viewType) {
        final MappingJacksonValue result = new MappingJacksonValue(obj);
        Class<? extends ProjectMinimalView> view = getView(viewType);

        result.setSerializationView(view);
        return result;
    }
}
