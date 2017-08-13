package io.elastest.etm.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import io.elastest.etm.model.TJob;

public interface TJobRepository extends JpaRepository<TJob, Long> {
	
	public TJob findByName(String name);

}
