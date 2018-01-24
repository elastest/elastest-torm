package io.elastest.etm.dao.external;

import org.springframework.data.jpa.repository.JpaRepository;

import io.elastest.etm.model.external.ExternalProject;

public interface ExternalProjectRepository
        extends JpaRepository<ExternalProject, Long> {

    public ExternalProject findByName(String name);
}
