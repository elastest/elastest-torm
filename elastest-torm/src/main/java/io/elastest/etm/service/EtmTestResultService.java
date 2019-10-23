package io.elastest.etm.service;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.maven.plugins.surefire.report.ReportTestCase;
import org.apache.maven.plugins.surefire.report.ReportTestSuite;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import io.elastest.etm.dao.TJobExecRepository;
import io.elastest.etm.dao.TestCaseRepository;
import io.elastest.etm.dao.TestSuiteRepository;
import io.elastest.etm.model.TJobExecution;
import io.elastest.etm.model.TestCase;
import io.elastest.etm.model.TestSuite;

@Service
public class EtmTestResultService {
    final Logger logger = getLogger(lookup().lookupClass());

    private final TestSuiteRepository testSuiteRepo;
    private final TestCaseRepository testCaseRepo;
    private AbstractMonitoringService monitoringService;
    private final TJobExecRepository tJobExecRepositoryImpl;

    public EtmTestResultService(TestSuiteRepository testSuiteRepo,
            TestCaseRepository testCaseRepo,
            AbstractMonitoringService monitoringService,
            TJobExecRepository tJobExecRepositoryImpl) {
        super();
        this.testSuiteRepo = testSuiteRepo;
        this.testCaseRepo = testCaseRepo;
        this.monitoringService = monitoringService;
        this.tJobExecRepositoryImpl = tJobExecRepositoryImpl;
    }

    public void saveTestResults(List<ReportTestSuite> testSuites,
            TJobExecution tJobExec) {
        logger.info("Saving TJob Execution {} results", tJobExec.getId());

        TestSuite tSuite;
        TestCase tCase;
        if (testSuites != null && testSuites.size() > 0) {
            for (ReportTestSuite reportTestSuite : testSuites) {
                tSuite = new TestSuite();
                tSuite.setTimeElapsed(reportTestSuite.getTimeElapsed());
                tSuite.setErrors(reportTestSuite.getNumberOfErrors());
                tSuite.setFailures(reportTestSuite.getNumberOfFailures());
                tSuite.setFlakes(reportTestSuite.getNumberOfFlakes());
                tSuite.setSkipped(reportTestSuite.getNumberOfSkipped());
                tSuite.setName(reportTestSuite.getName());
                tSuite.setnumTests(reportTestSuite.getNumberOfTests());

                tSuite = testSuiteRepo.save(tSuite);

                for (ReportTestCase reportTestCase : reportTestSuite
                        .getTestCases()) {
                    tCase = new TestCase();
                    tCase.cleanNameAndSet(reportTestCase.getName());
                    tCase.setTime(reportTestCase.getTime());
                    tCase.setFailureDetail(reportTestCase.getFailureDetail());
                    tCase.setFailureErrorLine(
                            reportTestCase.getFailureErrorLine());
                    tCase.setFailureMessage(reportTestCase.getFailureMessage());
                    tCase.setFailureType(reportTestCase.getFailureType());
                    tCase.setTestSuite(tSuite);
                    try {
                        Date startDate = this.monitoringService
                                .findFirstStartTestMsgAndGetTimestamp(
                                        tJobExec.getMonitoringIndex(),
                                        tSuite.getName(), tCase.getName(),
                                        Arrays.asList("test"));
                        tCase.setStartDate(startDate);

                        Date endDate = this.monitoringService
                                .findFirstFinishTestMsgAndGetTimestamp(
                                        tJobExec.getMonitoringIndex(),
                                        tSuite.getName(), tCase.getName(),
                                        Arrays.asList("test"));
                        tCase.setEndDate(endDate);
                    } catch (Exception e) {
                        logger.debug(
                                "Cannot save start/end date for Test Case {}",
                                tCase.getName(), e);
                    }

                    testCaseRepo.save(tCase);
                }

                tSuite.settJobExec(tJobExec);
                tJobExec.getTestSuites()
                        .add(testSuiteRepo.saveAndFlush(tSuite));
            }
        }
    }

}
