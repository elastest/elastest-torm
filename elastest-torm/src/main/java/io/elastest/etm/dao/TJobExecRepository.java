package io.elastest.etm.dao;

import java.util.List;

import org.springframework.data.domain.Pageable;
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

    @Query(value = "select exec from TJobExecution exec where tjob = :tjobId")
    public List<TJobExecution> findByTJobIdWithPageable(
            @Param("tjobId") Long tjobId, Pageable pageable);

    @Query(value = "select exec from TJobExecution exec")
    public List<TJobExecution> findWithPageable(Pageable pageable);

    @Query("select exec from TJobExecution exec where result in :results")
    public List<TJobExecution> findByResultsWithPageable(
            @Param("results") List<ResultEnum> resultList, Pageable pageable);

    // Without Childs

    @Query("select exec from TJobExecution exec where tjob = :tjobId and (not type = 2 or type is null)")
    public List<TJobExecution> findTJobIdWithoutChilds(
            @Param("tjobId") Long tjobId);

    @Query("select exec from TJobExecution exec where result in :results and (not type = 2 or type is null)")
    public List<TJobExecution> findByResultsWithoutChilds(
            @Param("results") List<ResultEnum> resultList);

    @Query(value = "select exec from TJobExecution exec where (not type = 2 or type is null)")
    public List<TJobExecution> findWithPageableWithoutChilds(Pageable pageable);

    @Query("select exec from TJobExecution exec where result in :results and (not type = 2 or type is null)")
    public List<TJobExecution> findByResultsWithPageableWithoutChilds(
            @Param("results") List<ResultEnum> resultList, Pageable pageable);
}
