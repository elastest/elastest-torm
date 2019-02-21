package io.elastest.etm.service;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.scheduling.annotation.Async;

import io.elastest.etm.model.AggregationTree;
import io.elastest.etm.model.LogAnalyzerQuery;
import io.elastest.etm.model.MonitoringQuery;
import io.elastest.etm.model.TimeRange;
import io.elastest.etm.utils.DiffMatchPatch;
import io.elastest.etm.utils.UtilsService;
import io.elastest.etm.utils.DiffMatchPatch.Diff;

public abstract class AbstractMonitoringService {
    protected final Logger logger = getLogger(lookup().lookupClass());
    String processingComparationMsg = "ET-PROCESSING";
    private Map<String, String> comparisonProcessMap = new HashMap<>();

    protected UtilsService utilsService;

    public abstract void createMonitoringIndex(String[] indicesList);

    /* *** Logs *** */
    public abstract List<Map<String, Object>> searchAllByTerms(
            MonitoringQuery monitoringQuery) throws Exception;

    public abstract List<Map<String, Object>> searchAllLogs(
            MonitoringQuery monitoringQuery) throws Exception;

    public abstract List<String> searchAllLogsMessage(
            MonitoringQuery monitoringQuery, boolean withTimestamp,
            boolean timeDiff, boolean discardStartFinishTestTraces)
            throws Exception;

    public abstract List<String> searchAllLogsMessage(
            MonitoringQuery monitoringQuery, boolean withTimestamp,
            boolean timeInMillis) throws Exception;

    public List<String> searchTestLogsMessage(MonitoringQuery monitoringQuery,
            boolean withTimestamp, boolean timeDiff) throws Exception {
        // If components list not empty, use list. Else, use unique
        // component
        List<String> components = monitoringQuery.getComponents();
        components = components != null && components.size() > 0 ? components
                : Arrays.asList(monitoringQuery.getComponent());

        Date firstStartTestTrace = this.findFirstStartTestMsgAndGetTimestamp(
                monitoringQuery.getIndicesAsString(), components);
        Date lastFinishTestTrace = this.findLastFinishTestMsgAndGetTimestamp(
                monitoringQuery.getIndicesAsString(), components);

        if (firstStartTestTrace == null && lastFinishTestTrace == null) {
            return new ArrayList<>();
        }

        TimeRange timeRange = new TimeRange();
        timeRange.setGte(firstStartTestTrace);
        timeRange.setLte(lastFinishTestTrace);
        monitoringQuery.setTimeRange(timeRange);

        return searchAllLogsMessage(monitoringQuery, withTimestamp, timeDiff,
                true);
    }

    public String compareLogsPair(MonitoringQuery body, String comparison,
            String view) throws Exception {
        if (body != null && body.getIndices() != null
                && body.getIndices().size() == 2) {

            boolean withTimestamp = false;
            boolean timeDiff = false;

            if (comparison != null) {
                switch (comparison) {
                case "complete":
                    withTimestamp = true;
                    break;
                case "timediff":
                    withTimestamp = true;
                    timeDiff = true;
                    break;
                case "notimestamp":
                default:
                    break;
                }

            }

            String[] pairLogs = new String[2];
            int pos = 0;
            for (String index : body.getIndices()) {

                MonitoringQuery newQuery = new MonitoringQuery(body);
                newQuery.setIndices(Arrays.asList(index));
                List<String> logs = new ArrayList<String>();

                if (view != null) {
                    switch (view) {
                    case "failedtests":
                        break;
                    case "testslogs":
                        logs = searchTestLogsMessage(newQuery, withTimestamp,
                                timeDiff);
                        break;
                    case "complete":
                    default:
                        logs = searchAllLogsMessage(newQuery, withTimestamp,
                                timeDiff);
                        break;
                    }
                }

                if (pos < 2) {
                    // Join with carriage return
                    pairLogs[pos] = StringUtils.join(logs, String.format("%n"));
                }
                pos++;
            }

            if (pairLogs[0] != null && pairLogs[1] != null) {
                DiffMatchPatch dmp = new DiffMatchPatch();
                LinkedList<Diff> diffs = dmp.diffMain(pairLogs[0], pairLogs[1]);
                dmp.diffCleanupSemantic(diffs);
                return dmp.diffPrettyHtml(diffs);
            }

        }
        return null;
    }

