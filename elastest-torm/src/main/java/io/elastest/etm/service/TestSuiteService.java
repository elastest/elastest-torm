package io.elastest.etm.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import io.elastest.etm.dao.TJobExecRepository;
import io.elastest.etm.dao.TestCaseRepository;
import io.elastest.etm.dao.TestSuiteRepository;
import io.elastest.etm.model.TJobExecution;
import io.elastest.etm.model.TestCase;
import io.elastest.etm.model.TestSuite;

@Service
public class TestSuiteService {
	private static final Logger logger = LoggerFactory.getLogger(TestSuiteService.class);

	private final TJobExecRepository tJobExecRepositoryImpl;
	private final TestSuiteRepository testSuiteRepository;
	private final TestCaseRepository testCaseRepository;

	Map<String, Future<Void>> asyncExecs = new HashMap<String, Future<Void>>();

	public TestSuiteService(TJobExecRepository tJobExecRepositoryImpl, TestSuiteRepository testSuiteRepository,
			TestCaseRepository testCaseRepository) {
		super();
		this.tJobExecRepositoryImpl = tJobExecRepositoryImpl;
		this.testSuiteRepository = testSuiteRepository;
		this.testCaseRepository = testCaseRepository;
	}

	/*********************/
	/**** Test Suites ****/
	/*********************/
	public List<TestSuite> getTestSuitesByTJobExec(Long execId) {
		TJobExecution tJobExec = this.tJobExecRepositoryImpl.findOne(execId);
		return this.testSuiteRepository.findByTJobExec(tJobExec);
	}

	public TestSuite getTestSuiteById(Long testSuiteId) {
		return this.testSuiteRepository.findOne(testSuiteId);
	}

	/********************/
	/**** Test Cases ****/
	/********************/

	public List<TestCase> getTestCasesByTestSuiteId(Long testSuiteId) {
		TestSuite testSuite = this.testSuiteRepository.findOne(testSuiteId);
		return this.testCaseRepository.findByTestSuite(testSuite);
	}

	public TestCase getTestCaseById(Long testCaseId) {
		return this.testCaseRepository.findOne(testCaseId);
	}
}