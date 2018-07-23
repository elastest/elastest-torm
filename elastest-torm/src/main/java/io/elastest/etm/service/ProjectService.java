package io.elastest.etm.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.elastest.etm.dao.ProjectRepository;
import io.elastest.etm.model.Project;

@Service
public class ProjectService {
	
	@Autowired
	ProjectRepository projectRepository;
	
	public Project createProject(Project project){
		
		return projectRepository.save(project);
	}
	
	public Project getProjectById(Long id){
		return projectRepository.findById(id).get();
	}
	
	public Project getProjectByName(String name){
		return projectRepository.findByName(name);
	}
	
	public List<Project> getAllProjects(){
		return projectRepository.findAll();
	}
	
	public void deleteProject(Long projectId) {
		Project project = projectRepository.findById(projectId).get();
		projectRepository.delete(project);
	}
}
