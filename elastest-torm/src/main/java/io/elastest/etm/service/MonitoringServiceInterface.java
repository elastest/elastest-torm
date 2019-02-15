package io.elastest.etm.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import io.elastest.etm.model.AggregationTree;
import io.elastest.etm.model.LogAnalyzerQuery;
import io.elastest.etm.model.MonitoringQuery;

public interface MonitoringServiceInterface {

    void createMonitoringIndex(String[] indicesList);

    /* *** Logs *** */
    List<Map<String, Object>> searchAllByTerms(MonitoringQuery monitoringQuery)
            throws Exception;

    List<Map<String, Object>> searchAllLogs(MonitoringQuery monitoringQuery)
            throws Exception;

    List<String> searchAllLogsMessage(MonitoringQuery monitoringQuery,
            boolean withTimestamp, boolean timeDiff,
            boolean discardStartFinishTestTraces) throws Exception;

    List<String> searchAllLogsMessage(MonitoringQuery monitoringQuery,
            boolean withTimestamp, boolean timeInMillis) throws Exception;

    List<String> searchTestLogsMessage(MonitoringQuery monitoringQuery,
            boolean withTimestamp, boolean timeDiff) throws Exception;

    List<Map<String, Object>> getLastLogs(MonitoringQuery monitoringQuery,
            int size) throws Exception;

    List<Map<String, Object>> getPreviousLogsFromTimestamp(
            MonitoringQuery monitoringQuery) throws Exception;

    List<AggregationTree> searchLogsTree(@Valid MonitoringQuery monitoringQuery)
            throws Exception;

    List<AggregationTree> searchLogsLevelsTree(
            @Valid MonitoringQuery monitoringQuery) throws Exception;

    Date findFirstMsgAndGetTimestamp(String index, String msg, List<String> components)
            throws Exception;

    Date findFirstStartTestMsgAndGetTimestamp(String index, String testName,
            List<String> components) throws Exception;

    Date findFirstFinishTestMsgAndGetTimestamp(String index, String testName,
            List<String> components) throws Exception;

    Date findFirstStartTestMsgAndGetTimestamp(String index, List<String> components)
            throws Exception;

    Date findFirstFinishTestMsgAndGetTimestamp(String index, List<String> components)
            throws Exception;

    Date findLastMsgAndGetTimestamp(String index, String msg, List<String> components)
            throws Exception;

    Date findLastStartTestMsgAndGetTimestamp(String index, List<String> components)
            throws Exception;

    Date findLastFinishTestMsgAndGetTimestamp(String index, List<String> components)
            throws Exception;

    /* *** Metrics *** */
    List<Map<String, Object>> searchAllMetrics(MonitoringQuery monitoringQuery)
            throws Exception;

    List<Map<String, Object>> getLastMetrics(MonitoringQuery monitoringQuery,
            int size) throws Exception;

    List<Map<String, Object>> getPreviousMetricsFromTimestamp(
            MonitoringQuery monitoringQuery) throws Exception;

    List<AggregationTree> searchMetricsTree(
            @Valid MonitoringQuery monitoringQuery) throws Exception;

    /* *** Log Analyzer *** */

    List<Map<String, Object>> searchLogAnalyzerQuery(
            LogAnalyzerQuery logAnalyzerQuery) throws Exception;

}
