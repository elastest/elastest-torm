package io.elastest.etm.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import io.elastest.etm.model.TestCase;
import io.elastest.etm.model.TestSuite;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2017-05-19T13:25:11.074+02:00")

@Api(value = "testsuite")
public interface TestSuiteApi extends EtmApiRoot {
	/*********************/
	/**** Test Suites ****/
	/*********************/
	@ApiOperation(value = "Returns all Test Suites of an Execution", notes = "Returns all Test Suites of an Execution.", response = TestSuite.class, responseContainer = "List", tags = {
			"Test Suite", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Successful operation", response = TestSuite.class, responseContainer = "List"),
			@ApiResponse(code = 404, message = "Resource not found") })
	@RequestMapping(value = "/tjob/{tJobId}/exec/{execId}/testsuite", produces = {
			"application/json" }, method = RequestMethod.GET)
	ResponseEntity<List<TestSuite>> getTestSuitesByTJobExec(
			@ApiParam(value = "TJob Id.", required = true) @PathVariable("tJobId") Long tJobId,
			@ApiParam(value = "TJob Exec Id.", required = true) @PathVariable("execId") Long execId);

	@ApiOperation(value = "Returns a Test Suite", notes = "Returns the Test Suite.", response = TestSuite.class, tags = {
			"Test Suite", })
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Successful operation", response = TestSuite.class),
			@ApiResponse(code = 400, message = "TJob not found.", response = TestSuite.class) })
	@RequestMapping(value = "/tjob/exec/testsuite/{testSuiteId}", produces = {
			"application/json" }, method = RequestMethod.GET)
	ResponseEntity<TestSuite> getTestSuiteById(
			@ApiParam(value = "Test Suite id.", required = true) @PathVariable("testSuiteId") Long testSuiteId);

	/********************/
	/**** Test Cases ****/
	/********************/

	@ApiOperation(value = "Returns all Test Cases of a Test Suite", notes = "Returns all Test Cases of a Test Suite.", response = TestCase.class, responseContainer = "List", tags = {
			"Test Case", })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Successful operation", response = TestCase.class, responseContainer = "List"),
			@ApiResponse(code = 404, message = "Resource not found") })
	@RequestMapping(value = "/tjob/{tJobId}/exec/{execId}/testsuite/{testSuiteId}/testcase", produces = {
			"application/json" }, method = RequestMethod.GET)
	ResponseEntity<List<TestCase>> getTestCasesByTestSuite(
			@ApiParam(value = "TJob id.", required = true) @PathVariable("tJobId") Long tJobId,
			@ApiParam(value = "TJob Exec Id.", required = true) @PathVariable("execId") Long execId,
			@ApiParam(value = "Test Suite id.", required = true) @PathVariable("testSuiteId") Long testSuiteId);

	@ApiOperation(value = "Returns a Test Case", notes = "Returns the Test Case.", response = TestCase.class, tags = {
			"Test Case", })
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Successful operation", response = TestCase.class),
			@ApiResponse(code = 400, message = "TJob not found.", response = TestCase.class) })
	@RequestMapping(value = "/tjob/exec/testsuite/testcase/{testCaseId}", produces = {
			"application/json" }, method = RequestMethod.GET)
	ResponseEntity<TestCase> getTestCaseById(
			@ApiParam(value = "Test Case id.", required = true) @PathVariable("testCaseId") Long testCaseId);

}
