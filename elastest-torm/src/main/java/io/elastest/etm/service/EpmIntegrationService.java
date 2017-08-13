package io.elastest.etm.service;

import java.util.List;

import org.apache.maven.plugins.surefire.report.ReportTestCase;
import org.apache.maven.plugins.surefire.report.ReportTestSuite;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import io.elastest.etm.dao.TJobExecRepository;
import io.elastest.etm.dao.TestCaseRepository;
import io.elastest.etm.dao.TestSuiteRepository;
import io.elastest.etm.model.TJobExecution;
import io.elastest.etm.model.TestCase;
import io.elastest.etm.model.TestSuite;

@Service
public class EpmIntegrationService {

	@Value("${elastest.elasticsearch.host}")
	private String elasticsearchHost;
	private final DockerService dockerService;
	private final TestSuiteRepository testSuiteRepo;
	private final TestCaseRepository testCaseRepo;

	private final TJobExecRepository tJobExecRepositoryImpl;

	private DatabaseSessionManager dbmanager;

	public EpmIntegrationService(DockerService dockerService, TestSuiteRepository testSuiteRepo,
			TestCaseRepository testCaseRepo, TJobExecRepository tJobExecRepositoryImpl,
			DatabaseSessionManager dbmanager) {
		super();
		this.dockerService = dockerService;
		this.testSuiteRepo = testSuiteRepo;
		this.testCaseRepo = testCaseRepo;
		this.tJobExecRepositoryImpl = tJobExecRepositoryImpl;
		this.dbmanager = dbmanager;
	}

	@Async
	public void executeTJob(TJobExecution tJobExec) {
		dbmanager.bindSession();
		tJobExec = tJobExecRepositoryImpl.findOne(tJobExec.getId());

		DockerExecution dockerExec = new DockerExecution(tJobExec);
		String testLogUrl = dockerExec.initializeLog();

		try {
			// Create queues and load basic services
			dockerService.loadBasicServices(dockerExec);

			List<ReportTestSuite> testSuites;
			// Start Test
			testSuites = dockerService.executeTest(dockerExec);
			tJobExec.setResult(TJobExecution.ResultEnum.FINISHED);
			saveTestResults(testSuites, tJobExec);

			// End and purge services
			dockerService.endAllExec(dockerExec);
		} catch (Exception e) {
			e.printStackTrace();
			if (!e.getMessage().equals("end error")) { // TODO customize
														// exception
				tJobExec.setResult(TJobExecution.ResultEnum.FAILURE);
			}
		}

		// Setting execution data
		tJobExec.setSutExecution(dockerExec.getSutExec());

		// Saving execution data
		tJobExecRepositoryImpl.save(tJobExec);
		dbmanager.unbindSession();
	}

	public void saveTestResults(List<ReportTestSuite> testSuites, TJobExecution tJobExec) {
		System.out.println("Saving test results " + tJobExec.getId());

		TestSuite tSuite;
		TestCase tCase;
		if (testSuites.size() > 0) {
			ReportTestSuite reportTestSuite = testSuites.get(0);
			tSuite = new TestSuite();
			tSuite.setTimeElapsed(reportTestSuite.getTimeElapsed());
			tSuite.setErrors(reportTestSuite.getNumberOfErrors());
			tSuite.setFailures(reportTestSuite.getNumberOfFailures());
			tSuite.setFlakes(reportTestSuite.getNumberOfFlakes());
			tSuite.setSkipped(reportTestSuite.getNumberOfSkipped());
			tSuite.setName(reportTestSuite.getName());
			tSuite.setnumTests(reportTestSuite.getNumberOfTests());

			tSuite = testSuiteRepo.save(tSuite);

			for (ReportTestCase reportTestCase : reportTestSuite.getTestCases()) {
				tCase = new TestCase();
				tCase.setName(reportTestCase.getName());
				tCase.setTime(reportTestCase.getTime());
				tCase.setFailureDetail(reportTestCase.getFailureDetail());
				tCase.setFailureErrorLine(reportTestCase.getFailureErrorLine());
				tCase.setFailureMessage(reportTestCase.getFailureMessage());
				tCase.setFailureType(reportTestCase.getFailureType());
				tCase.setTestSuite(tSuite);

				testCaseRepo.save(tCase);
			}

			testSuiteRepo.save(tSuite);
			tJobExec.setTestSuite(tSuite);
		}

	}

}
