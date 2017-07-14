package io.elastest.etm.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import io.elastest.etm.api.model.TestSuite;

public interface TestSuiteRepository extends JpaRepository<TestSuite, Long>{

}
