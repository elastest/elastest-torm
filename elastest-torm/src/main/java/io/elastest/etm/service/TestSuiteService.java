package io.elastest.etm.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import org.springframework.stereotype.Service;

import io.elastest.etm.dao.TJobExecRepository;
import io.elastest.etm.dao.TestCaseRepository;
import io.elastest.etm.dao.TestSuiteRepository;
import io.elastest.etm.model.TJobExecution;
import io.elastest.etm.model.TestCase;
import io.elastest.etm.model.TestSuite;

@Service
public class TestSuiteService {
    private final TJobExecRepository tJobExecRepositoryImpl;
    private final TestSuiteRepository testSuiteRepository;
    private final TestCaseRepository testCaseRepository;

    Map<String, Future<Void>> asyncExecs = new HashMap<String, Future<Void>>();

    public TestSuiteService(TJobExecRepository tJobExecRepositoryImpl,
            TestSuiteRepository testSuiteRepository,
            TestCaseRepository testCaseRepository) {
        super();
        this.tJobExecRepositoryImpl = tJobExecRepositoryImpl;
        this.testSuiteRepository = testSuiteRepository;
        this.testCaseRepository = testCaseRepository;
    }

    /* ******************* */
    /* *** Test Suites *** */
    /* ******************* */
    public List<TestSuite> getTestSuitesByTJobExec(Long execId) {
        TJobExecution tJobExec = this.tJobExecRepositoryImpl.findById(execId)
                .get();
        return this.testSuiteRepository.findByTJobExec(tJobExec);
    }

    public TestSuite getTestSuiteById(Long testSuiteId) {
        return this.testSuiteRepository.findById(testSuiteId).get();
    }

    /* ****************** */
    /* *** Test Cases *** */
    /* ****************** */

    public List<TestCase> getTestCasesByTestSuiteId(Long testSuiteId) {
        TestSuite testSuite = this.testSuiteRepository.findById(testSuiteId)
                .get();
        return this.testCaseRepository.findByTestSuite(testSuite);
    }

    public TestCase getTestCaseById(Long testCaseId) {
        return this.testCaseRepository.findById(testCaseId).get();
    }

    public List<TestCase> getTJobExecTestCases(Long tJobExecId) {
        if (tJobExecId != null) {
            List<TestSuite> suites = testSuiteRepository
                    .findByTJobExecId(tJobExecId);

            for (TestSuite suite : suites) {
                if (suite.getTestCases() != null) {
                    return suite.getTestCases();
                }
            }
        }
        return new ArrayList<>();
    }

    public List<TestCase> getFailedTJobExecTestCases(Long tJobExecId) {
        List<TestCase> failedTestCases = new ArrayList<>();
        if (tJobExecId != null) {
            List<TestSuite> suites = testSuiteRepository
                    .findByTJobExecId(tJobExecId);

            for (TestSuite suite : suites) {
                if (suite.getTestCases() != null) {
                    for (TestCase currentCase : suite.getTestCases()) {
                        // If all tests or only failed tests
                        if (currentCase.isFailed()) {
                            currentCase.setTestSuite(suite);
                            failedTestCases.add(currentCase);
                        }
                    }
                }
            }
        }
        return failedTestCases;
    }

    public List<TestCase> getFailedTJobExecsTestCases(List<Long> tJobExecIds) {
        List<TestCase> failedTCases = new ArrayList<>();
        if (tJobExecIds != null) {
            for (Long id : tJobExecIds) {
                List<TestCase> currentFailedTCases = getFailedTJobExecTestCases(
                        id);
                if (currentFailedTCases != null
                        && currentFailedTCases.size() > 0) {
                    failedTCases.addAll(currentFailedTCases);
                }
            }
        }
        return failedTCases;
    }

    public Map<String, List<String>> getFailedTJobExecTestCasesAndSuitesNamesPair(
            Long tJobExecId,
            Map<String, List<String>> failedTCasesAndSuitesNames) {
        if (tJobExecId != null) {
            List<TestSuite> suites = testSuiteRepository
                    .findByTJobExecId(tJobExecId);

            for (TestSuite suite : suites) {
                if (suite.getTestCases() != null) {
                    for (TestCase currentCase : suite.getTestCases()) {
                        // If all tests or only failed tests
                        if (currentCase.isFailed()) {

                            String suiteName = suite.getName();
                            if (!failedTCasesAndSuitesNames
                                    .containsKey(suiteName)) {
                                failedTCasesAndSuitesNames.put(suiteName,
                                        new ArrayList<>());
                            }
                            if (!failedTCasesAndSuitesNames.get(suiteName)
                                    .contains(currentCase.getName())) {
                                failedTCasesAndSuitesNames.get(suiteName)
                                        .add(currentCase.getName());
                            }
                        }
                    }
                }
            }
        }
        return failedTCasesAndSuitesNames;
    }

    public Map<String, List<String>> getFailedTJobExecsTestCasesAndSuitesNamesPair(
            List<Long> tJobExecIds) {
        Map<String, List<String>> failedTCasesAndSuitesNames = new HashMap<>();
        if (tJobExecIds != null) {
            for (Long id : tJobExecIds) {
                failedTCasesAndSuitesNames = getFailedTJobExecTestCasesAndSuitesNamesPair(
                        id, failedTCasesAndSuitesNames);
            }
        }
        return failedTCasesAndSuitesNames;
    }

    public Map<String, List<String>> getFailedTJobExecsTestCasesAndSuitesNamesPairByStrIds(
            List<String> tJobExecIds) {
        List<Long> ids = new ArrayList<>();
        for (String idStr : tJobExecIds) {
            ids.add(new Long(idStr));
        }
        return getFailedTJobExecsTestCasesAndSuitesNamesPair(ids);
    }
}