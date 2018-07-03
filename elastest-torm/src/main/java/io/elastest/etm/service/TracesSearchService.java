package io.elastest.etm.service;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
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
import com.querydsl.core.types.dsl.StringTemplate;
import com.querydsl.jpa.impl.JPAQueryFactory;

import io.elastest.etm.dao.TraceRepository;
import io.elastest.etm.model.AggregationTree;
import io.elastest.etm.model.Enums.LevelEnum;
import io.elastest.etm.model.Enums.StreamType;
import io.elastest.etm.model.LogAnalyzerQuery;
import io.elastest.etm.model.MonitoringQuery;
import io.elastest.etm.model.QTrace;
import io.elastest.etm.model.Trace;
import io.elastest.etm.utils.UtilsService;

//@Service
public class TracesSearchService implements MonitoringServiceInterface {
    final Logger logger = getLogger(lookup().lookupClass());

    TraceRepository traceRepository;
    UtilsService utilsService;

    @PersistenceContext
    private EntityManager em;

    JPAQueryFactory queryFactory;

    public TracesSearchService(TraceRepository traceRepository,
            UtilsService utilsService) {
        this.traceRepository = traceRepository;
        this.utilsService = utilsService;
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

    @Override
    public List<AggregationTree> getMonitoringTree(
            MonitoringQuery monitoringQuery, boolean isMetric)
            throws Exception {
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
        // logger.debug("findMessage=> index: {}, msg: {}, component: {}",
        // index,
        // msg, component);
        BooleanExpression query = QTrace.trace.exec.eq(index)
                .and(QTrace.trace.component.eq(component))
                .and(QTrace.trace.stream.eq("default_log"))
                .and(QTrace.trace.message.like( // TODO does not works and i
                                                // can not understand
                        Expressions.asString("%").concat(msg).concat("%")));

        return queryFactory.select(QTrace.trace).from(QTrace.trace).where(query)
                .fetch();
    }

    @Override
    public Date findFirstMsgAndGetTimestamp(String index, String msg,
            String component) throws Exception {
        List<Trace> traces = this.findMessage(index, msg, component);
        // logger.debug("traces: {}", traces);
        if (traces != null && traces.size() > 0) {
            Trace firstResult = traces.get(0);
            String timestamp = firstResult.getTimestamp();

            Date date = utilsService.getIso8061GMTTimestampDate(timestamp);

            return date;
        }

        return null;
    }

    @Override
    public Date findFirstStartTestMsgAndGetTimestamp(String index,
            String testName, String component) throws Exception {
        return this.findFirstMsgAndGetTimestamp(index,
                utilsService.getETTestStartPrefix() + testName, component);
    }

    @Override
    public Date findFirstFinishTestMsgAndGetTimestamp(String index,
            String testName, String component) throws Exception {
        return this.findFirstMsgAndGetTimestamp(index,
                utilsService.getETTestFinishPrefix() + testName, component);
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
        StringTemplate dateStrTemplate = Expressions.stringTemplate(
                "DATE_FORMAT({0}, {1})", QTrace.trace.timestamp,
                "%Y-%m-%dT%T.%fZ");
        if (logAnalyzerQuery.getRangeLT() != null) {
            timeRangeQuery = dateStrTemplate.lt(logAnalyzerQuery.getRangeLT());
        }
        if (logAnalyzerQuery.getRangeLTE() != null) {

            BooleanExpression timeRangeQueryLte = dateStrTemplate
                    .loe(logAnalyzerQuery.getRangeLTE());
            if (timeRangeQuery == null) {
                timeRangeQuery = timeRangeQueryLte;
            } else {
                timeRangeQuery = timeRangeQuery.and(timeRangeQueryLte);
            }
        }
        if (logAnalyzerQuery.getRangeGT() != null) {
            BooleanExpression timeRangeQueryGt = dateStrTemplate
                    .gt(logAnalyzerQuery.getRangeGT());
            if (timeRangeQuery == null) {
                timeRangeQuery = timeRangeQueryGt;
            } else {
                timeRangeQuery = timeRangeQuery.and(timeRangeQueryGt);
            }
        }
        if (logAnalyzerQuery.getRangeGTE() != null) {
            BooleanExpression timeRangeQueryGTE = dateStrTemplate
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