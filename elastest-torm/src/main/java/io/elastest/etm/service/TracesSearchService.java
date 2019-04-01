package io.elastest.etm.service;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.validation.Valid;

import org.apache.commons.collections4.ListUtils;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.ExampleMatcher.StringMatcher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringTemplate;
import com.querydsl.jpa.impl.JPAQueryFactory;

import io.elastest.etm.dao.TestSuiteRepository;
import io.elastest.etm.dao.TraceRepository;
import io.elastest.etm.model.AggregationTree;
import io.elastest.etm.model.Enums.LevelEnum;
import io.elastest.etm.model.Enums.StreamType;
import io.elastest.etm.model.LogAnalyzerQuery;
import io.elastest.etm.model.MonitoringQuery;
import io.elastest.etm.model.QTrace;
import io.elastest.etm.model.TimeRange;
import io.elastest.etm.model.Trace;
import io.elastest.etm.utils.UtilTools;
import io.elastest.etm.utils.UtilsService;

public class TracesSearchService extends AbstractMonitoringService {
    TraceRepository traceRepository;

    @PersistenceContext
    private EntityManager em;

    JPAQueryFactory queryFactory;

    public TracesSearchService(TraceRepository traceRepository,
            TestSuiteRepository testSuiteRepository, UtilsService utilsService) {
        super(testSuiteRepository, utilsService);
        this.traceRepository = traceRepository;
    }

