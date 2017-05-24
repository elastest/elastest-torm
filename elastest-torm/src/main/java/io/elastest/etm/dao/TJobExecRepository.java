package io.elastest.etm.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import io.elastest.etm.model.ElasEtmTjobexec;

public interface TJobExecRepository extends JpaRepository<ElasEtmTjobexec, Long> {	 
//    @Query("SELECT t FROM ELAS_ETM_TJOBEXEC t WHERE LOWER(t.ELAS_ETM_TJOBEXEC_TJOB) = :tJobId")
//	public List<ElasEtmTjobexec> getByTjobId(@Param("tJobId") Long tJobId); 
}
