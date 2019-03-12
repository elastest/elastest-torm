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

import io.elastest.etm.dao.TestSuiteRepository;
import io.elastest.etm.model.AggregationTree;
import io.elastest.etm.model.LogAnalyzerQuery;
import io.elastest.etm.model.MonitoringQuery;
import io.elastest.etm.model.TestCase;
import io.elastest.etm.model.TestSuite;
import io.elastest.etm.model.TimeRange;
import io.elastest.etm.utils.DiffMatchPatch;
import io.elastest.etm.utils.DiffMatchPatch.Diff;
import io.elastest.etm.utils.UtilsService;

public abstract class AbstractMonitoringService {
    protected final Logger logger = getLogger(lookup().lookupClass());
    String processingComparationMsg = "ET-PROCESSING";
    private Map<String, String> comparisonProcessMap = new HashMap<>();

    protected TestSuiteRepository testSuiteRepository;
    protected UtilsService utilsService;
    protected DatabaseSessionManager dbmanager;

    public abstract void createMonitoringIndex(String[] indicesList);

    public abstract boolean deleteMonitoringDataByExec(String exec);

    public boolean deleteMonitoringDataByIndices(List<String> indices) {
        boolean allDeleted = true;
        if (indices != null) {
            for (String index : indices) {
                try {
                    allDeleted = deleteMonitoringDataByExec(index)
                            && allDeleted;
                } catch (Exception e) {
                    logger.error("Error on delete monitoring data exec {}",
                            index);
                    allDeleted = false;
                }
            }
        }

        return allDeleted;

    }

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

    public List<List<String>> searchTestLogsMessage(
            MonitoringQuery monitoringQuery, boolean withTimestamp,
            boolean timeDiff, Long tJobExecId, boolean onlyFailed)
            throws Exception {
        List<List<String>> testsLogs = new ArrayList<>();
        if (tJobExecId != null) {
            List<TestSuite> suites = testSuiteRepository
                    .findByTJobExecId(tJobExecId);

            // If components list not empty, use list. Else, use unique
            // component
            List<String> components = monitoringQuery.getComponents();
            components = components != null && components.size() > 0
                    ? components
                    : Arrays.asList(monitoringQuery.getComponent());

            for (TestSuite suite : suites) {
                if (suite.getTestCases() != null) {
                    for (TestCase currentCase : suite.getTestCases()) {

                        // If all tests or only failed tests
                        if (!onlyFailed
                                || (onlyFailed && currentCase.isFailed())) {

                            List<String> testComponents = components;

                            if (!components.contains("test")) {
                                // Only test has start/finish trace
                                testComponents = Arrays.asList("test");
                            }

                            Date startTestTrace = this
                                    .findFirstStartTestMsgAndGetTimestamp(
                                            monitoringQuery
                                                    .getIndicesAsString(),
                                            currentCase.getName(),
                                            testComponents);
                            Date finishTestTrace = this
                                    .findFirstFinishTestMsgAndGetTimestamp(
                                            monitoringQuery
                                                    .getIndicesAsString(),
                                            currentCase.getName(),
                                            testComponents);

                            if (startTestTrace != null
                                    && finishTestTrace != null) {

                                TimeRange timeRange = new TimeRange();
                                timeRange.setGte(startTestTrace);
                                timeRange.setLte(finishTestTrace);
                                monitoringQuery.setTimeRange(timeRange);

                                List<String> tcLogs = searchAllLogsMessage(
                                        monitoringQuery, withTimestamp,
                                        timeDiff, true);

                                if (tcLogs.size() > 0) {
                                    String completeTestName = "<TEST>: "
                                            + suite.getName() + " -> "
                                            + currentCase.getName();
                                    List<String> aux = new ArrayList<>();
                                    aux.add(completeTestName);
                                    aux.addAll(tcLogs);
                                    tcLogs = aux;
                                }

                                testsLogs.add(tcLogs);
                            }
                        }
                    }
                }
            }
        }
        return testsLogs;
    }

    public String compareLogsPair(MonitoringQuery body, String comparison,
            String view, String timeout) throws Exception {
        if (body != null && body.getIndices() != null
                && body.getIndices().size() == 2) {
            float timeoutFloat = 0;
            try {
                timeoutFloat = Float.parseFloat(timeout);
            } catch (Exception e) {
            }

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

            List<String>[] pairLogs = new List[2];
            int pos = 0;
            for (String index : body.getIndices()) {

                MonitoringQuery newQuery = new MonitoringQuery(body);
                newQuery.setIndices(Arrays.asList(index));

                // List of Tests logs. For "complete" there will only be one
                // item in the list
                List<List<String>> logsList = new ArrayList<>();

                Long tJobExecId = new Long(index);
                if (view != null) {
                    switch (view) {
                    case "failedtests":
                        logsList = searchTestLogsMessage(newQuery,
                                withTimestamp, timeDiff, tJobExecId, true);
                        break;
                    case "testslogs":
                        logsList = searchTestLogsMessage(newQuery,
                                withTimestamp, timeDiff, tJobExecId, false);
                        break;
                    case "complete":
                    default:
                        logsList.add(searchAllLogsMessage(newQuery,
                                withTimestamp, timeDiff));
                        break;
                    }
                }
                if (pos < 2) {
                    pairLogs[pos] = new ArrayList<>();
                    for (List<String> currentLogs : logsList) {
                        // Join with carriage return
                        pairLogs[pos].add(StringUtils.join(currentLogs,
                                String.format("%n")));
                    }
                }
                pos++;
            }

            String htmlComparison = "";
            boolean firstConcat = true;
            if (pairLogs[0].size() == pairLogs[1].size()) {
                pos = 0;
                for (String currentLog : pairLogs[0]) {
                    if (!firstConcat) {
                        htmlComparison += "<br>";
                    } else {
                        firstConcat = false;
                    }
                    htmlComparison += getDiffHtmlFromLogs(currentLog,
                            pairLogs[1].get(pos), timeoutFloat);
                    pos++;
                }
            }
            return htmlComparison;

        }
        return null;
    }

    public String getDiffHtmlFromLogs(String log1, String log2, float timeout) {
        String html = "";
        if (log1 != null && log2 != null) {
            DiffMatchPatch dmp = new DiffMatchPatch();
            dmp.setDiff_Timeout(timeout);
            LinkedList<Diff> diffs = dmp.diffMain(log1, log2);
            dmp.diffCleanupSemantic(diffs);
            html = dmp.diffPrettyHtml(diffs);
        }
        return html;
    }

    @Async
    public void compareLogsPairAsync(MonitoringQuery body, String comparison,
            String view, String timeout, String processId) throws Exception {
        dbmanager.bindSession();
        comparisonProcessMap.put(processId, processingComparationMsg);
        String comparisonString = this.compareLogsPair(body, comparison, view,
                timeout);
        logger.debug("Async comparison with process ID {} ends", processId);
        comparisonProcessMap.put(processId, comparisonString);
        dbmanager.unbindSession();
    }

    // This method consumes the comparison if is available
    public String getComparisonByProcessId(String processId) {
        if (processId == null) {
            return null;
        }
        String comparation = comparisonProcessMap.get(processId);

        // Consume
        if (comparation != null
                && !comparation.equals(processingComparationMsg)) {
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
