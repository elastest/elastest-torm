package io.elastest.etm.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import io.elastest.etm.model.TJobExecution;
import io.elastest.etm.model.TestSuite;

public interface TestSuiteRepository extends JpaRepository<TestSuite, Long> {
	public TestSuite findByIdAndTJobExec(Long id, TJobExecution tJobExec);

	public List<TestSuite> findByTJobExec(TJobExecution tJobExec);
}
