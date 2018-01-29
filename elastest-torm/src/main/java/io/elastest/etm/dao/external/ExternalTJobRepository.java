package io.elastest.etm.dao.external;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import io.elastest.etm.model.external.ExternalProject;
import io.elastest.etm.model.external.ExternalTJob;

public interface ExternalTJobRepository
        extends JpaRepository<ExternalTJob, Long> {

    public ExternalTJob findByName(String name);

    public List<ExternalTJob> findByExProject(ExternalProject exProject);

    public ExternalTJob findById(Long id);

    public ExternalTJob findByExternalIdAndExternalSystemId(
            String externalId, String externalSystemId);
}