    @Async
    public void compareLogsPairAsync(MonitoringQuery body, String comparison,
            String view, String processId) throws Exception {
        comparisonProcessMap.put(processId, processingComparationMsg);
        String comparisonString = this.compareLogsPair(body, comparison, view);
        logger.debug("Async comparison with process ID {} ends", processId);
        comparisonProcessMap.put(processId, comparisonString);
    }

    // This method consumes the comparison if is available
    public String getComparisonByProcessId(String processId) {
        if (processId == null) {
            return null;
        }
        String comparation = comparisonProcessMap.get(processId);

        // Consume
        if (!comparation.equals(processingComparationMsg)) {
            comparisonProcessMap.remove(processId);

        }
        return comparation;
    }

    public abstract List<Map<String, Object>> getLastLogs(
            MonitoringQuery monitoringQuery, int size) throws Exception;

    public abstract List<Map<String, Object>> getPreviousLogsFromTimestamp(
            MonitoringQuery monitoringQuery) throws Exception;

    public abstract List<AggregationTree> searchLogsTree(
            @Valid MonitoringQuery monitoringQuery) throws Exception;

    public abstract List<AggregationTree> searchLogsLevelsTree(
            @Valid MonitoringQuery monitoringQuery) throws Exception;

    public abstract Date findFirstMsgAndGetTimestamp(String index, String msg,
            List<String> components) throws Exception;

    public Date findFirstStartTestMsgAndGetTimestamp(String index,
            String testName, List<String> components) throws Exception {
        return this.findFirstMsgAndGetTimestamp(index,
                utilsService.getETTestStartPrefix() + testName, components);
    }

    public Date findFirstFinishTestMsgAndGetTimestamp(String index,
            String testName, List<String> components) throws Exception {
        return this.findFirstMsgAndGetTimestamp(index,
                utilsService.getETTestFinishPrefix() + testName, components);
    }

    public Date findFirstStartTestMsgAndGetTimestamp(String index,
            List<String> components) throws Exception {
        return this.findFirstMsgAndGetTimestamp(index,
                utilsService.getETTestStartPrefix(), components);
    }

    public Date findFirstFinishTestMsgAndGetTimestamp(String index,
            List<String> components) throws Exception {
        return this.findFirstMsgAndGetTimestamp(index,
                utilsService.getETTestFinishPrefix(), components);
    }

    public abstract Date findLastMsgAndGetTimestamp(String index, String msg,
            List<String> components) throws Exception;

    public Date findLastStartTestMsgAndGetTimestamp(String index,
            List<String> components) throws Exception {
        return this.findLastMsgAndGetTimestamp(index,
                utilsService.getETTestStartPrefix(), components);
    }

    public Date findLastFinishTestMsgAndGetTimestamp(String index,
            List<String> components) throws Exception {
        return this.findLastMsgAndGetTimestamp(index,
                utilsService.getETTestFinishPrefix(), components);
    }

    /* *** Metrics *** */
    public abstract List<Map<String, Object>> searchAllMetrics(
            MonitoringQuery monitoringQuery) throws Exception;

    public abstract List<Map<String, Object>> getLastMetrics(
            MonitoringQuery monitoringQuery, int size) throws Exception;

    public abstract List<Map<String, Object>> getPreviousMetricsFromTimestamp(
            MonitoringQuery monitoringQuery) throws Exception;

    public abstract List<AggregationTree> searchMetricsTree(
            @Valid MonitoringQuery monitoringQuery) throws Exception;

    /* *** Log Analyzer *** */

    public abstract List<Map<String, Object>> searchLogAnalyzerQuery(
            LogAnalyzerQuery logAnalyzerQuery) throws Exception;

}
