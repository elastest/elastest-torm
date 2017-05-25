package io.elastest.etm.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import io.elastest.etm.api.model.TJob;
import io.elastest.etm.api.model.TJobExecution;

public interface TJobExecRepository extends JpaRepository<TJobExecution, Long> {	 
	public List<TJobExecution> findByTJob(TJob tJob); 
	public TJobExecution findByIdAndTJob(Long tJobExecId, TJob tJob); 
}
