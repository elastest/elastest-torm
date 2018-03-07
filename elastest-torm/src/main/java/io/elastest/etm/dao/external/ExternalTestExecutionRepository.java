package io.elastest.etm.dao.external;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import io.elastest.etm.model.external.ExternalTJobExecution;
import io.elastest.etm.model.external.ExternalTestCase;
import io.elastest.etm.model.external.ExternalTestExecution;

public interface ExternalTestExecutionRepository extends JpaRepository<ExternalTestExecution, Long> {

	public List<ExternalTestExecution> findByExTestCase(ExternalTestCase exTestCase);

	public ExternalTestExecution findById(Long id);

	public ExternalTestExecution findByExternalIdAndExternalSystemId(String externalId, String externalSystemId);

	public List<ExternalTestExecution> findByExTJobExec(ExternalTJobExecution exTJobExec);
}
