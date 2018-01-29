package io.elastest.etm.dao.external;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import io.elastest.etm.model.external.ExternalProject;
import io.elastest.etm.model.external.ExternalProject.TypeEnum;

public interface ExternalProjectRepository
        extends JpaRepository<ExternalProject, Long> {

    public ExternalProject findByName(String name);

    public ExternalProject findById(Long id);

    public List<ExternalProject> findAllByType(TypeEnum type);

    public ExternalProject findByExternalIdAndExternalSystemId(
            String externalId, String externalSystemId);
}
