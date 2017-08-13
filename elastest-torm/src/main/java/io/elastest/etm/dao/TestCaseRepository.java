package io.elastest.etm.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import io.elastest.etm.model.TestCase;

public interface TestCaseRepository extends JpaRepository<TestCase, Long> {

}
