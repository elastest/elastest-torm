package io.elastest.etm.api;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;

import com.fasterxml.jackson.annotation.JsonView;

import io.elastest.etm.model.TestCase;
import io.elastest.etm.model.TestCase.BasicTestCase;
import io.elastest.etm.model.TestSuite;
import io.elastest.etm.model.TestSuite.BasicTestSuite;
import io.elastest.etm.service.TestSuiteService;
import io.swagger.annotations.ApiParam;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2017-05-19T13:25:11.074+02:00")

@Controller
public class TestSuiteApiController implements TestSuiteApi {

    private static final Logger logger = LoggerFactory
            .getLogger(TestSuiteApiController.class);

    @Autowired
    private TestSuiteService testSuiteService;

    /* ******************* */
    /* *** Test Suites *** */
    /* ******************* */

    @JsonView(BasicTestSuite.class)
    public ResponseEntity<List<TestSuite>> getTestSuitesByTJobExec(Long tJobId,
            Long execId) {
        List<TestSuite> testSuites = this.testSuiteService
                .getTestSuitesByTJobExec(execId);
        return new ResponseEntity<List<TestSuite>>(testSuites, HttpStatus.OK);
    }

    @JsonView(BasicTestSuite.class)
    public ResponseEntity<TestSuite> getTestSuiteById(
            @ApiParam(value = "Test Suite id.", required = true) @PathVariable("testSuiteId") Long testSuiteId) {
        TestSuite testSuite = this.testSuiteService
                .getTestSuiteById(testSuiteId);
        return new ResponseEntity<TestSuite>(testSuite, HttpStatus.OK);
    }

    /* ****************** */
    /* *** Test Cases *** */
    /* ****************** */
    @JsonView(BasicTestCase.class)
    public ResponseEntity<List<TestCase>> getTestCasesByTestSuite(Long tJobId,
            Long execId, Long testSuiteId) {
        List<TestCase> testCases = this.testSuiteService
                .getTestCasesByTestSuiteId(testSuiteId);
        return new ResponseEntity<List<TestCase>>(testCases, HttpStatus.OK);
    }

    @JsonView(BasicTestCase.class)
    public ResponseEntity<TestCase> getTestCaseById(
            @ApiParam(value = "Test Case id.", required = true) @PathVariable("testCaseId") Long testCaseId) {
        TestCase testCase = this.testSuiteService.getTestCaseById(testCaseId);
        return new ResponseEntity<TestCase>(testCase, HttpStatus.OK);
    }
}
