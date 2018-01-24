package io.elastest.etm.api;

import javax.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import br.eti.kinoshita.testlinkjavaapi.model.Build;
import br.eti.kinoshita.testlinkjavaapi.model.Execution;
import br.eti.kinoshita.testlinkjavaapi.model.ReportTCResultResponse;
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

    @ApiOperation(value = "Returns a Test Project By Given Name", notes = "Returns a Test Project By Given Name.", response = TestProject.class, tags = {
            "TestLink", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = TestProject.class),
            @ApiResponse(code = 404, message = "Resources not found") })
    @RequestMapping(value = "/testlink/project/name/{projectName}", method = RequestMethod.GET)
    ResponseEntity<TestProject> getProjectByName(
            @ApiParam(value = "Name of the project.", required = true) @PathVariable("projectName") String projectName);

    @ApiOperation(value = "Returns a Test Project By Given Id", notes = "Returns a Test Project By Given Id.", response = TestProject.class, tags = {
            "TestLink", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = TestProject.class),
            @ApiResponse(code = 404, message = "Resources not found") })
    @RequestMapping(value = "/testlink/project/{projectId}", method = RequestMethod.GET)
    ResponseEntity<TestProject> getProjectById(
            @ApiParam(value = "ID of the project.", required = true) @PathVariable("projectId") Integer projectId);

    @ApiOperation(value = "Creates a new Test Project", notes = "Creates a new Test Project", response = TestProject.class, tags = {
            "TestLink", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = TestProject.class),
            @ApiResponse(code = 405, message = "Invalid input"),
            @ApiResponse(code = 409, message = "Already exist") })
    @RequestMapping(value = "/testlink/project", produces = {
            "application/json" }, consumes = {
                    "application/json" }, method = RequestMethod.POST)
    ResponseEntity<TestProject> createProject(
            @ApiParam(value = "Object with the test project data to create.", required = true) @Valid @RequestBody TestProject body);

    /* ************************************************************************/
    /* ***************************** Test Suites ******************************/
    /* ************************************************************************/
    @ApiOperation(value = "Returns a Test Suite", notes = "Returns a test suite.", response = TestSuite.class, tags = {
            "TestLink", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = TestSuite.class),
            @ApiResponse(code = 404, message = "Resources not found") })
    @RequestMapping(value = "/testlink/project/{projectId}/suite/{suiteId}", method = RequestMethod.GET)
    ResponseEntity<TestSuite> getTestSuiteById(
            @ApiParam(value = "ID of the project.", required = true) @PathVariable("projectId") Integer projectId,
            @ApiParam(value = "Id of Test suite.", required = true) @PathVariable("suiteId") Integer suiteId);

    @ApiOperation(value = "Returns the Test Suites of a Test Project", notes = "Returns the Test Suites of a Test Project.", response = TestSuite.class, responseContainer = "List", tags = {
            "TestLink", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = TestSuite.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Resources not found") })
    @RequestMapping(value = "/testlink/project/{projectId}/suite", method = RequestMethod.GET)
    ResponseEntity<TestSuite[]> getProjectTestSuites(
            @ApiParam(value = "ID of the Test Project.", required = true) @PathVariable("projectId") Integer projectId);

    @ApiOperation(value = "Creates a new Test Suite", notes = "Creates a new Test Suite", response = TestSuite.class, tags = {
            "TestLink", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = TestSuite.class),
            @ApiResponse(code = 405, message = "Invalid input"),
            @ApiResponse(code = 409, message = "Already exist") })
    @RequestMapping(value = "/testlink/project/{projectId}/suite", produces = {
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
    @RequestMapping(value = "/testlink/project/suite/case/{caseId}", method = RequestMethod.GET)
    ResponseEntity<TestCase> getTestcase(
            @ApiParam(value = "Id of Test case.", required = true) @PathVariable("caseId") Integer caseId);

    @ApiOperation(value = "Returns the Test Cases of a Test Suite", notes = "Returnsthe Test Cases of a Test Suite", response = TestCase.class, responseContainer = "List", tags = {
            "TestLink", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = TestCase.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Resources not found") })
    @RequestMapping(value = "/testlink/project/{projectId}/suite/{suiteId}/case", method = RequestMethod.GET)
    ResponseEntity<TestCase[]> getSuiteTestCases(
            @ApiParam(value = "Id of Test Suite.", required = true) @PathVariable("suiteId") Integer suiteId);

    @ApiOperation(value = "Returns the Test Cases of a Test Plan", notes = "Returnsthe Test Cases of a Test Plan", response = TestCase.class, responseContainer = "List", tags = {
            "TestLink", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = TestCase.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Resources not found") })
    @RequestMapping(value = "/testlink/project/{projectId}/plan/{planId}/case", method = RequestMethod.GET)
    ResponseEntity<TestCase[]> getPlanTestCases(
            @ApiParam(value = "Id of Test Plan.", required = true) @PathVariable("planId") Integer planId);

    @ApiOperation(value = "Creates a new Test Case", notes = "Creates a new Test Suite", response = TestSuite.class, tags = {
            "TestLink", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = TestCase.class),
            @ApiResponse(code = 405, message = "Invalid input"),
            @ApiResponse(code = 409, message = "Already exist") })
    @RequestMapping(value = "/testlink/project/{projectId}/suite/{suiteId}/case", produces = {
            "application/json" }, consumes = {
                    "application/json" }, method = RequestMethod.POST)
    ResponseEntity<TestCase> createTestCase(
            @ApiParam(value = "ID of the project.", required = true) @PathVariable("projectId") Integer projectId,
            @ApiParam(value = "Id of Test Suite.", required = true) @PathVariable("suiteId") Integer suiteId,
            @ApiParam(value = "Object with the Test Case data to create.", required = true) @Valid @RequestBody TestCase body);

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
    @RequestMapping(value = "/testlink/project/{projectName}/plan/name/{planName}", method = RequestMethod.GET)
    ResponseEntity<TestPlan> getPlanByName(
            @ApiParam(value = "Name of the project.", required = true) @PathVariable("projectName") String projectName,
            @ApiParam(value = "Name of the plan.", required = true) @PathVariable("planName") String planName);

    @ApiOperation(value = "Returns a Test plan By Given Id", notes = "Returns a test plan by given id.", response = TestPlan.class, tags = {
            "TestLink", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = TestPlan.class),
            @ApiResponse(code = 404, message = "Resources not found") })
    @RequestMapping(value = "/testlink/project/plan/{planId}", method = RequestMethod.GET)
    ResponseEntity<TestPlan> getPlanById(
            @ApiParam(value = "Id of the plan.", required = true) @PathVariable("planId") Integer planId);

    @ApiOperation(value = "Creates a new Test Plan", notes = "Creates a new Test Plan", response = TestPlan.class, tags = {
            "TestLink", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = TestPlan.class),
            @ApiResponse(code = 405, message = "Invalid input"),
            @ApiResponse(code = 409, message = "Already exist") })
    @RequestMapping(value = "/testlink/project/{projectId}/plan", produces = {
            "application/json" }, consumes = {
                    "application/json" }, method = RequestMethod.POST)
    ResponseEntity<TestPlan> createPlan(
            @ApiParam(value = "Object with the Test Plan data to create.", required = true) @Valid @RequestBody TestPlan body);

    /* *********************************************************************/
    /* ****************************** Builds *******************************/
    /* *********************************************************************/

    @ApiOperation(value = "Returns all builds of a Test Plan", notes = "Returns all builds of a Test Plan.", response = Build.class, responseContainer = "List", tags = {
            "TestLink", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = Build.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Resources not found") })
    @RequestMapping(value = "/testlink/project/{projectName}/plan/{planId}/build", method = RequestMethod.GET)
    ResponseEntity<Build[]> getPlanBuilds(
            @ApiParam(value = "Name of the project.", required = true) @PathVariable("projectName") String projectName,
            @ApiParam(value = "ID of the plan.", required = true) @PathVariable("planId") Integer planId);

    @ApiOperation(value = "Returns last plan build", notes = "Returns last plan build.", response = Build.class, tags = {
            "TestLink", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = Build.class),
            @ApiResponse(code = 404, message = "Resources not found") })
    @RequestMapping(value = "/testlink/project/{projectName}/plan/{planId}/build/latest", method = RequestMethod.GET)
    ResponseEntity<Build> getLatestPlanBuild(
            @ApiParam(value = "Name of the project.", required = true) @PathVariable("projectName") String projectName,
            @ApiParam(value = "ID of the plan.", required = true) @PathVariable("planId") Integer planId);

    @ApiOperation(value = "Returns build by ID", notes = "Returns build by ID.", response = Build.class, tags = {
            "TestLink", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = Build.class),
            @ApiResponse(code = 404, message = "Resources not found") })
    @RequestMapping(value = "/testlink/project/plan/build/{buildId}", method = RequestMethod.GET)
    ResponseEntity<Build> getBuildById(
            @ApiParam(value = "ID of the build.", required = true) @PathVariable("buildId") Integer buildId);

    @ApiOperation(value = "Creates a new Build", notes = "Creates a new Build", response = Build.class, tags = {
            "TestLink", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = Build.class),
            @ApiResponse(code = 405, message = "Invalid input"),
            @ApiResponse(code = 409, message = "Already exist") })
    @RequestMapping(value = "/testlink/project/{projectName}/plan/{planId}/build", produces = {
            "application/json" }, consumes = {
                    "application/json" }, method = RequestMethod.POST)
    ResponseEntity<Build> createBuild(
            @ApiParam(value = "Name of the project.", required = true) @PathVariable("projectName") String projectName,
            @ApiParam(value = "ID of the plan.", required = true) @PathVariable("planId") Integer planId,
            @ApiParam(value = "Object with the Test Plan data to create.", required = true) @Valid @RequestBody Build body);

    @ApiOperation(value = "Returns the Test Cases of a Build", notes = "Returnsthe Test Cases of a Build", response = TestCase.class, responseContainer = "List", tags = {
            "TestLink", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = TestCase.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Resources not found") })
    @RequestMapping(value = "/testlink/project/plan/build/{buildId}/case", method = RequestMethod.GET)
    ResponseEntity<TestCase[]> getBuildTestCases(
            @ApiParam(value = "Id of the Build.", required = true) @PathVariable("buildId") Integer buildId);

    /* ***********************************************************************/
    /* ***************************** Executions ******************************/
    /* ***********************************************************************/

    @ApiOperation(value = "Execute Test Case", notes = "Execute Test Case", response = ReportTCResultResponse.class, tags = {
            "TestLink", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = ReportTCResultResponse.class),
            @ApiResponse(code = 405, message = "Invalid input"),
            @ApiResponse(code = 409, message = "Already exist") })
    @RequestMapping(value = "/testlink/project/plan/build/case/{caseId}/exec", produces = {
            "application/json" }, consumes = {
                    "application/json" }, method = RequestMethod.POST)
    ResponseEntity<ReportTCResultResponse> executeTestCase(
            @ApiParam(value = "ID of the test case.", required = true) @PathVariable("caseId") Integer caseId,
            @ApiParam(value = "Object with the Test Case Results.", required = true) @Valid @RequestBody Execution body);

    @ApiOperation(value = "Returns all execs", notes = "Returns all execs.", response = Execution.class, responseContainer = "List", tags = {
            "TestLink", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = Execution.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Resources not found") })
    @RequestMapping(value = "/testlink/execs", method = RequestMethod.GET)
    ResponseEntity<Execution[]> getAllExecs();

    @ApiOperation(value = "Returns all execs of a Test Case", notes = "Returns all execs of a Test Case.", response = Execution.class, responseContainer = "List", tags = {
            "TestLink", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = Execution.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Resources not found") })
    @RequestMapping(value = "/testlink/project/suite/case/{caseId}/execs", method = RequestMethod.GET)
    ResponseEntity<Execution[]> getTestCaseExecs(
            @ApiParam(value = "Id of Test case.", required = true) @PathVariable("caseId") Integer caseId);

    @ApiOperation(value = "Returns all execs of a Plan Test Case", notes = "Returns all execs of a Plan Test Case.", response = Execution.class, responseContainer = "List", tags = {
            "TestLink", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = Execution.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Resources not found") })
    @RequestMapping(value = "/testlink/project/plan/{planId}/build/case/{caseId}/execs", method = RequestMethod.GET)
    ResponseEntity<Execution[]> getPlanTestCaseExecs(
            @ApiParam(value = "Id of Test Plan.", required = true) @PathVariable("planId") Integer planId,
            @ApiParam(value = "Id of Test case.", required = true) @PathVariable("caseId") Integer caseId);

    @ApiOperation(value = "Returns all execs of a Build Test Case", notes = "Returns all execs of a Build Test Case.", response = Execution.class, responseContainer = "List", tags = {
            "TestLink", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = Execution.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Resources not found") })
    @RequestMapping(value = "/testlink/project/plan/build/{buildId}/case/{caseId}/execs", method = RequestMethod.GET)
    ResponseEntity<Execution[]> getBuildTestCaseExecs(
            @ApiParam(value = "ID of the build.", required = true) @PathVariable("buildId") Integer buildId,
            @ApiParam(value = "Id of Test case.", required = true) @PathVariable("caseId") Integer caseId);

    /* *********************************************************/
    /* ************************ Others *************************/
    /* *********************************************************/

    @ApiOperation(value = "Returns TestLink Ip/port", notes = "Returns TEstLink IP/Port.", response = String.class, tags = {
            "TestLink", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = String.class),
            @ApiResponse(code = 404, message = "Resources not found") })
    @RequestMapping(value = "/testlink/url", method = RequestMethod.GET)
    ResponseEntity<String> getTestLinkUrl();

    @ApiOperation(value = "Synchronizes TestLink Data With Elastest Data", notes = "Synchronizes TestLink Data With Elastest Data.", response = Boolean.class, tags = {
            "TestLink", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = Boolean.class),
            @ApiResponse(code = 404, message = "Resources not found") })
    @RequestMapping(value = "/testlink/sync", method = RequestMethod.GET)
    ResponseEntity<Boolean> syncTestLink();

}
