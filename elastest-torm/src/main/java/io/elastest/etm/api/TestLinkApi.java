package io.elastest.etm.api;

import javax.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import br.eti.kinoshita.testlinkjavaapi.model.TestCase;
import br.eti.kinoshita.testlinkjavaapi.model.TestPlan;
import br.eti.kinoshita.testlinkjavaapi.model.TestProject;
import br.eti.kinoshita.testlinkjavaapi.model.TestSuite;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2017-10-03T12:35:11.074+02:00")

@Api(value = "/testlink")
public interface TestLinkApi extends EtmApiRoot {

    /* ************************************************************************/
    /* **************************** Test Projects *****************************/
    /* ************************************************************************/

    @ApiOperation(value = "Returns all Test Projects", notes = "Returns the test projects.", response = TestProject.class, responseContainer = "List", tags = {
            "TestLink", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = TestProject.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Resources not found") })
    @RequestMapping(value = "/testlink/project", method = RequestMethod.GET)
    ResponseEntity<TestProject[]> getAllTestProjects();

    @ApiOperation(value = "Returns a Test Project", notes = "Returns a test project.", response = TestProject.class, tags = {
            "TestLink", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = TestProject.class),
            @ApiResponse(code = 404, message = "Resources not found") })
    @RequestMapping(value = "/testlink/project/{projectName}", method = RequestMethod.GET)
    ResponseEntity<TestProject> getProjectByName(
            @ApiParam(value = "Name of the project.", required = true) @PathVariable("projectName") String projectName);

    @ApiOperation(value = "Creates a new Test Project", notes = "Creates a new Test Project", response = TestProject.class, tags = {
            "TestLink", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = TestProject.class),
            @ApiResponse(code = 405, message = "Invalid input") })
    @RequestMapping(value = "/testlink/project", produces = {
            "application/json" }, consumes = {
                    "application/json" }, method = RequestMethod.POST)
    ResponseEntity<TestProject> createProject(
            @ApiParam(value = "Object with the test project data to create.", required = true) @Valid @RequestBody TestProject body);

    /* ***********************************************************************/
    /* ***************************** Test Plans ******************************/
    /* ***********************************************************************/

    @ApiOperation(value = "Returns all test plans of a project", notes = "Returns all test plans of a project.", response = TestPlan.class, responseContainer = "List", tags = {
            "TestLink", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = TestPlan.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Resources not found") })
    @RequestMapping(value = "/testlink/project/{id}/plan", method = RequestMethod.GET)
    ResponseEntity<TestPlan[]> getProjectTestPlans(
            @ApiParam(value = "ID of the project.", required = true) @PathVariable("id") Integer id);

    @ApiOperation(value = "Returns a Test plan", notes = "Returns a test plan by given name and project name.", response = TestPlan.class, tags = {
            "TestLink", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = TestPlan.class),
            @ApiResponse(code = 404, message = "Resources not found") })
    @RequestMapping(value = "/testlink/project/{projectName}/plan/{planName}", method = RequestMethod.GET)
    ResponseEntity<TestPlan> getPlanByName(
            @ApiParam(value = "Name of the project.", required = true) @PathVariable("projectName") String projectName,
            @ApiParam(value = "Name of the plan.", required = true) @PathVariable("planName") String planName);

    @ApiOperation(value = "Creates a new Test Plan", notes = "Creates a new Test Plan", response = TestPlan.class, tags = {
            "TestLink", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = TestPlan.class),
            @ApiResponse(code = 405, message = "Invalid input") })
    @RequestMapping(value = "/testlink/project/{projectId}/plan/", produces = {
            "application/json" }, consumes = {
                    "application/json" }, method = RequestMethod.POST)
    ResponseEntity<TestPlan> createPlan(
            @ApiParam(value = "Object with the Test Plan data to create.", required = true) @Valid @RequestBody TestPlan body);

    /* ************************************************************************/
    /* ***************************** Test Suites ******************************/
    /* ************************************************************************/
    @ApiOperation(value = "Returns a Test Suite", notes = "Returns a test suite.", response = TestSuite.class, tags = {
            "TestLink", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = TestSuite.class),
            @ApiResponse(code = 404, message = "Resources not found") })
    @RequestMapping(value = "/testlink/project/{projectId}/suite/{suiteid}", method = RequestMethod.GET)
    ResponseEntity<TestSuite> getTestSuiteById(
            @ApiParam(value = "Id of Test suite.", required = true) @PathVariable("suiteId") Integer suiteId);

    @ApiOperation(value = "Returns the Test Suites of a Test Project", notes = "Returns the Test Suites of a Test Project.", response = TestSuite.class, responseContainer = "List", tags = {
            "TestLink", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = TestSuite.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Resources not found") })
    @RequestMapping(value = "/testlink/project/{projectId}/suite/", method = RequestMethod.GET)
    ResponseEntity<TestSuite[]> getProjectTestSuites(
            @ApiParam(value = "Id of Test Project.", required = true) @PathVariable("projectId") Integer projectId);

    @ApiOperation(value = "Creates a new Test Suite", notes = "Creates a new Test Suite", response = TestSuite.class, tags = {
            "TestLink", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = TestSuite.class),
            @ApiResponse(code = 405, message = "Invalid input") })
    @RequestMapping(value = "/testlink/project/{projectId}/suite/", produces = {
            "application/json" }, consumes = {
                    "application/json" }, method = RequestMethod.POST)
    ResponseEntity<TestSuite> createSuite(
            @ApiParam(value = "Object with the Test Suite data to create.", required = true) @Valid @RequestBody TestSuite body);

    /* ***********************************************************************/
    /* ***************************** Test Cases ******************************/
    /* ***********************************************************************/

    @ApiOperation(value = "Returns a Test Case", notes = "Returns a test case.", response = TestCase.class, tags = {
            "TestLink", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = TestCase.class),
            @ApiResponse(code = 404, message = "Resources not found") })
    @RequestMapping(value = "/testlink/project/{projectId}/suite/{suiteid}/case/{caseId}", method = RequestMethod.GET)
    ResponseEntity<TestCase> getTestcase(
            @ApiParam(value = "Id of Test Suite.", required = true) @PathVariable("suiteId") Integer suiteId,
            @ApiParam(value = "Id of Test case.", required = true) @PathVariable("caseId") Integer caseId);

    @ApiOperation(value = "Returns the Test Cases of a Test Suite", notes = "Returnsthe Test Cases of a Test Suite", response = TestCase.class, responseContainer = "List", tags = {
            "TestLink", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = TestCase.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Resources not found") })
    @RequestMapping(value = "/testlink/project/{projectId}/suite/{suiteid}/case", method = RequestMethod.GET)
    ResponseEntity<TestCase[]> getSuiteTestCases(
            @ApiParam(value = "Id of Test Suite.", required = true) @PathVariable("suiteId") Integer suiteId);

    @ApiOperation(value = "Creates a new Test Case", notes = "Creates a new Test Suite", response = TestSuite.class, tags = {
            "TestLink", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = TestCase.class),
            @ApiResponse(code = 405, message = "Invalid input") })
    @RequestMapping(value = "/testlink/project/{projectId}/suite/case", produces = {
            "application/json" }, consumes = {
                    "application/json" }, method = RequestMethod.POST)
    ResponseEntity<TestCase> createTestCase(
            @ApiParam(value = "Object with the Test Case data to create.", required = true) @Valid @RequestBody TestCase body);

}
