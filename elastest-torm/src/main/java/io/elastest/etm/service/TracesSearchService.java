package io.elastest.etm.service;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;

import io.elastest.etm.dao.TraceRepository;
import io.elastest.etm.model.AggregationTree;
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
        // TODO
        return null;

    }

    @Override
    public List<Map<String, Object>> searchAllLogs(
            MonitoringQuery monitoringQuery) throws Exception {
        List<Map<String, Object>> tracesList = new ArrayList<>();
        if (monitoringQuery.getIndices() != null
                && monitoringQuery.getStream() != null
                && monitoringQuery.getComponent() != null) {
            List<Trace> traces = traceRepository
                    .findByExecInAndStreamAndComponent(
                            monitoringQuery.getIndices(),
                            monitoringQuery.getStream(),
                            monitoringQuery.getComponent());

            for (Trace trace : traces) {
                tracesList.add(trace.getAsMap());
            }
        }
        return tracesList;
    }

    @Override
    public List<Map<String, Object>> getPreviousLogsFromTimestamp(
            MonitoringQuery monitoringQuery) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Map<String, Object>> getLastLogs(
            MonitoringQuery monitoringQuery, int size) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<AggregationTree> getMonitoringTree(
            MonitoringQuery monitoringQuery, boolean isMetric)
            throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Map<String, Object>> searchAllMetrics(
            MonitoringQuery monitoringQuery) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Map<String, Object>> getPreviousMetricsFromTimestamp(
            MonitoringQuery monitoringQuery) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Map<String, Object>> getLastMetrics(
            MonitoringQuery monitoringQuery, int size) throws Exception {
        // TODO Auto-generated method stub
        return null;
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

}
