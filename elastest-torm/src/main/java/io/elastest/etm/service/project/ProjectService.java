package io.elastest.etm.service.project;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.elastest.etm.api.model.Project;
import io.elastest.etm.dao.ProjectRepository;

@Service
public class ProjectService {
	
	@Autowired
	ProjectRepository projectRepository;
	
	public Project createProject(Project project){
		
		return projectRepository.save(project);
	}
	
	public Project getProjectById(Long id){
		return projectRepository.findOne(id);
	}
	
	public List<Project> getAllProjects(){
		return projectRepository.findAll();
	}
	
	public void deleteProject(Long projectId) {
		Project project = projectRepository.findOne(projectId);
		projectRepository.delete(project);
	}
}
