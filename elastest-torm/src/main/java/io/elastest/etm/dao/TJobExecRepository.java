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

    /* *** By Results *** */
    @Query("select exec from TJobExecution exec where result in :results")
    public List<TJobExecution> findByResults(
            @Param("results") List<ResultEnum> resultList);

    @Query("select exec from TJobExecution exec where result in :results and id<:id")
    public List<TJobExecution> findByResultsAndIdLessThan(
            @Param("results") List<ResultEnum> resultList,
            @Param("id") Long id);

    @Query("select exec from TJobExecution exec where result in :results and id>:id")
    public List<TJobExecution> findByResultsAndIdGreaterThan(
            @Param("results") List<ResultEnum> resultList,
            @Param("id") Long id);

    /* *** */
    @Query(value = "select exec from TJobExecution exec where tjob = :tjobId")
    public List<TJobExecution> findByTJobIdWithPageable(
            @Param("tjobId") Long tjobId, Pageable pageable);

    @Query(value = "select exec from TJobExecution exec where tjob = :tjobId and id<:id")
    public List<TJobExecution> findByTJobIdAndIdLessThanWithPageable(
            @Param("tjobId") Long tjobId, @Param("id") Long id,
            Pageable pageable);

    @Query(value = "select exec from TJobExecution exec where tjob = :tjobId and id>:id")
    public List<TJobExecution> findByTJobIdAndIdGreaterThanWithPageable(
            @Param("tjobId") Long tjobId, @Param("id") Long id,
            Pageable pageable);

    /* *** */

    @Query(value = "select exec from TJobExecution exec")
    public List<TJobExecution> findWithPageable(Pageable pageable);

    @Query(value = "select exec from TJobExecution exec where id<:id")
    public List<TJobExecution> findByIdLessThanWithPageable(
            @Param("id") Long id, Pageable pageable);

    @Query(value = "select exec from TJobExecution exec where id>:id")
    public List<TJobExecution> findByIdGreaterThanWithPageable(
            @Param("id") Long id, Pageable pageable);

    /* *** By Results *** */

    @Query("select exec from TJobExecution exec where result in :results")
    public List<TJobExecution> findByResultsWithPageable(
            @Param("results") List<ResultEnum> resultList, Pageable pageable);

    @Query("select exec from TJobExecution exec where result in :results and id<:id")
    public List<TJobExecution> findByResultsAndIdLessThanWithPageable(
            @Param("results") List<ResultEnum> resultList, @Param("id") Long id,
            Pageable pageable);

    @Query("select exec from TJobExecution exec where result in :results and id>:id")
    public List<TJobExecution> findByResultsAndIdGreaterThanWithPageable(
            @Param("results") List<ResultEnum> resultList, @Param("id") Long id,
            Pageable pageable);

    /* ********************** */
    /* *** Without Childs *** */
    /* ********************** */

    @Query("select exec from TJobExecution exec where tjob = :tjobId and (not type = 2 or type is null)")
    public List<TJobExecution> findTJobIdWithoutChilds(
            @Param("tjobId") Long tjobId);

    @Query("select exec from TJobExecution exec where tjob = :tjobId and (not type = 2 or type is null) and id<:id")
    public List<TJobExecution> findTJobIdAndIdLessThanWithoutChilds(
            @Param("tjobId") Long tjobId, @Param("id") Long id);

    @Query("select exec from TJobExecution exec where tjob = :tjobId and (not type = 2 or type is null) and id>:id")
    public List<TJobExecution> findTJobIdAndIdGreaterThanWithoutChilds(
            @Param("tjobId") Long tjobId, @Param("id") Long id);

    /* *** By Results *** */

    @Query("select exec from TJobExecution exec where result in :results and (not type = 2 or type is null)")
    public List<TJobExecution> findByResultsWithoutChilds(
            @Param("results") List<ResultEnum> resultList);

    @Query("select exec from TJobExecution exec where result in :results and (not type = 2 or type is null) and id<:id")
    public List<TJobExecution> findByResultsAndIdLessThanWithoutChilds(
            @Param("results") List<ResultEnum> resultList,
            @Param("id") Long id);

    @Query("select exec from TJobExecution exec where result in :results and (not type = 2 or type is null) and id>:id")
    public List<TJobExecution> findByResultsAndIdGreaterThanWithoutChilds(
            @Param("results") List<ResultEnum> resultList,
            @Param("id") Long id);

    /* *** */

    @Query(value = "select exec from TJobExecution exec where (not type = 2 or type is null)")
    public List<TJobExecution> findWithPageableWithoutChilds(Pageable pageable);

    @Query(value = "select exec from TJobExecution exec where (not type = 2 or type is null) and id<:id")
    public List<TJobExecution> findByIdLessThanWithPageableWithoutChilds(
            @Param("id") Long id, Pageable pageable);

    @Query(value = "select exec from TJobExecution exec where (not type = 2 or type is null) and id>:id")
    public List<TJobExecution> findByIdGreaterThanWithPageableWithoutChilds(
            @Param("id") Long id, Pageable pageable);

    /* *** */

    @Query("select exec from TJobExecution exec where result in :results and (not type = 2 or type is null)")
    public List<TJobExecution> findByResultsWithPageableWithoutChilds(
            @Param("results") List<ResultEnum> resultList, Pageable pageable);

    @Query("select exec from TJobExecution exec where result in :results and (not type = 2 or type is null) and id<:id")
    public List<TJobExecution> findByResultsAndIdLessThanWithPageableWithoutChilds(
            @Param("results") List<ResultEnum> resultList, @Param("id") Long id,
            Pageable pageable);

    @Query("select exec from TJobExecution exec where result in :results and (not type = 2 or type is null) and id>:id")
    public List<TJobExecution> findByResultsAndIdGreaterThanWithPageableWithoutChilds(
            @Param("results") List<ResultEnum> resultList, @Param("id") Long id,
            Pageable pageable);
}
