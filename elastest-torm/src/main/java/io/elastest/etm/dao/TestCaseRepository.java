package io.elastest.etm.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import io.elastest.etm.model.TestCase;
import io.elastest.etm.model.TestSuite;

public interface TestCaseRepository extends JpaRepository<TestCase, Long> {
	public TestCase findByIdAndTestSuite(Long id, TestSuite testSuite);

	public List<TestCase> findByTestSuite(TestSuite testSuite);
}
