package io.elastest.etm.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import io.elastest.etm.model.Project;
import io.elastest.etm.model.SutSpecification;

public interface SutRepository extends JpaRepository<SutSpecification, Long> {

    public List<SutSpecification> findByName(String name);

    public List<SutSpecification> findByNameAndProject(String name,
            Project project);
}
