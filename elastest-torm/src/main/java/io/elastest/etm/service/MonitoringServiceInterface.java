package io.elastest.etm.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import io.elastest.etm.model.AggregationTree;
import io.elastest.etm.model.LogAnalyzerQuery;
import io.elastest.etm.model.MonitoringQuery;

public interface MonitoringServiceInterface {

    void createMonitoringIndex(String[] indicesList);

    /* Search */
    List<Map<String, Object>> searchAllByTerms(MonitoringQuery monitoringQuery)
            throws Exception;

    List<Map<String, Object>> searchAllLogs(MonitoringQuery monitoringQuery)
            throws Exception;

    List<Map<String, Object>> getLastLogs(MonitoringQuery monitoringQuery,
            int size) throws Exception;

    List<Map<String, Object>> getPreviousLogsFromTimestamp(
            MonitoringQuery monitoringQuery) throws Exception;

    List<AggregationTree> getMonitoringTree(MonitoringQuery monitoringQuery,
            boolean isMetric) throws Exception;

    List<Map<String, Object>> searchAllMetrics(MonitoringQuery monitoringQuery)
            throws Exception;

    List<Map<String, Object>> getLastMetrics(MonitoringQuery monitoringQuery,
            int size) throws Exception;

    List<Map<String, Object>> getPreviousMetricsFromTimestamp(
            MonitoringQuery monitoringQuery) throws Exception;

    List<Map<String, Object>> searchLogAnalyzerQuery(
            LogAnalyzerQuery logAnalyzerQuery) throws Exception;

    Date findFirstMsgAndGetTimestamp(String index, String msg, String component)
            throws Exception;

    Date findFirstStartTestMsgAndGetTimestamp(String index, String testName,
            String component) throws Exception;

    Date findFirstFinishTestMsgAndGetTimestamp(String index, String testName,
            String component) throws Exception;

}
