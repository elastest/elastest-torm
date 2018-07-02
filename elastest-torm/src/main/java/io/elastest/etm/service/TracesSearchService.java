package io.elastest.etm.service;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.ListUtils;
import org.slf4j.Logger;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.ExampleMatcher.StringMatcher;

import io.elastest.etm.dao.TraceRepository;
import io.elastest.etm.model.AggregationTree;
import io.elastest.etm.model.Enums.StreamType;
import io.elastest.etm.model.LogAnalyzerQuery;
import io.elastest.etm.model.MonitoringQuery;
import io.elastest.etm.model.Trace;

//@Service
public class TracesSearchService implements MonitoringServiceInterface {
    final Logger logger = getLogger(lookup().lookupClass());

    TraceRepository traceRepository;

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

    @Override
    public List<AggregationTree> getMonitoringTree(
            MonitoringQuery monitoringQuery, boolean isMetric)
            throws Exception {
        StreamType log = StreamType.LOG;
        List<String[]> treeValues = new ArrayList<>();
        if (isMetric) {
            // Not Log
            treeValues = traceRepository
                    .findMetricsTreeByStreamTypeAndFieldList(log);
        } else {
            treeValues = traceRepository
                    .findLogsTreeByStreamTypeAndFieldList(log);
        }

        List<AggregationTree> aggregationTreeList = new ArrayList<>();

        for (Object[] treeValue : treeValues) {

            aggregationTreeList.addAll(getAggTreeList(treeValue,
                    monitoringQuery.getSelectedTerms()));
        }
        return aggregationTreeList;
    }

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

    @Override
    public List<Map<String, Object>> searchLogAnalyzerQuery(
            LogAnalyzerQuery logAnalyzerQuery) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Date findFirstMsgAndGetTimestamp(String index, String msg,
            String component) throws Exception {
        // TODO Auto-generated method stub
        return null;
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

    public List<AggregationTree> getAggTreeList(Object[] treeValue,
            List<String> fields) {
        List<AggregationTree> aggTreeList = new ArrayList<>();

        if (fields.size() > 0 && treeValue.length > 0) {
            String fieldValue = (String) treeValue[0];

            if (fieldValue != null) {
                AggregationTree aggObj = new AggregationTree();
                aggObj.setName(fieldValue);

                aggObj.setChildren(this.getAggTreeList(
                        Arrays.copyOfRange(treeValue, 1, treeValue.length),
                        fields.subList(1, fields.size())));
                aggTreeList.add(aggObj);
            }

        }
        return aggTreeList;
    }

}
