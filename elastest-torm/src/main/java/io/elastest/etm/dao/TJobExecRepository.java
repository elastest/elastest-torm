package io.elastest.etm.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import io.elastest.etm.api.model.TJob;
import io.elastest.etm.api.model.TJobExecution;
import io.elastest.etm.dao.extension.TJobExecRepositoryExtension;

@Repository
public interface TJobExecRepository extends JpaRepository<TJobExecution, Long>, TJobExecRepositoryExtension {	 
	public List<TJobExecution> findByTJob(TJob tJob); 
	public TJobExecution findByIdAndTJob(Long tJobExecId, TJob tJob); 
}
