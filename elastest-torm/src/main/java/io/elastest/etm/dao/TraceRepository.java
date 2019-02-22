package io.elastest.etm.dao;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import io.elastest.etm.model.Enums.StreamType;
import io.elastest.etm.model.Trace;

@Repository
public interface TraceRepository
        extends JpaRepository<Trace, Long>, QuerydslPredicateExecutor<Trace> {
    public List<Trace> findByTimestamp(Date timestamp);

    /* ************************** */
    /* ********** Logs ********** */
    /* ************************** */

    /* *** By Exec, Stream, Component | and Date Ranges *** */

    public List<Trace> findByStreamTypeAndExecInAndStreamAndComponentInOrderByTimestampAsc(
            StreamType streamType, List<String> execs, String stream,
            List<String> components);

    public List<Trace> findByStreamTypeAndExecInAndStreamAndComponentInAndTimestampGreaterThanOrderByTimestampAsc(
            StreamType streamType, List<String> execs, String stream,
            List<String> components, Date start);

    public List<Trace> findByStreamTypeAndExecInAndStreamAndComponentInAndTimestampGreaterThanEqualOrderByTimestampAsc(
            StreamType streamType, List<String> execs, String stream,
            List<String> components, Date start);

    public List<Trace> findByStreamTypeAndExecInAndStreamAndComponentInAndTimestampLessThanOrderByTimestampAsc(
            StreamType streamType, List<String> execs, String stream,
            List<String> components, Date end);

    public List<Trace> findByStreamTypeAndExecInAndStreamAndComponentInAndTimestampLessThanEqualOrderByTimestampAsc(
            StreamType streamType, List<String> execs, String stream,
            List<String> components, Date end);

    public List<Trace> findByStreamTypeAndExecInAndStreamAndComponentInAndTimestampGreaterThanAndTimestampLessThanOrderByTimestampAsc(
            StreamType streamType, List<String> execs, String stream,
            List<String> components, Date start, Date end);

    public List<Trace> findByStreamTypeAndExecInAndStreamAndComponentInAndTimestampGreaterThanEqualAndTimestampLessThanOrderByTimestampAsc(
            StreamType streamType, List<String> execs, String stream,
            List<String> components, Date start, Date end);

    public List<Trace> findByStreamTypeAndExecInAndStreamAndComponentInAndTimestampGreaterThanEqualAndTimestampLessThanEqualOrderByTimestampAsc(
            StreamType streamType, List<String> execs, String stream,
            List<String> components, Date start, Date end);

    public List<Trace> findByStreamTypeAndExecInAndStreamAndComponentInAndTimestampGreaterThanAndTimestampLessThanEqualOrderByTimestampAsc(
            StreamType streamType, List<String> execs, String stream,
            List<String> components, Date start, Date end);

    /* *** By Exec, Stream, Component, And Id Less Than | and Date Range *** */
    public List<Trace> findByStreamTypeAndExecInAndStreamAndComponentAndIdLessThanOrderByTimestampAsc(
            StreamType streamType, List<String> execs, String stream,
            String component, Long id);

    public List<Trace> findByStreamTypeAndExecInAndStreamAndComponentAndIdLessThanAndTimestampGreaterThanOrderByTimestampAsc(
            StreamType streamType, List<String> execs, String stream,
            String component, Long id, Date start);

    public List<Trace> findByStreamTypeAndExecInAndStreamAndComponentAndIdLessThanAndTimestampGreaterThanEqualOrderByTimestampAsc(
            StreamType streamType, List<String> execs, String stream,
            String component, Long id, Date start);

    public List<Trace> findByStreamTypeAndExecInAndStreamAndComponentAndIdLessThanAndTimestampLessThanOrderByTimestampAsc(
            StreamType streamType, List<String> execs, String stream,
            String component, Long id, Date end);

    public List<Trace> findByStreamTypeAndExecInAndStreamAndComponentAndIdLessThanAndTimestampLessThanEqualOrderByTimestampAsc(
            StreamType streamType, List<String> execs, String stream,
            String component, Long id, Date end);

    public List<Trace> findByStreamTypeAndExecInAndStreamAndComponentAndIdLessThanAndTimestampGreaterThanAndTimestampLessThanOrderByTimestampAsc(
            StreamType streamType, List<String> execs, String stream,
            String component, Long id, Date start, Date end);

    public List<Trace> findByStreamTypeAndExecInAndStreamAndComponentAndIdLessThanAndTimestampGreaterThanEqualAndTimestampLessThanOrderByTimestampAsc(
            StreamType streamType, List<String> execs, String stream,
            String component, Long id, Date start, Date end);

    public List<Trace> findByStreamTypeAndExecInAndStreamAndComponentAndIdLessThanAndTimestampGreaterThanEqualAndTimestampLessThanEqualOrderByTimestampAsc(
            StreamType streamType, List<String> execs, String stream,
            String component, Long id, Date start, Date end);

    public List<Trace> findByStreamTypeAndExecInAndStreamAndComponentAndIdLessThanAndTimestampGreaterThanAndTimestampLessThanEqualOrderByTimestampAsc(
            StreamType streamType, List<String> execs, String stream,
            String component, Long id, Date start, Date end);

    /* *** By Exec, Stream, Component, etc *** */

    public List<Trace> findByStreamTypeAndExecInAndStreamAndComponentAndMessageAndTimestampOrderByTimestampAsc(
            StreamType streamType, List<String> execs, String stream,
            String component, String message, Date timestamp);

    /* *** By Exec, Stream, Component by id DESC | and date range *** */
    public List<Trace> findByStreamTypeAndExecInAndStreamAndComponentOrderByIdDescTimestampDesc(
            StreamType streamType, List<String> execs, String stream,
            String component);

    public List<Trace> findByStreamTypeAndExecInAndStreamAndComponentAndTimestampLessThanEqualOrderByIdDescTimestampDesc(
            StreamType streamType, List<String> execs, String stream,
            String component, Date end);

    /* *************************** */
    /* ********* Metrics ********* */
    /* *************************** */

    /* *** By Exec, EtType | Date Ranges *** */
    public List<Trace> findByExecInAndEtType(List<String> indices,
            String etType);

    public List<Trace> findByExecInAndEtTypeAndTimestampGreaterThan(
            List<String> indices, String etType, Date start);

    public List<Trace> findByExecInAndEtTypeAndTimestampGreaterThanEqual(
            List<String> indices, String etType, Date start);

    public List<Trace> findByExecInAndEtTypeAndTimestampLessThan(
            List<String> indices, String etType, Date end);

    public List<Trace> findByExecInAndEtTypeAndTimestampLessThanEqual(
            List<String> indices, String etType, Date end);

    public List<Trace> findByExecInAndEtTypeAndTimestampGreaterThanAndTimestampLessThan(
            List<String> indices, String etType, Date start, Date end);

    public List<Trace> findByExecInAndEtTypeAndTimestampGreaterThanEqualAndTimestampLessThan(
            List<String> indices, String etType, Date start, Date end);

    public List<Trace> findByExecInAndEtTypeAndTimestampGreaterThanEqualAndTimestampLessThanEqual(
            List<String> indices, String etType, Date start, Date end);

    public List<Trace> findByExecInAndEtTypeAndTimestampGreaterThanAndTimestampLessThanEqual(
            List<String> indices, String etType, Date start, Date end);

    /* *** By Exec, EtType, Component | Date Ranges *** */
    public List<Trace> findByExecInAndEtTypeAndComponent(List<String> execs,
            String etType, String component);

    public List<Trace> findByExecInAndEtTypeAndComponentAndTimestampGreaterThan(
            List<String> indices, String etType, String component, Date start);

    public List<Trace> findByExecInAndEtTypeAndComponentAndTimestampGreaterThanEqual(
            List<String> indices, String etType, String component, Date start);

    public List<Trace> findByExecInAndEtTypeAndComponentAndTimestampLessThan(
            List<String> indices, String etType, String component, Date end);

    public List<Trace> findByExecInAndEtTypeAndComponentAndTimestampLessThanEqual(
            List<String> indices, String etType, String component, Date end);

    public List<Trace> findByExecInAndEtTypeAndComponentAndTimestampGreaterThanAndTimestampLessThan(
            List<String> indices, String etType, String component, Date start,
            Date end);

    public List<Trace> findByExecInAndEtTypeAndComponentAndTimestampGreaterThanEqualAndTimestampLessThan(
            List<String> indices, String etType, String component, Date start,
            Date end);

    public List<Trace> findByExecInAndEtTypeAndComponentAndTimestampGreaterThanEqualAndTimestampLessThanEqual(
            List<String> indices, String etType, String component, Date start,
            Date end);

    public List<Trace> findByExecInAndEtTypeAndComponentAndTimestampGreaterThanAndTimestampLessThanEqual(
            List<String> indices, String etType, String component, Date start,
            Date end);

    /* *** By Exec, EtType, Component, etc *** */

    public List<Trace> findByExecInAndEtTypeAndComponentAndTimestamp(
            List<String> execs, String etType, String component,
            Date timestamp);

    public List<Trace> findByExecInAndEtTypeAndComponentAndIdLessThan(
            List<String> execs, String etType, String component, Long id);

    public List<Trace> findByExecInAndEtTypeAndTimestamp(List<String> indices,
            String etType, Date timestamp);

    public List<Trace> findByExecInAndEtTypeAndIdLessThan(List<String> execs,
            String etType, Long id);

    public List<Trace> findByExecInAndEtTypeAndComponentOrderByIdDesc(
            List<String> execs, String etType, String component);

    public List<Trace> findByExecInAndEtTypeOrderByIdDesc(List<String> execs,
            String etType);
}
