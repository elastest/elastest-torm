package io.elastest.etm.dao.external;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import io.elastest.etm.model.external.ExternalTestCase;

public interface ExternalTestCaseRepository
        extends JpaRepository<ExternalTestCase, Long> {

    public ExternalTestCase findByName(String name);

    public List<ExternalTestCase> findByExTJobs_id(Long exTJobId);

    public ExternalTestCase findByExternalIdAndExternalSystemId(
            String externalId, String externalSystemId);
}
