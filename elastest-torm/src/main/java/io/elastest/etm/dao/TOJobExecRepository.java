package io.elastest.etm.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import io.elastest.etm.api.model.TOJob;
import io.elastest.etm.api.model.TOJobExecution;

public interface TOJobExecRepository extends JpaRepository<TOJobExecution, Long> {
	
	public List<TOJobExecution> findByTOJob(TOJob tOJob);
	public TOJobExecution findByTOJobAndId(TOJob tOJob, Long id);

}
