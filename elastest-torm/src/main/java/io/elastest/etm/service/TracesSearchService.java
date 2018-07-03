package io.elastest.etm.service;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.collections4.ListUtils;
import org.slf4j.Logger;
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
import com.querydsl.jpa.impl.JPAQueryFactory;

import io.elastest.etm.dao.TraceRepository;
import io.elastest.etm.model.AggregationTree;
import io.elastest.etm.model.Enums.LevelEnum;
import io.elastest.etm.model.Enums.StreamType;
import io.elastest.etm.model.LogAnalyzerQuery;
import io.elastest.etm.model.MonitoringQuery;
import io.elastest.etm.model.QTrace;
import io.elastest.etm.model.Trace;

//@Service
public class TracesSearchService implements MonitoringServiceInterface {
    final Logger logger = getLogger(lookup().lookupClass());

    TraceRepository traceRepository;

    @PersistenceContext
    private EntityManager em;

    public TracesSearchService(TraceRepository traceRepository) {
        this.traceRepository = traceRepository;
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

    @Override
    public List<AggregationTree> getMonitoringTree(
            MonitoringQuery monitoringQuery, boolean isMetric)
            throws Exception {
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);
        List<AggregationTree> aggregationTreeList = new ArrayList<>();

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
            BooleanExpression whereExpression = null;
            if (isMetric) {
                // Not Log
                whereExpression = QTrace.trace.streamType.ne(log);
            } else {
                whereExpression = QTrace.trace.streamType.eq(log);
            }

            treeValues = queryFactory.selectDistinct(selectExpression)
                    .from(QTrace.trace).groupBy(selectExpression)
                    .where(whereExpression).fetch();

            for (Tuple treeValueList : treeValues) {
                if (treeValueList != null) {
                    aggregationTreeList
                            .addAll(getAggTreeList(treeValueList.toArray(),
                                    monitoringQuery.getSelectedTerms()));
                }
            }
        }
        return aggregationTreeList;
    }

    /* ****************************************** */
    /* ****************** Logs ****************** */
    /* ****************************************** */

    @Override
    public List<Map<String, Object>> searchAllLogs(
            MonitoringQuery monitoringQuery) throws Exception {
        List<Trace> traces = traceRepository.findByExecInAndStreamAndComponent(
                monitoringQuery.getIndices(), monitoringQuery.getStream(),
                monitoringQuery.getComponent());

        return this.getTracesMapListByTracesList(traces);
    }

    @Override
    public List<Map<String, Object>> getLastLogs(
            MonitoringQuery monitoringQuery, int size) throws Exception {
        List<Trace> traces = traceRepository
                .findByExecInAndStreamAndComponentOrderByIdDesc(
                        monitoringQuery.getIndices(),
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
                .findByExecInAndStreamAndComponentAndMessageAndTimestamp(
                        monitoringQuery.getIndices(),
                        monitoringQuery.getStream(),
                        monitoringQuery.getComponent(),
                        monitoringQuery.getMessage(),
                        monitoringQuery.getTimestamp());

        List<Trace> traces = new ArrayList<>();
        if (tracesWithSameTimestamp.size() > 0) {
            // Get previous
            traces = traceRepository
                    .findByExecInAndStreamAndComponentAndIdLessThan(
                            monitoringQuery.getIndices(),
                            monitoringQuery.getStream(),
                            monitoringQuery.getComponent(),
                            tracesWithSameTimestamp.get(0).getId());

        }
        return this.getTracesMapListByTracesList(traces);
    }

    /* *** Messages *** */

    public List<Trace> findMessage(String index, String msg, String component)
            throws IOException {
        return traceRepository.findByExecAndMessageAndComponentAndStream(index,
                msg, component, "default_log");
    }

    @Override
    public Date findFirstMsgAndGetTimestamp(String index, String msg,
            String component) throws Exception {
        List<Trace> traces = this.findMessage(index, msg, component);
        logger.debug("traces: {}", traces);
        if (traces != null && traces.size() > 0) {
            Trace firstResult = traces.get(0);
            String timestamp = firstResult.getTimestamp();

            DateFormat df = new SimpleDateFormat(
                    "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            Date date = df.parse(timestamp);

            return date;
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
        if (monitoringQuery.getComponent() != null) {
            traces = traceRepository.findByExecInAndEtTypeAndComponent(
                    monitoringQuery.getIndices(), monitoringQuery.getEtType(),
                    monitoringQuery.getComponent());
        } else {
            traces = traceRepository.findByExecInAndEtType(
                    monitoringQuery.getIndices(), monitoringQuery.getEtType());
        }

        return this.getTracesMapListByTracesList(traces);
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
                            monitoringQuery.getTimestamp());
        } else {
            tracesWithSameTimestamp = traceRepository
                    .findByExecInAndEtTypeAndTimestamp(
                            monitoringQuery.getIndices(),
                            monitoringQuery.getEtType(),
                            monitoringQuery.getTimestamp());
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

    /* ******************* */
    /* *** LogAnalyzer *** */
    /* ******************* */

    public BooleanExpression getLogAnalyzerQuery(
            LogAnalyzerQuery logAnalyzerQuery) {
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
        if (logAnalyzerQuery.getRangeLT() != null) {
            BooleanExpression timeRangeQueryLt = Expressions
                    .stringTemplate("DATE_FORMAT({0}, {1})",
                            QTrace.trace.timestamp, "%Y-%m-%dT%T.%fZ")
                    .lt(logAnalyzerQuery.getRangeLT());
            timeRangeQuery = timeRangeQueryLt;
        }
        if (logAnalyzerQuery.getRangeLTE() != null) {
            BooleanExpression timeRangeQueryLte = Expressions
                    .stringTemplate("DATE_FORMAT({0}, {1})",
                            QTrace.trace.timestamp, "%Y-%m-%dT%T.%fZ")
                    .loe(logAnalyzerQuery.getRangeLTE());
            if (timeRangeQuery == null) {
                timeRangeQuery = timeRangeQueryLte;
            } else {
                timeRangeQuery = timeRangeQuery.and(timeRangeQueryLte);
            }
        }
        if (logAnalyzerQuery.getRangeGT() != null) {
            BooleanExpression timeRangeQueryGt = Expressions
                    .stringTemplate("DATE_FORMAT({0}, {1})",
                            QTrace.trace.timestamp, "%Y-%m-%dT%T.%fZ")
                    .gt(logAnalyzerQuery.getRangeGT());
            if (timeRangeQuery == null) {
                timeRangeQuery = timeRangeQueryGt;
            } else {
                timeRangeQuery = timeRangeQuery.and(timeRangeQueryGt);
            }
        }
        if (logAnalyzerQuery.getRangeGTE() != null) { // TODO gte does not exist
            BooleanExpression timeRangeQueryGTE = Expressions
                    .stringTemplate("DATE_FORMAT({0}, {1})",
                            QTrace.trace.timestamp, "%Y-%m-%dT%T.%fZ")
                    .goe(logAnalyzerQuery.getRangeGTE());
            if (timeRangeQuery == null) {
                timeRangeQuery = timeRangeQueryGTE;
            } else {
                timeRangeQuery = timeRangeQuery.and(timeRangeQueryGTE);
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
            LogAnalyzerQuery logAnalyzerQuery) throws IOException {
        BooleanExpression logAnalyzerQueryPredicate = getLogAnalyzerQuery(
                logAnalyzerQuery);

        // Size
        Pageable sizePageable = PageRequest.of(0, logAnalyzerQuery.getSize(),
                Direction.ASC, "id");

        // Search After TODO
        if (logAnalyzerQuery.getSearchAfterTrace() != null
                && logAnalyzerQuery.getSearchAfterTrace().get("sort") != null) {
            //
            // ArrayList<Object> sort = (ArrayList<Object>) logAnalyzerQuery
            // .getSearchAfterTrace().get("sort");
            //
            // sourceBuilder.searchAfter(sort.toArray());
        }

        logAnalyzerQueryPredicate = logAnalyzerQueryPredicate.and(
                QTrace.trace.exec.in(logAnalyzerQuery.getIndicesAsArray()));

        logger.debug("asd {}", logAnalyzerQueryPredicate);
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

    public List<AggregationTree> getAggTreeList(Object[] treeValueList,
            List<String> fields) {
        List<AggregationTree> aggTreeList = new ArrayList<>();

        if (fields.size() > 0 && treeValueList.length > 0) {
            String fieldValue = "";
            try {
                fieldValue = (String) treeValueList[0];
            } catch (Exception e) {
                fieldValue = (String) treeValueList[0].toString();
            }
            if (fieldValue != null) {
                AggregationTree aggObj = new AggregationTree();
                aggObj.setName(fieldValue);

                aggObj.setChildren(this.getAggTreeList(
                        Arrays.copyOfRange(treeValueList, 1,
                                treeValueList.length),
                        fields.subList(1, fields.size())));
                aggTreeList.add(aggObj);
            }

        }
        return aggTreeList;
    }

}
