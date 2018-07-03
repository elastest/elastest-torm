package io.elastest.etm.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import io.elastest.etm.model.Trace;

@Repository
public interface TraceRepository
        extends JpaRepository<Trace, Long>, QuerydslPredicateExecutor<Trace> {
    public List<Trace> findByTimestamp(String timestamp);

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
