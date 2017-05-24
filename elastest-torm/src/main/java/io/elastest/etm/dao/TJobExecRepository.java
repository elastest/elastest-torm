package io.elastest.etm.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import io.elastest.etm.model.ElasEtmTjob;
import io.elastest.etm.model.ElasEtmTjobexec;

public interface TJobExecRepository extends JpaRepository<ElasEtmTjobexec, Long> {	 
    @Query("SELECT t FROM ElasEtmTjobexec t WHERE t.elasEtmTjob = :tJob")
	public List<ElasEtmTjobexec> getByTjobId(@Param("tJob") ElasEtmTjob tJob); 
    
    @Query("SELECT t FROM ElasEtmTjobexec t WHERE t.elasEtmTjob = :tJob AND t.elasEtmTjobexecId = :tJobExecId" )
	public ElasEtmTjobexec getTjobExec(@Param("tJob") ElasEtmTjob tJob, @Param("tJobExecId") Long tJobExecId); 
}
