package io.elastest.etm.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import io.elastest.etm.model.Enums.StreamType;
import io.elastest.etm.model.Trace;

@Repository
public interface TraceRepository
        extends JpaRepository<Trace, Long>, QuerydslPredicateExecutor<Trace> {
    public List<Trace> findByTimestamp(String timestamp);

    @Query(value = "SELECT DISTINCT :fieldList FROM Trace WHERE NOT streamType=:streamType GROUP BY :fieldList", nativeQuery = true)
    public List<String[]> findMetricsTreeByStreamTypeAndFieldList(
            @Param("streamType") StreamType streamType,
            @Param("fieldList") List<String> fieldList);

    @Query(value = "SELECT DISTINCT :fieldList FROM Trace WHERE streamType=:streamType GROUP BY :fieldList", nativeQuery = true)
    public List<String[]> findLogsTreeByStreamTypeAndFieldList(
            @Param("streamType") StreamType streamType,
            @Param("fieldList") List<String> fieldList);

    /* *** Logs *** */
    public List<Trace> findByExecInAndStreamAndComponent(List<String> execs,
            String stream, String component);

    public List<Trace> findByExecInAndStreamAndComponentAndMessageAndTimestamp(
            List<String> execs, String stream, String component, String message,
            String timestamp);

    public List<Trace> findByExecInAndStreamAndComponentAndIdLessThan(
            List<String> execs, String stream, String component, Long id);

    public List<Trace> findByExecInAndStreamAndComponentOrderByIdDesc(
            List<String> execs, String stream, String component);

    @Query(value = "SELECT * FROM Trace WHERE exec=:exec AND stream=:stream AND component=:component AND message LIKE %:message%", nativeQuery = true)
    public List<Trace> findByExecAndMessageAndComponentAndStream(
            @Param("exec") String exec, @Param("message") String message,
            @Param("component") String component,
            @Param("stream") String stream);

    /* *** Metrics *** */
    public List<Trace> findByExecInAndEtTypeAndComponent(List<String> execs,
            String etType, String component);

    public List<Trace> findByExecInAndEtTypeAndComponentAndTimestamp(
            List<String> execs, String etType, String component,
            String timestamp);

    public List<Trace> findByExecInAndEtTypeAndComponentAndIdLessThan(
            List<String> execs, String etType, String component, Long id);

    public List<Trace> findByExecInAndEtType(List<String> indices,
            String etType);

    public List<Trace> findByExecInAndEtTypeAndTimestamp(List<String> indices,
            String etType, String timestamp);

    public List<Trace> findByExecInAndEtTypeAndIdLessThan(List<String> execs,
            String etType, Long id);

    public List<Trace> findByExecInAndEtTypeAndComponentOrderByIdDesc(
            List<String> execs, String etType, String component);

    public List<Trace> findByExecInAndEtTypeOrderByIdDesc(List<String> execs,
            String etType);
}
