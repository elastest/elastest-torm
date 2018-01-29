package io.elastest.etm.dao.external;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import io.elastest.etm.model.external.ExternalTJob;
import io.elastest.etm.model.external.ExternalTJobExecution;

public interface ExternalTJobExecutionRepository
        extends JpaRepository<ExternalTJobExecution, Long> {

    public List<ExternalTJobExecution> findByExTJob(ExternalTJob exTJob);

    public ExternalTJobExecution findById(Long id);
}
