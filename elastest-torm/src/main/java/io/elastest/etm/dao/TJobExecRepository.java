package io.elastest.etm.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import io.elastest.etm.model.ElasEtmTjob;
import io.elastest.etm.model.ElasEtmTjobexec;

public interface TJobExecRepository extends JpaRepository<ElasEtmTjobexec, Long> {	 
	public List<ElasEtmTjobexec> findByElasEtmTjob(@Param("tJob") ElasEtmTjob tJob); 
    
	public ElasEtmTjobexec findByElasEtmTjobexecIdAndElasEtmTjob(Long tJobExecId, ElasEtmTjob tJob); 
}
