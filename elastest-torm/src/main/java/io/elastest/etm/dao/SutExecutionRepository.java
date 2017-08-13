package io.elastest.etm.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import io.elastest.etm.model.SutExecution;
import io.elastest.etm.model.SutSpecification;

public interface SutExecutionRepository extends JpaRepository<SutExecution, Long> {
	
	public List<SutExecution> findBySutSpecification(SutSpecification sutSpecification);
	
	public SutExecution findByIdAndSutSpecification(Long Id, SutSpecification sutSpecification);

}
