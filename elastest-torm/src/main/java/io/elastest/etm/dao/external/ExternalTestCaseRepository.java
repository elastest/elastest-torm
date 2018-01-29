package io.elastest.etm.dao.external;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import io.elastest.etm.model.external.ExternalTJob;
import io.elastest.etm.model.external.ExternalTestCase;

public interface ExternalTestCaseRepository
        extends JpaRepository<ExternalTestCase, Long> {

    public ExternalTestCase findByName(String name);

    public List<ExternalTestCase> findByExTJob(ExternalTJob exTJob);

    public ExternalTestCase findById(Long id);
    
    public ExternalTestCase findByExternalIdAndExternalSystemId(
            String externalId, String externalSystemId);
}