    @PostConstruct
    private void init() {
        queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public void createMonitoringIndex(String[] indicesList) {
        // Nothing to do
    }

    @Override
    public List<Map<String, Object>> searchAllByTerms(
            MonitoringQuery monitoringQuery) throws Exception {
        List<Trace> traces = new ArrayList<>();
        ExampleMatcher matcher = ExampleMatcher.matching();
        Trace sampleTrace = new Trace();

        for (String field : monitoringQuery.getSelectedTerms()) {
            matcher = matcher.withMatcher(field,
                    ExampleMatcher.GenericPropertyMatcher
                            .of(StringMatcher.EXACT));

            sampleTrace.setAttributeByGivenName(field,
                    monitoringQuery.getAttributeValueByGivenName(field));
        }

        for (String exec : monitoringQuery.getIndices()) {
            sampleTrace.setExec(exec);
            Example<Trace> example = Example.of(sampleTrace, matcher);

            traces.addAll(traceRepository.findAll(example));
        }

        return this.getTracesMapListByTracesList(traces);
    }

    public Iterable<Tuple> getMonitoringTree(MonitoringQuery monitoringQuery,
            boolean isMetric) throws Exception {
        StreamType log = StreamType.LOG;
        Iterable<Tuple> treeValues = new ArrayList<>();

        List<Path<?>> monitoringTreeExpressionList = new ArrayList<>();
        for (String currentSelectedTerm : monitoringQuery.getSelectedTerms()) {
            Path<?> currentExpression = monitoringQuery
                    .getAttributeBooleanExpressionByGivenName(
                            currentSelectedTerm);
            if (currentExpression != null) {
                monitoringTreeExpressionList.add(currentExpression);
            }

        }

        // Select / group by
        Expression<?>[] selectExpression = null;
        if (monitoringTreeExpressionList.size() > 0) {
            Path<?>[] selectedArray = monitoringTreeExpressionList
                    .toArray(new Path<?>[monitoringTreeExpressionList.size()]);
            selectExpression = selectedArray;
        }

        if (selectExpression != null) {

            // Where
            BooleanExpression whereExpression = QTrace.trace.exec
                    .in(monitoringQuery.getIndices());
            if (isMetric) {
                // Not Log
                whereExpression = whereExpression
                        .and(QTrace.trace.streamType.ne(log));
            } else {
                whereExpression = whereExpression
                        .and(QTrace.trace.streamType.eq(log));
            }

            treeValues = queryFactory.selectDistinct(selectExpression)
                    .from(QTrace.trace).groupBy(selectExpression)
                    .where(whereExpression).fetch();
        }
        return treeValues;
    }

    /* ******************************************************************** */
    /* ************************** Implementation ************************** */
    /* ******************************************************************** */

    public boolean deleteMonitoringDataByExec(String exec) {
        boolean deleted = false;
        try {
            traceRepository.deleteByExec(exec);
            deleted = true;
        } catch (Exception e) {
            logger.error("Error on delete monitoring data by exec {}", exec);
        }

        return deleted;
    }

    /* ****************************************** */
    /* ****************** Logs ****************** */
    /* ****************************************** */

    @Override
    public List<Map<String, Object>> searchAllLogs(
            MonitoringQuery monitoringQuery) throws Exception {
        List<Trace> traces = this.searchAllLogsTraces(monitoringQuery);

        return this.getTracesMapListByTracesList(traces);
    }

    public List<Trace> searchAllLogsByTimeRange(MonitoringQuery monitoringQuery)
            throws Exception {
        List<Trace> traces = null;
        TimeRange timeRange = monitoringQuery.getTimeRange();
        if (timeRange != null && !timeRange.isEmpty()) {
            List<String> indices = monitoringQuery.getIndices();
            String stream = monitoringQuery.getStream();
            String component = monitoringQuery.getComponent();

            Date gt = timeRange.getGt();
            Date gte = timeRange.getGte();
            Date lt = timeRange.getLt();
            Date lte = timeRange.getLte();

            // If components list not empty, use list. Else, use unique
            // component
            List<String> components = monitoringQuery.getComponents();
            components = components != null && components.size() > 0
                    ? components
                    : Arrays.asList(component);

            if (gt != null) {
                // gt and lt
                if (lt != null) {
                    traces = traceRepository
                            .findByStreamTypeAndExecInAndStreamAndComponentInAndTimestampGreaterThanAndTimestampLessThanOrderByIdAscTimestampAsc(
                                    StreamType.LOG, indices, stream, components,
                                    gt, lt);
                } else {
                    // gt and lte
                    if (lte != null) {
                        traces = traceRepository
                                .findByStreamTypeAndExecInAndStreamAndComponentInAndTimestampGreaterThanAndTimestampLessThanEqualOrderByIdAscTimestampAsc(
                                        StreamType.LOG, indices, stream,
                                        components, gt, lte);
                    } else { // gt only
                        traces = traceRepository
                                .findByStreamTypeAndExecInAndStreamAndComponentInAndTimestampGreaterThanOrderByIdAscTimestampAsc(
                                        StreamType.LOG, indices, stream,
                                        components, gt);
                    }
                }

            } else if (gte != null) {
                // gte and lt
                if (lt != null) {
                    traces = traceRepository
                            .findByStreamTypeAndExecInAndStreamAndComponentInAndTimestampGreaterThanEqualAndTimestampLessThanOrderByIdAscTimestampAsc(
                                    StreamType.LOG, indices, stream, components,
                                    gte, lt);
                } else {
                    // gte and lte
                    if (lte != null) {
                        traces = traceRepository
                                .findByStreamTypeAndExecInAndStreamAndComponentInAndTimestampGreaterThanEqualAndTimestampLessThanEqualOrderByIdAscTimestampAsc(
                                        StreamType.LOG, indices, stream,
                                        components, gte, lte);
                    } else { // gte only
                        traces = traceRepository
                                .findByStreamTypeAndExecInAndStreamAndComponentInAndTimestampGreaterThanEqualOrderByIdAscTimestampAsc(
                                        StreamType.LOG, indices, stream,
                                        components, gte);
                    }
                }
            } else if (lte != null) {
                traces = traceRepository
                        .findByStreamTypeAndExecInAndStreamAndComponentInAndTimestampLessThanEqualOrderByIdAscTimestampAsc(
                                StreamType.LOG, indices, stream, components,
                                lte);
            } else if (lt != null) {
                traces = traceRepository
                        .findByStreamTypeAndExecInAndStreamAndComponentInAndTimestampLessThanOrderByIdAscTimestampAsc(
                                StreamType.LOG, indices, stream, components,
                                lt);
            }
        }

        return traces;
    }

    public List<Trace> searchAllLogsTraces(MonitoringQuery monitoringQuery)
            throws Exception {
        List<Trace> traces;
        if (monitoringQuery.getTimeRange() != null
                && !monitoringQuery.getTimeRange().isEmpty()) {
            traces = this.searchAllLogsByTimeRange(monitoringQuery);

        } else {
            // If components list not empty, use list. Else, use unique
            // component
            List<String> components = monitoringQuery.getComponents();
            components = components != null && components.size() > 0
                    ? components
                    : Arrays.asList(monitoringQuery.getComponent());

            traces = traceRepository
                    .findByStreamTypeAndExecInAndStreamAndComponentInOrderByIdAscTimestampAsc(
                            StreamType.LOG, monitoringQuery.getIndices(),
                            monitoringQuery.getStream(), components);
        }
        return traces;
    }

    @Override
    public List<String> searchAllLogsMessage(MonitoringQuery monitoringQuery,
            boolean withTimestamp, boolean timeDiff) throws Exception {
        return searchAllLogsMessage(monitoringQuery, withTimestamp, timeDiff,
                false);
    }

    @Override
    public List<String> searchAllLogsMessage(MonitoringQuery monitoringQuery,
            boolean withTimestamp, boolean timeDiff,
            boolean discardStartFinishTestTraces) throws Exception {
        List<String> logs = new ArrayList<>();
        List<Trace> logTraces = searchAllLogsTraces(monitoringQuery);
        if (logTraces != null) {
            Trace firstTrace = null;
            for (Trace trace : logTraces) {
                if (trace != null && trace.getMessage() != null) {
                    String message = trace.getMessage();

                    boolean isStartFinishTraceAndDiscardActivated = discardStartFinishTestTraces
                            && (utilsService.containsTCStartMsgPrefix(message)
                                    || utilsService.containsTCFinishMsgPrefix(
                                            message));

                    // If is start/finish test trace and modify
                    if (!isStartFinishTraceAndDiscardActivated) {

                        // Temporally withTimestamp is Complete message (
                        // because some messages has timestamp into)
                        // if (withTimestamp && trace.getTimestamp() != null) {
                        if (withTimestamp) {
                            if (timeDiff) {
                                long traceTimeDiff = trace.getTimestamp()
                                        .getTime();
                                // First is 0
                                if (firstTrace == null) {
                                    traceTimeDiff = 0;
                                    firstTrace = trace;
                                } else { // Others is difference with the first
                                    traceTimeDiff -= firstTrace.getTimestamp()
                                            .getTime();
                                }

                                message = traceTimeDiff + " " + message;
                            } else {
                                // Temporally behavior changed
                                // message = trace.getTimestamp().toString()
                                // + " "
                                // + message;
                            }
                        } else {
                            // Remove timestamp from message start (if has)

                            // 2019-03-01 10:54:01.462
                            String msgWithTimestampRegex = "^\\d\\d\\d\\d*-\\d\\d-\\d\\d\\s\\d\\d:\\d\\d:\\d\\d\\.\\d\\d\\d\\s\\s";
                            // Feb 25, 2019 11:15:34 AM
                            String msgWithTimestampRegex2 = "^[A-Z][a-z][a-z]\\s\\d\\d,\\s\\d\\d\\d\\d\\s\\d\\d:\\d\\d:\\d\\d\\s[A-Z][A-Z]\\s";
                            message = message
                                    .replaceFirst(msgWithTimestampRegex, "");
                            message = message
                                    .replaceFirst(msgWithTimestampRegex2, "");
                        }

                        logs.add(message);
                    }

                }
            }
        }

        return logs;
    }

    @Override
    public List<Map<String, Object>> getLastLogs(
            MonitoringQuery monitoringQuery, int size) throws Exception {
        List<Trace> traces = traceRepository
                .findByStreamTypeAndExecInAndStreamAndComponentOrderByIdDescTimestampDesc(
                        StreamType.LOG, monitoringQuery.getIndices(),
                        monitoringQuery.getStream(),
                        monitoringQuery.getComponent());

        if (traces.size() > 0) {
            traces = ListUtils.partition(traces, size).get(0);
            // Sort ASC
            Collections.reverse(traces);
        }

        return this.getTracesMapListByTracesList(traces);
    }

    @Override
    public List<Map<String, Object>> getPreviousLogsFromTimestamp(
            MonitoringQuery monitoringQuery) throws Exception {
        // Get by timestamp
        List<Trace> tracesWithSameTimestamp = traceRepository
                .findByStreamTypeAndExecInAndStreamAndComponentAndMessageAndTimestampOrderByIdAscTimestampAsc(
                        StreamType.LOG, monitoringQuery.getIndices(),
                        monitoringQuery.getStream(),
                        monitoringQuery.getComponent(),
                        monitoringQuery.getMessage(),
                        utilsService.getGMT0DateFromIso8601Str(
                                monitoringQuery.getTimestamp()));

        List<Trace> traces = new ArrayList<>();
        if (tracesWithSameTimestamp.size() > 0) {
            Long referenceTraceId = tracesWithSameTimestamp.get(0).getId();
            // Get previous

            // By time Range
            if (monitoringQuery.getTimeRange() != null
                    && !monitoringQuery.getTimeRange().isEmpty()) {
                traces = this.getPreviousLogsFromTraceIdByTimeRange(
                        monitoringQuery, referenceTraceId);

            } else { // Without range
                traces = traceRepository
                        .findByStreamTypeAndExecInAndStreamAndComponentAndIdLessThanOrderByIdAscTimestampAsc(
                                StreamType.LOG, monitoringQuery.getIndices(),
                                monitoringQuery.getStream(),
                                monitoringQuery.getComponent(),
                                referenceTraceId);
            }
        }
        return this.getTracesMapListByTracesList(traces);
    }

    public List<Trace> getPreviousLogsFromTraceIdByTimeRange(
            MonitoringQuery monitoringQuery, Long referenceTraceId)
            throws Exception {
        List<Trace> traces = null;
        TimeRange timeRange = monitoringQuery.getTimeRange();
        if (timeRange != null && !timeRange.isEmpty()) {
            List<String> indices = monitoringQuery.getIndices();
            String stream = monitoringQuery.getStream();
            String component = monitoringQuery.getComponent();

            Date gt = timeRange.getGt();
            Date gte = timeRange.getGte();
            Date lt = timeRange.getLt();
            Date lte = timeRange.getLte();
            if (gt != null) {
                // gt and lt
                if (lt != null) {
                    traces = traceRepository
                            .findByStreamTypeAndExecInAndStreamAndComponentAndIdLessThanAndTimestampGreaterThanAndTimestampLessThanOrderByIdAscTimestampAsc(
                                    StreamType.LOG, indices, stream, component,
                                    referenceTraceId, gt, lt);
                } else {
                    // gt and lte
                    if (lte != null) {
                        traces = traceRepository
                                .findByStreamTypeAndExecInAndStreamAndComponentAndIdLessThanAndTimestampGreaterThanAndTimestampLessThanEqualOrderByIdAscTimestampAsc(
                                        StreamType.LOG, indices, stream,
                                        component, referenceTraceId, gt, lte);
                    } else { // gt only
                        traces = traceRepository
                                .findByStreamTypeAndExecInAndStreamAndComponentAndIdLessThanAndTimestampGreaterThanOrderByIdAscTimestampAsc(
                                        StreamType.LOG, indices, stream,
                                        component, referenceTraceId, gt);
                    }
                }

            } else if (gte != null) {
                // gte and lt
                if (lt != null) {
                    traces = traceRepository
                            .findByStreamTypeAndExecInAndStreamAndComponentAndIdLessThanAndTimestampGreaterThanEqualAndTimestampLessThanOrderByIdAscTimestampAsc(
                                    StreamType.LOG, indices, stream, component,
                                    referenceTraceId, gte, lt);
                } else {
                    // gte and lte
                    if (lte != null) {
                        traces = traceRepository
                                .findByStreamTypeAndExecInAndStreamAndComponentAndIdLessThanAndTimestampGreaterThanEqualAndTimestampLessThanEqualOrderByIdAscTimestampAsc(
                                        StreamType.LOG, indices, stream,
                                        component, referenceTraceId, gte, lte);
                    } else { // gte only
                        traces = traceRepository
                                .findByStreamTypeAndExecInAndStreamAndComponentAndIdLessThanAndTimestampGreaterThanEqualOrderByIdAscTimestampAsc(
                                        StreamType.LOG, indices, stream,
                                        component, referenceTraceId, gte);
                    }
                }
            } else if (lte != null) {
                traces = traceRepository
                        .findByStreamTypeAndExecInAndStreamAndComponentAndIdLessThanAndTimestampLessThanEqualOrderByIdAscTimestampAsc(
                                StreamType.LOG, indices, stream, component,
                                referenceTraceId, lte);
            } else if (lt != null) {
                traces = traceRepository
                        .findByStreamTypeAndExecInAndStreamAndComponentAndIdLessThanAndTimestampLessThanOrderByIdAscTimestampAsc(
                                StreamType.LOG, indices, stream, component,
                                referenceTraceId, lt);
            }
        }

        return traces;

    }

    @Override
    @SuppressWarnings("rawtypes")
    public List<AggregationTree> searchLogsTree(
            @Valid MonitoringQuery monitoringQuery) throws Exception {
        Iterable<Tuple> treeValues = this.getMonitoringTree(monitoringQuery,
                false);

        Map<String, Map> tmpMetricsTreeMap = new HashMap<>();
        for (Tuple treeValuesTuple : treeValues) {
            if (treeValuesTuple != null) {
                Object[] componentStream = treeValuesTuple.toArray();
                tmpMetricsTreeMap = this.getMapTree(componentStream,
                        tmpMetricsTreeMap);
            }
        }

        return this.getAggTreeList(tmpMetricsTreeMap);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public List<AggregationTree> searchLogsLevelsTree(
            @Valid MonitoringQuery monitoringQuery) throws Exception {
        Iterable<Tuple> treeValues = this.getMonitoringTree(monitoringQuery,
                false);

        Map<String, Map> tmpMetricsTreeMap = new HashMap<>();
        for (Tuple treeValuesTuple : treeValues) {
            if (treeValuesTuple != null) {
                Object[] componentStream = treeValuesTuple.toArray();
                tmpMetricsTreeMap = this.getMapTree(componentStream,
                        tmpMetricsTreeMap);
            }
        }

        return this.getAggTreeList(tmpMetricsTreeMap);
    }

    /* *** Messages *** */

    public List<Trace> findMessage(String index, String msg,
            List<String> components) throws IOException {
        String regex = ".*"
                + UtilTools.replaceAllSpecialCharactersForQueryDsl(msg.trim())
                + " .*";

        BooleanExpression query = QTrace.trace.exec.eq(index)
                .and(QTrace.trace.component.in(components))
                .and(QTrace.trace.stream.eq("default_log"))
                .and(QTrace.trace.message.matches(regex)
                        .or(QTrace.trace.message.matches(regex)));

        return queryFactory.select(QTrace.trace).from(QTrace.trace).where(query)
                .fetch();
    }

    public Trace findFirstTrace(String index, String msg,
            List<String> components) throws Exception {
        List<Trace> traces = this.findMessage(index, msg, components);
        if (traces != null && traces.size() > 0) {
            Trace firstResult = traces.get(0);

            return firstResult;
        }

        return null;
    }

    @Override
    public Date findFirstMsgAndGetTimestamp(String index, String msg,
            List<String> components) throws Exception {
        Trace firstTrace = this.findFirstTrace(index, msg, components);
        if (firstTrace != null) {
            Date timestamp = firstTrace.getTimestamp();

            return timestamp;
        }

        return null;
    }

    public Trace findLastTrace(String index, String msg,
            List<String> components) throws Exception {
        List<Trace> traces = this.findMessage(index, msg, components);
        if (traces != null && traces.size() > 0) {
            Trace lastResult = traces.get(traces.size() - 1);

            return lastResult;
        }

        return null;
    }

    @Override
    public Date findLastMsgAndGetTimestamp(String index, String msg,
            List<String> components) throws Exception {
        Trace lastTrace = this.findLastTrace(index, msg, components);
        if (lastTrace != null) {
            Date timestamp = lastTrace.getTimestamp();

            return timestamp;
        }

        return null;
    }

    /* ***************************************** */
    /* **************** Metrics **************** */
    /* ***************************************** */

    @Override
    public List<Map<String, Object>> searchAllMetrics(
            MonitoringQuery monitoringQuery) throws Exception {
        List<Trace> traces = new ArrayList<>();

        boolean withTimeRange = monitoringQuery.getTimeRange() != null
                && !monitoringQuery.getTimeRange().isEmpty();

        if (monitoringQuery.getComponent() != null) {
            if (withTimeRange) {
                traces = this.searchAllMetricsByTimeRangeWithComponent(
                        monitoringQuery);
            } else {
                traces = traceRepository.findByExecInAndEtTypeAndComponent(
                        monitoringQuery.getIndices(),
                        monitoringQuery.getEtType(),
                        monitoringQuery.getComponent());
            }
        } else {
            if (withTimeRange) {
                traces = this.searchAllMetricsByTimeRangeWithoutComponent(
                        monitoringQuery);
            } else {
                traces = traceRepository.findByExecInAndEtType(
                        monitoringQuery.getIndices(),
                        monitoringQuery.getEtType());
            }
        }

        return this.getTracesMapListByTracesList(traces);
    }

    public List<Trace> searchAllMetricsByTimeRangeWithComponent(
            MonitoringQuery monitoringQuery) throws Exception {
        List<Trace> traces = null;
        TimeRange timeRange = monitoringQuery.getTimeRange();
        if (timeRange != null && !timeRange.isEmpty()) {
            List<String> indices = monitoringQuery.getIndices();
            String etType = monitoringQuery.getEtType();
            String component = monitoringQuery.getComponent();

            Date gt = timeRange.getGt();
            Date gte = timeRange.getGte();
            Date lt = timeRange.getLt();
            Date lte = timeRange.getLte();
            if (gt != null) {
                // gt and lt
                if (lt != null) {
                    traces = traceRepository
                            .findByExecInAndEtTypeAndComponentAndTimestampGreaterThanAndTimestampLessThan(
                                    indices, etType, component, gt, lt);
                } else {
                    // gt and lte
                    if (lte != null) {
                        traces = traceRepository
                                .findByExecInAndEtTypeAndComponentAndTimestampGreaterThanAndTimestampLessThanEqual(
                                        indices, etType, component, gt, lte);
                    } else { // gt only
                        traces = traceRepository
                                .findByExecInAndEtTypeAndComponentAndTimestampGreaterThan(
                                        indices, etType, component, gt);
                    }
                }

            } else if (gte != null) {
                // gte and lt
                if (lt != null) {
                    traces = traceRepository
                            .findByExecInAndEtTypeAndComponentAndTimestampGreaterThanEqualAndTimestampLessThan(
                                    indices, etType, component, gte, lt);
                } else {
                    // gte and lte
                    if (lte != null) {
                        traces = traceRepository
                                .findByExecInAndEtTypeAndComponentAndTimestampGreaterThanEqualAndTimestampLessThanEqual(
                                        indices, etType, component, gte, lte);
                    } else { // gte only
                        traces = traceRepository
                                .findByExecInAndEtTypeAndComponentAndTimestampGreaterThanEqual(
                                        indices, etType, component, gte);
                    }
                }
            } else if (lte != null) {
                traces = traceRepository
                        .findByExecInAndEtTypeAndComponentAndTimestampLessThanEqual(
                                indices, etType, component, lte);
            } else if (lt != null) {
                traces = traceRepository
                        .findByExecInAndEtTypeAndComponentAndTimestampLessThan(
                                indices, etType, component, lt);
            }
        }

        return traces;
    }

    public List<Trace> searchAllMetricsByTimeRangeWithoutComponent(
            MonitoringQuery monitoringQuery) throws Exception {
        List<Trace> traces = null;
        TimeRange timeRange = monitoringQuery.getTimeRange();
        if (timeRange != null && !timeRange.isEmpty()) {
            List<String> indices = monitoringQuery.getIndices();
            String etType = monitoringQuery.getEtType();

            Date gt = timeRange.getGt();
            Date gte = timeRange.getGte();
            Date lt = timeRange.getLt();
            Date lte = timeRange.getLte();
            if (gt != null) {
                // gt and lt
                if (lt != null) {
                    traces = traceRepository
                            .findByExecInAndEtTypeAndTimestampGreaterThanAndTimestampLessThan(
                                    indices, etType, gt, lt);
                } else {
                    // gt and lte
                    if (lte != null) {
                        traces = traceRepository
                                .findByExecInAndEtTypeAndTimestampGreaterThanAndTimestampLessThanEqual(
                                        indices, etType, gt, lte);
                    } else { // gt only
                        traces = traceRepository
                                .findByExecInAndEtTypeAndTimestampGreaterThan(
                                        indices, etType, gt);
                    }
                }

            } else if (gte != null) {
                // gte and lt
                if (lt != null) {
                    traces = traceRepository
                            .findByExecInAndEtTypeAndTimestampGreaterThanEqualAndTimestampLessThan(
                                    indices, etType, gte, lt);
                } else {
                    // gte and lte
                    if (lte != null) {
                        traces = traceRepository
                                .findByExecInAndEtTypeAndTimestampGreaterThanEqualAndTimestampLessThanEqual(
                                        indices, etType, gte, lte);
                    } else { // gte only
                        traces = traceRepository
                                .findByExecInAndEtTypeAndTimestampGreaterThanEqual(
                                        indices, etType, gte);
                    }
                }
            } else if (lte != null) {
                traces = traceRepository
                        .findByExecInAndEtTypeAndTimestampLessThanEqual(indices,
                                etType, lte);
            } else if (lt != null) {
                traces = traceRepository
                        .findByExecInAndEtTypeAndTimestampLessThan(indices,
                                etType, lt);
            }
        }

        return traces;
    }

    @Override
    public List<Map<String, Object>> getLastMetrics(
            MonitoringQuery monitoringQuery, int size) throws Exception {
        List<Trace> traces = new ArrayList<>();
        if (monitoringQuery.getComponent() != null) {
            traces = traceRepository
                    .findByExecInAndEtTypeAndComponentOrderByIdDesc(
                            monitoringQuery.getIndices(),
                            monitoringQuery.getEtType(),
                            monitoringQuery.getComponent());
        } else {
            traces = traceRepository.findByExecInAndEtTypeOrderByIdDesc(
                    monitoringQuery.getIndices(), monitoringQuery.getEtType());
        }
        if (traces.size() > 0) {
            traces = ListUtils.partition(traces, size).get(0);
            // Sort ASC
            Collections.reverse(traces);
        }

        return this.getTracesMapListByTracesList(traces);
    }

    @Override
    public List<Map<String, Object>> getPreviousMetricsFromTimestamp(
            MonitoringQuery monitoringQuery) throws Exception {
        List<Trace> tracesWithSameTimestamp = new ArrayList<>();
        // Get by timestamp
        if (monitoringQuery.getComponent() != null) {
            tracesWithSameTimestamp = traceRepository
                    .findByExecInAndEtTypeAndComponentAndTimestamp(
                            monitoringQuery.getIndices(),
                            monitoringQuery.getEtType(),
                            monitoringQuery.getComponent(),
                            utilsService.getGMT0DateFromIso8601Str(
                                    monitoringQuery.getTimestamp()));
        } else {
            tracesWithSameTimestamp = traceRepository
                    .findByExecInAndEtTypeAndTimestamp(
                            monitoringQuery.getIndices(),
                            monitoringQuery.getEtType(),
                            utilsService.getGMT0DateFromIso8601Str(
                                    monitoringQuery.getTimestamp()));
        }
        List<Trace> traces = new ArrayList<>();
        if (tracesWithSameTimestamp.size() > 0) {
            // Get previous
            if (monitoringQuery.getComponent() != null) {
                traces = traceRepository
                        .findByExecInAndEtTypeAndComponentAndIdLessThan(
                                monitoringQuery.getIndices(),
                                monitoringQuery.getEtType(),
                                monitoringQuery.getComponent(),
                                tracesWithSameTimestamp.get(0).getId());
            } else {
                traces = traceRepository.findByExecInAndEtTypeAndIdLessThan(
                        monitoringQuery.getIndices(),
                        monitoringQuery.getEtType(),
                        tracesWithSameTimestamp.get(0).getId());
            }
        }
        return this.getTracesMapListByTracesList(traces);
    }

    @Override
    @SuppressWarnings("rawtypes")
    public List<AggregationTree> searchMetricsTree(
            @Valid MonitoringQuery monitoringQuery) throws Exception {
        Iterable<Tuple> treeValues = this.getMonitoringTree(monitoringQuery,
                true);

        Map<String, Map> tmpMetricsTreeMap = new HashMap<>();
        for (Tuple treeValuesTuple : treeValues) {
            if (treeValuesTuple != null) {
                Object[] componentStream = treeValuesTuple.toArray();
                tmpMetricsTreeMap = this.getMapTree(componentStream,
                        tmpMetricsTreeMap);
            }
        }

        return this.getAggTreeList(tmpMetricsTreeMap);
    }

    /* ******************* */
    /* *** LogAnalyzer *** */
    /* ******************* */

    public BooleanExpression getLogAnalyzerQuery(
            LogAnalyzerQuery logAnalyzerQuery) throws ParseException {
        BooleanExpression parentBooleanExpression = null;

        // Components/streams
        BooleanExpression componentStreamQuery = null;
        for (AggregationTree componentStream : logAnalyzerQuery
                .getComponentsStreams()) {
            for (AggregationTree stream : componentStream.getChildren()) {
                BooleanExpression currentComponentStreamQuery = QTrace.trace.component
                        .eq(componentStream.getName())
                        .and(QTrace.trace.stream.eq(stream.getName()));

                if (componentStreamQuery == null) {
                    componentStreamQuery = currentComponentStreamQuery;
                } else {
                    componentStreamQuery = componentStreamQuery
                            .or(currentComponentStreamQuery);
                }
            }
        }

        // Levels
        BooleanExpression levelQuery = null;
        for (String level : logAnalyzerQuery.getLevels()) {

            BooleanExpression currentLevelQuery = QTrace.trace.level
                    .eq(LevelEnum.fromValue(level));

            if (levelQuery == null) {
                levelQuery = currentLevelQuery;
            } else {
                levelQuery = levelQuery.or(currentLevelQuery);
            }
        }

        // Range Time
        BooleanExpression timeRangeQuery = null;
        StringTemplate dateStrTemplate = Expressions.stringTemplate(
                "DATE_FORMAT({0}, {1})", QTrace.trace.timestamp,
                "%Y-%m-%dT%T.%fZ");

        if (logAnalyzerQuery.getSearchBeforeTrace() == null
                || logAnalyzerQuery.getSearchBeforeTrace().isEmpty()) {
            if (logAnalyzerQuery.getRangeLT() != null) {
                timeRangeQuery = dateStrTemplate.lt(utilsService
                        .getStrIso8601With6MillisUTCFromLogAnalyzerDateStr(
                                logAnalyzerQuery.getRangeLT()));
            }
            if (logAnalyzerQuery.getRangeLTE() != null
                    && !logAnalyzerQuery.getRangeLTE().equals("now")) { // now
                                                                        // is
                                                                        // used
                                                                        // for
                                                                        // tail
                BooleanExpression timeRangeQueryLte = dateStrTemplate
                        .loe(utilsService
                                .getStrIso8601With6MillisUTCFromLogAnalyzerDateStr(
                                        logAnalyzerQuery.getRangeLTE()));
                if (timeRangeQuery == null) {
                    timeRangeQuery = timeRangeQueryLte;
                } else {
                    timeRangeQuery = timeRangeQuery.and(timeRangeQueryLte);
                }
            }
        }
        if (logAnalyzerQuery.getSearchAfterTrace() == null
                || logAnalyzerQuery.getSearchAfterTrace().isEmpty()) {

            if (logAnalyzerQuery.getRangeGT() != null) {
                BooleanExpression timeRangeQueryGt = dateStrTemplate
                        .gt(utilsService
                                .getStrIso8601With6MillisUTCFromLogAnalyzerDateStr(
                                        logAnalyzerQuery.getRangeGT()));
                if (timeRangeQuery == null) {
                    timeRangeQuery = timeRangeQueryGt;
                } else {
                    timeRangeQuery = timeRangeQuery.and(timeRangeQueryGt);
                }
            }
            if (logAnalyzerQuery.getRangeGTE() != null) {
                BooleanExpression timeRangeQueryGTE = dateStrTemplate
                        .goe(utilsService
                                .getStrIso8601With6MillisUTCFromLogAnalyzerDateStr(
                                        logAnalyzerQuery.getRangeGTE()));
                if (timeRangeQuery == null) {
                    timeRangeQuery = timeRangeQueryGTE;
                } else {
                    timeRangeQuery = timeRangeQuery.and(timeRangeQueryGTE);
                }
            }
        }
        // Match Message
        BooleanExpression matchMessageQuery = null;
        if (logAnalyzerQuery.getMatchMessage() != null
                && !logAnalyzerQuery.getMatchMessage().equals("")) {
            matchMessageQuery = QTrace.trace.message
                    .contains(logAnalyzerQuery.getMatchMessage());
        }

        // Stream Type
        BooleanExpression streamTypeQuery = QTrace.trace.streamType
                .eq(StreamType.LOG);

        if (componentStreamQuery != null) {
            parentBooleanExpression = componentStreamQuery;
        }

        if (levelQuery != null) {
            if (parentBooleanExpression == null) {
                parentBooleanExpression = levelQuery;
            } else {
                parentBooleanExpression = parentBooleanExpression
                        .and(levelQuery);
            }
        }

        if (timeRangeQuery != null) {
            if (parentBooleanExpression == null) {
                parentBooleanExpression = timeRangeQuery;
            } else {
                parentBooleanExpression = parentBooleanExpression
                        .and(timeRangeQuery);
            }
        }

        if (matchMessageQuery != null) {
            if (parentBooleanExpression == null) {
                parentBooleanExpression = matchMessageQuery;
            } else {
                parentBooleanExpression = parentBooleanExpression
                        .and(matchMessageQuery);
            }
        }

        if (streamTypeQuery != null) {
            if (parentBooleanExpression == null) {
                parentBooleanExpression = streamTypeQuery;
            } else {
                parentBooleanExpression = parentBooleanExpression
                        .and(streamTypeQuery);
            }
        }

        return parentBooleanExpression;

    }

    public List<Map<String, Object>> searchLogAnalyzerQuery(
            LogAnalyzerQuery logAnalyzerQuery)
            throws IOException, ParseException {
        BooleanExpression logAnalyzerQueryPredicate = getLogAnalyzerQuery(
                logAnalyzerQuery);

        // Size
        Pageable sizePageable = PageRequest.of(0, logAnalyzerQuery.getSize(),
                Direction.ASC, "id");

        if (logAnalyzerQuery.getSearchAfterTrace() != null
                && logAnalyzerQuery.getSearchAfterTrace().get("id") != null) {

            Long id = utilsService.convertToLong(
                    logAnalyzerQuery.getSearchAfterTrace().get("id"));

            logAnalyzerQueryPredicate = logAnalyzerQueryPredicate
                    .and(QTrace.trace.id.gt(id));

            if (logAnalyzerQuery.getSearchBeforeTrace() != null
                    && logAnalyzerQuery.getSearchBeforeTrace()
                            .get("id") != null) {

                Long beforeId = utilsService.convertToLong(
                        logAnalyzerQuery.getSearchBeforeTrace().get("id"));
                logAnalyzerQueryPredicate = logAnalyzerQueryPredicate
                        .and(QTrace.trace.id.lt(beforeId));
            }
        }

        logAnalyzerQueryPredicate = logAnalyzerQueryPredicate
                .and(QTrace.trace.exec.in(logAnalyzerQuery.getIndices()));

        return this.getTracesMapListByTracesList(traceRepository
                .findAll(logAnalyzerQueryPredicate, sizePageable).getContent());
    }

    /* *** Utils *** */

    public List<Map<String, Object>> getTracesMapListByTracesList(
            List<Trace> traces) {
        List<Map<String, Object>> tracesAsMapList = new ArrayList<>();

        if (traces != null) {
            for (Trace trace : traces) {
                tracesAsMapList.add(trace.getAsMap());
            }
        }

        return tracesAsMapList;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private Map<String, Map> getMapTree(Object[] treeValuesList,
            Map<String, Map> treeMap) {
        if (treeValuesList.length > 0) {
            String firstFieldValue = "";
            try {
                firstFieldValue = (String) treeValuesList[0];
            } catch (Exception e) {
                firstFieldValue = (String) treeValuesList[0].toString();
            }

            if (!treeMap.containsKey(firstFieldValue)) {
                treeMap.put(firstFieldValue, new HashMap<String, Map>());
            }

            Object[] treeValuesListWithoutFirst = Arrays
                    .copyOfRange(treeValuesList, 1, treeValuesList.length);
            Map<String, Map> subMap = this.getMapTree(
                    treeValuesListWithoutFirst, treeMap.get(firstFieldValue));

            treeMap.put(firstFieldValue, subMap);
        }

        return treeMap;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private List<AggregationTree> getAggTreeList(Map<String, Map> treeMap) {
        List<AggregationTree> aggTreeList = new ArrayList<>();
        for (HashMap.Entry<String, Map> currentMapEntry : treeMap.entrySet()) {
            if (currentMapEntry != null) {
                AggregationTree currentTree = new AggregationTree();
                currentTree.setName(currentMapEntry.getKey());

                Map<String, Map> childrens = currentMapEntry.getValue();

                if (childrens != null && childrens.size() > 0) {
                    currentTree.getChildren()
                            .addAll(this.getAggTreeList(childrens));
                }

                aggTreeList.add(currentTree);
            }
        }

        return aggTreeList;
    }

}
