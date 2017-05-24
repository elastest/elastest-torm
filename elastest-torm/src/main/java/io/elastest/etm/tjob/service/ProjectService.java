package io.elastest.etm.tjob.service;

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
}
