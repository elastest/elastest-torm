package io.elastest.etm.dao.external;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import io.elastest.etm.model.external.ExternalProject;
import io.elastest.etm.model.external.ExternalProject.TypeEnum;

public interface ExternalProjectRepository
        extends JpaRepository<ExternalProject, Long> {

    public ExternalProject findByName(String name);

    public List<ExternalProject> findAllByType(TypeEnum type);

    public ExternalProject findByExternalIdAndExternalSystemId(
            String externalId, String externalSystemId);

    @Transactional
    public void deleteByExternalSystemId(String externalSystemId);
}
