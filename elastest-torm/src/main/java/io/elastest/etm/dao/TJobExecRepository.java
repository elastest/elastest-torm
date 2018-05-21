package io.elastest.etm.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import io.elastest.etm.model.TJob;
import io.elastest.etm.model.TJobExecution;
import io.elastest.etm.model.TJobExecution.ResultEnum;

@Repository
public interface TJobExecRepository extends JpaRepository<TJobExecution, Long> {
    public List<TJobExecution> findByTJob(TJob tJob);

    public TJobExecution findByIdAndTJob(Long tJobExecId, TJob tJob);

    @Query("select exec from TJobExecution exec where result in :results")
    public List<TJobExecution> findByResults(
            @Param("results") List<ResultEnum> resultList);
}
