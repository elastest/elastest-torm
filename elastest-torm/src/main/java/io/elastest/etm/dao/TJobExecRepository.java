package io.elastest.etm.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import io.elastest.etm.model.TJob;
import io.elastest.etm.model.TJobExecution;

@Repository
public interface TJobExecRepository extends JpaRepository<TJobExecution, Long> {	 
	public List<TJobExecution> findByTJob(TJob tJob); 
	public TJobExecution findByIdAndTJob(Long tJobExecId, TJob tJob); 
}
