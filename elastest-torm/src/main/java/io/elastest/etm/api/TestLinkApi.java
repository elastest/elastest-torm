package io.elastest.etm.api;

import javax.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import br.eti.kinoshita.testlinkjavaapi.model.Build;
import br.eti.kinoshita.testlinkjavaapi.model.Execution;
import br.eti.kinoshita.testlinkjavaapi.model.TestCase;
import br.eti.kinoshita.testlinkjavaapi.model.TestPlan;
import br.eti.kinoshita.testlinkjavaapi.model.TestProject;
import br.eti.kinoshita.testlinkjavaapi.model.TestSuite;
import io.elastest.etm.model.external.ExternalProject;
import io.elastest.etm.model.external.ExternalTJob;
import io.elastest.etm.model.external.ExternalTestCase;
import io.elastest.etm.model.external.ExternalTestExecution;
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
    @RequestMapping(value = "/testlink/project", produces = {
            "application/json" }, method = RequestMethod.GET)
    ResponseEntity<TestProject[]> getAllTestProjects();

    @ApiOperation(value = "Returns a Test Project By Given Name", notes = "Returns a Test Project By Given Name.", response = TestProject.class, tags = {
            "TestLink", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = TestProject.class),
            @ApiResponse(code = 404, message = "Resources not found") })
    @RequestMapping(value = "/testlink/project/name/{projectName}", produces = {
            "application/json" }, method = RequestMethod.GET)
    ResponseEntity<TestProject> getProjectByName(
            @ApiParam(value = "Name of the project.", required = true) @PathVariable("projectName") String projectName);

    @ApiOperation(value = "Returns a Test Project By Given Id", notes = "Returns a Test Project By Given Id.", response = TestProject.class, tags = {
            "TestLink", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = TestProject.class),
            @ApiResponse(code = 404, message = "Resources not found") })
    @RequestMapping(value = "/testlink/project/{projectId}", produces = {
            "application/json" }, method = RequestMethod.GET)
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
    @RequestMapping(value = "/testlink/project/{projectId}/suite/{suiteId}", produces = {
            "application/json" }, method = RequestMethod.GET)
    ResponseEntity<TestSuite> getTestSuiteById(
            @ApiParam(value = "ID of the project.", required = true) @PathVariable("projectId") Integer projectId,
            @ApiParam(value = "Id of Test suite.", required = true) @PathVariable("suiteId") Integer suiteId);

    @ApiOperation(value = "Returns a Test suite By Given Name", notes = "Returns a test suite by given name.", response = TestSuite.class, tags = {
            "TestLink", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = TestSuite.class),
            @ApiResponse(code = 404, message = "Resources not found") })
    @RequestMapping(value = "/testlink/project/suite/name/{suiteName}", produces = {
            "application/json" }, method = RequestMethod.GET)
    ResponseEntity<TestSuite> getSuiteByName(
            @ApiParam(value = "Name of the suite.", required = true) @PathVariable("suiteName") String suiteName);

    @ApiOperation(value = "Returns the Test Suites of a Test Project", notes = "Returns the Test Suites of a Test Project.", response = TestSuite.class, responseContainer = "List", tags = {
            "TestLink", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = TestSuite.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Resources not found") })
    @RequestMapping(value = "/testlink/project/{projectId}/suite", produces = {
            "application/json" }, method = RequestMethod.GET)
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
    @RequestMapping(value = "/testlink/project/suite/case/{caseId}", produces = {
            "application/json" }, method = RequestMethod.GET)
    ResponseEntity<TestCase> getTestcase(
            @ApiParam(value = "Id of Test case.", required = true) @PathVariable("caseId") Integer caseId);

    @ApiOperation(value = "Returns a Test Case By Given Name", notes = "Returns a test case By Given Name.", response = TestCase.class, tags = {
            "TestLink", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = TestCase.class),
            @ApiResponse(code = 404, message = "Resources not found") })
    @RequestMapping(value = "/testlink/project/suite/case/name/{caseName}", produces = {
            "application/json" }, method = RequestMethod.GET)
    ResponseEntity<TestCase> getTestCaseByName(
            @ApiParam(value = "Name of Test case.", required = true) @PathVariable("caseName") String caseName);

    @ApiOperation(value = "Returns all Test Cases", notes = "Returns all Test Cases", response = TestCase.class, responseContainer = "List", tags = {
            "TestLink", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = TestCase.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Resources not found") })
    @RequestMapping(value = "/testlink/project/suite/case", produces = {
            "application/json" }, method = RequestMethod.GET)
    ResponseEntity<TestCase[]> getAllTestCases();

    @ApiOperation(value = "Returns the Test Cases of a Test Suite", notes = "Returns the Test Cases of a Test Suite", response = TestCase.class, responseContainer = "List", tags = {
            "TestLink", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = TestCase.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Resources not found") })
    @RequestMapping(value = "/testlink/project/{projectId}/suite/{suiteId}/case", produces = {
            "application/json" }, method = RequestMethod.GET)
    ResponseEntity<TestCase[]> getSuiteTestCases(
            @ApiParam(value = "Id of Test Suite.", required = true) @PathVariable("suiteId") Integer suiteId);

    @ApiOperation(value = "Returns the Test Cases of a Test Plan", notes = "Returnsthe Test Cases of a Test Plan", response = TestCase.class, responseContainer = "List", tags = {
            "TestLink", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = TestCase.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Resources not found") })
    @RequestMapping(value = "/testlink/project/plan/{planId}/case", produces = {
            "application/json" }, method = RequestMethod.GET)
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

    @ApiOperation(value = "Returns all Test Plans", notes = "Returns all Test Plans.", response = TestPlan.class, responseContainer = "List", tags = {
            "TestLink", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = TestPlan.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Resources not found") })
    @RequestMapping(value = "/testlink/project/plan", produces = {
            "application/json" }, method = RequestMethod.GET)
    ResponseEntity<TestPlan[]> getAllTestPlans();

    @ApiOperation(value = "Returns all test plans of a project", notes = "Returns all test plans of a project.", response = TestPlan.class, responseContainer = "List", tags = {
            "TestLink", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = TestPlan.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Resources not found") })
    @RequestMapping(value = "/testlink/project/{id}/plan", produces = {
            "application/json" }, method = RequestMethod.GET)
    ResponseEntity<TestPlan[]> getProjectTestPlans(
            @ApiParam(value = "ID of the project.", required = true) @PathVariable("id") Integer id);

    @ApiOperation(value = "Returns a Test plan By Given Id", notes = "Returns a test plan by given id.", response = TestPlan.class, tags = {
            "TestLink", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = TestPlan.class),
            @ApiResponse(code = 404, message = "Resources not found") })
    @RequestMapping(value = "/testlink/project/plan/{planId}", produces = {
            "application/json" }, method = RequestMethod.GET)
    ResponseEntity<TestPlan> getPlanById(
            @ApiParam(value = "Id of the plan.", required = true) @PathVariable("planId") Integer planId);

    @ApiOperation(value = "Returns a Test plan by given name and project name", notes = "Returns a test plan by given name and project name.", response = TestPlan.class, tags = {
            "TestLink", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = TestPlan.class),
            @ApiResponse(code = 404, message = "Resources not found") })
    @RequestMapping(value = "/testlink/project/{projectName}/plan/name/{planName}", produces = {
            "application/json" }, method = RequestMethod.GET)
    ResponseEntity<TestPlan> getPlanByNameAndProjectName(
            @ApiParam(value = "Name of the project.", required = true) @PathVariable("projectName") String projectName,
            @ApiParam(value = "Name of the plan.", required = true) @PathVariable("planName") String planName);

    @ApiOperation(value = "Returns a Test plan By Given Name", notes = "Returns a test plan by given Name.", response = TestPlan.class, tags = {
            "TestLink", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = TestPlan.class),
            @ApiResponse(code = 404, message = "Resources not found") })
    @RequestMapping(value = "/testlink/project/plan/name/{planName}", produces = {
            "application/json" }, method = RequestMethod.GET)
    ResponseEntity<TestPlan> getPlanByName(
            @ApiParam(value = "Name of the plan.", required = true) @PathVariable("planName") String planName);

    @ApiOperation(value = "Creates a new Test Plan", notes = "Creates a new Test Plan", response = TestPlan.class, tags = {
            "TestLink", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = TestPlan.class),
            @ApiResponse(code = 405, message = "Invalid input"),
            @ApiResponse(code = 409, message = "Already exist") })
    @RequestMapping(value = "/testlink/project/plan", produces = {
            "application/json" }, consumes = {
                    "application/json" }, method = RequestMethod.POST)
    ResponseEntity<TestPlan> createPlan(
            @ApiParam(value = "Object with the Test Plan data to create.", required = true) @Valid @RequestBody TestPlan body);

    @ApiOperation(value = "Associates a Test Case to a Test Plan", notes = "Associates a Test Case to a Test Plan", response = Integer.class, tags = {
            "TestLink", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = Integer.class),
            @ApiResponse(code = 405, message = "Invalid input"),
            @ApiResponse(code = 409, message = "Already exist") })
    @RequestMapping(value = "/testlink/project/plan/{planId}/case/add", produces = {
            "application/json" }, consumes = {
                    "application/json" }, method = RequestMethod.POST)
    ResponseEntity<Integer> addTestCaseToTestPlan(
            @ApiParam(value = "Id of the plan.", required = true) @PathVariable("planId") Integer planId,
            @ApiParam(value = "Object with the Test Case data to create.", required = true) @Valid @RequestBody TestCase body);

    /* *********************************************************************/
    /* ****************************** Builds *******************************/
    /* *********************************************************************/

    @ApiOperation(value = "Returns all builds of a Test Plan", notes = "Returns all builds of a Test Plan.", response = Build.class, responseContainer = "List", tags = {
            "TestLink", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = Build.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Resources not found") })
    @RequestMapping(value = "/testlink/project/plan/{planId}/build", produces = {
            "application/json" }, method = RequestMethod.GET)
    ResponseEntity<Build[]> getPlanBuilds(
            @ApiParam(value = "ID of the plan.", required = true) @PathVariable("planId") Integer planId);

    @ApiOperation(value = "Returns a build of a Test Plan By given Id", notes = "Returns a build of a Test Plan By given Id.", response = Build.class, tags = {
            "TestLink", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = Build.class),
            @ApiResponse(code = 404, message = "Resources not found") })
    @RequestMapping(value = "/testlink/project/plan/{planId}/build/{buildId}", produces = {
            "application/json" }, method = RequestMethod.GET)
    ResponseEntity<Build> getPlanBuildById(
            @ApiParam(value = "ID of the plan.", required = true) @PathVariable("planId") Integer planId,
            @ApiParam(value = "ID of the build.", required = true) @PathVariable("buildId") Integer buildId);

    @ApiOperation(value = "Returns last plan build", notes = "Returns last plan build.", response = Build.class, tags = {
            "TestLink", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = Build.class),
            @ApiResponse(code = 404, message = "Resources not found") })
    @RequestMapping(value = "/testlink/project/{projectName}/plan/{planId}/build/latest", produces = {
            "application/json" }, method = RequestMethod.GET)
    ResponseEntity<Build> getLatestPlanBuild(
            @ApiParam(value = "Name of the project.", required = true) @PathVariable("projectName") String projectName,
            @ApiParam(value = "ID of the plan.", required = true) @PathVariable("planId") Integer planId);

    @ApiOperation(value = "Returns build by ID", notes = "Returns build by ID.", response = Build.class, tags = {
            "TestLink", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = Build.class),
            @ApiResponse(code = 404, message = "Resources not found") })
    @RequestMapping(value = "/testlink/project/plan/build/{buildId}", produces = {
            "application/json" }, method = RequestMethod.GET)
    ResponseEntity<Build> getBuildById(
            @ApiParam(value = "ID of the build.", required = true) @PathVariable("buildId") Integer buildId);

    @ApiOperation(value = "Returns build by Name", notes = "Returns build by Name.", response = Build.class, tags = {
            "TestLink", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = Build.class),
            @ApiResponse(code = 404, message = "Resources not found") })
    @RequestMapping(value = "/testlink/project/plan/build/name/{buildName}", produces = {
            "application/json" }, method = RequestMethod.GET)
    ResponseEntity<Build> getBuildByName(
            @ApiParam(value = "Name of the build.", required = true) @PathVariable("buildName") String buildName);

    @ApiOperation(value = "Creates a new Build", notes = "Creates a new Build", response = Build.class, tags = {
            "TestLink", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = Build.class),
            @ApiResponse(code = 405, message = "Invalid input"),
            @ApiResponse(code = 409, message = "Already exist") })
    @RequestMapping(value = "/testlink/project/plan/build", produces = {
            "application/json" }, consumes = {
                    "application/json" }, method = RequestMethod.POST)
    ResponseEntity<Build> createBuild(
            @ApiParam(value = "Object with the Test Plan data to create.", required = true) @Valid @RequestBody Build body);

    @ApiOperation(value = "Returns the Test Cases of a Build", notes = "Returns the Test Cases of a Build", response = TestCase.class, responseContainer = "List", tags = {
            "TestLink", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = TestCase.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Resources not found") })
    @RequestMapping(value = "/testlink/project/plan/build/{buildId}/case", produces = {
            "application/json" }, method = RequestMethod.GET)
    ResponseEntity<TestCase[]> getBuildTestCases(
            @ApiParam(value = "Id of the Build.", required = true) @PathVariable("buildId") Integer buildId);

    @ApiOperation(value = "Returns specific Test Case of a Build", notes = "Returns specific Test Case of a Build", response = TestCase.class, tags = {
            "TestLink", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = TestCase.class),
            @ApiResponse(code = 404, message = "Resources not found") })
    @RequestMapping(value = "/testlink/project/plan/build/{buildId}/case/{caseId}", produces = {
            "application/json" }, method = RequestMethod.GET)
    ResponseEntity<TestCase> getBuildTestCaseById(
            @ApiParam(value = "Id of the Build.", required = true) @PathVariable("buildId") Integer buildId,
            @ApiParam(value = "Id of the Test Case.", required = true) @PathVariable("caseId") Integer caseId);

    /* ***********************************************************************/
    /* ***************************** Executions ******************************/
    /* ***********************************************************************/

    @ApiOperation(value = "Execute Test Case", notes = "Execute Test Case", response = Execution.class, tags = {
            "TestLink", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = Execution.class),
            @ApiResponse(code = 405, message = "Invalid input"),
            @ApiResponse(code = 409, message = "Already exist") })
    @RequestMapping(value = "/testlink/project/plan/build/case/{caseId}/exec", produces = {
            "application/json" }, consumes = {
                    "application/json" }, method = RequestMethod.POST)
    ResponseEntity<Execution> executeTestCase(
            @ApiParam(value = "ID of the test case.", required = true) @PathVariable("caseId") Integer caseId,
            @ApiParam(value = "Object with the Test Case Results.", required = true) @Valid @RequestBody Execution body);

    @ApiOperation(value = "Returns all execs", notes = "Returns all execs.", response = Execution.class, responseContainer = "List", tags = {
            "TestLink", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = Execution.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Resources not found") })
    @RequestMapping(value = "/testlink/execs", produces = {
            "application/json" }, method = RequestMethod.GET)
    ResponseEntity<Execution[]> getAllExecs();

    @ApiOperation(value = "Returns all execs of a Test Case", notes = "Returns all execs of a Test Case.", response = Execution.class, responseContainer = "List", tags = {
            "TestLink", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = Execution.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Resources not found") })
    @RequestMapping(value = "/testlink/project/suite/case/{caseId}/execs", produces = {
            "application/json" }, method = RequestMethod.GET)
    ResponseEntity<Execution[]> getTestCaseExecs(
            @ApiParam(value = "Id of Test case.", required = true) @PathVariable("caseId") Integer caseId);

    @ApiOperation(value = "Returns a Test Execution of a Test Case by Id", notes = "Returns a Test Execution of a Test Case by Id.", response = Execution.class, tags = {
            "TestLink", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = Execution.class),
            @ApiResponse(code = 404, message = "Resources not found") })
    @RequestMapping(value = "/testlink/project/suite/case/{caseId}/exec/{execId}", produces = {
            "application/json" }, method = RequestMethod.GET)
    ResponseEntity<Execution> getTestExecById(
            @ApiParam(value = "Id of Test Case.", required = true) @PathVariable("caseId") Integer caseId,
            @ApiParam(value = "Id of Test Execution.", required = true) @PathVariable("execId") Integer execId);

    @ApiOperation(value = "Returns all execs of a Plan Test Case", notes = "Returns all execs of a Plan Test Case.", response = Execution.class, responseContainer = "List", tags = {
            "TestLink", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = Execution.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Resources not found") })
    @RequestMapping(value = "/testlink/project/plan/{planId}/build/case/{caseId}/execs", produces = {
            "application/json" }, method = RequestMethod.GET)
    ResponseEntity<Execution[]> getPlanTestCaseExecs(
            @ApiParam(value = "Id of Test Plan.", required = true) @PathVariable("planId") Integer planId,
            @ApiParam(value = "Id of Test case.", required = true) @PathVariable("caseId") Integer caseId);

    @ApiOperation(value = "Returns all execs of a Build Test Case", notes = "Returns all execs of a Build Test Case.", response = Execution.class, responseContainer = "List", tags = {
            "TestLink", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = Execution.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Resources not found") })
    @RequestMapping(value = "/testlink/project/plan/build/{buildId}/case/{caseId}/execs", produces = {
            "application/json" }, method = RequestMethod.GET)
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
    @RequestMapping(value = "/testlink/url", produces = {
            "application/json" }, method = RequestMethod.GET)
    ResponseEntity<String> getTestLinkUrl();

    @ApiOperation(value = "Synchronizes TestLink Data With Elastest Data", notes = "Synchronizes TestLink Data With Elastest Data.", response = Boolean.class, tags = {
            "TestLink", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = Boolean.class),
            @ApiResponse(code = 404, message = "Resources not found") })
    @RequestMapping(value = "/testlink/sync", produces = {
            "application/json" }, method = RequestMethod.GET)
    ResponseEntity<Boolean> syncTestLink();

    @ApiOperation(value = "Returns an External Project By Given Test Project Id", notes = "Returns an External Project By Given Test Project Id.", response = ExternalProject.class, tags = {
            "TestLink", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = ExternalProject.class),
            @ApiResponse(code = 404, message = "Resources not found") })
    @RequestMapping(value = "/testlink/external/project/{projectId}", produces = {
            "application/json" }, method = RequestMethod.GET)
    ResponseEntity<ExternalProject> getExternalProjectByTestProjectId(
            @ApiParam(value = "ID of the project.", required = true) @PathVariable("projectId") Integer projectId);

    @ApiOperation(value = "Returns an External TJob By Given Test Plan Id", notes = "Returns an External TJob By Given Test Plan Id.", response = ExternalTJob.class, tags = {
            "TestLink", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = ExternalTJob.class),
            @ApiResponse(code = 404, message = "Resources not found") })
    @RequestMapping(value = "/testlink/external/tjob/{planId}", produces = {
            "application/json" }, method = RequestMethod.GET)
    ResponseEntity<ExternalTJob> getExternalTJobByTestPlanId(
            @ApiParam(value = "ID of the plan.", required = true) @PathVariable("planId") Integer planId);

    @ApiOperation(value = "Returns an External Test Case By Given Test Case Id", notes = "Returns an External Case By Given Test Case Id.", response = ExternalTestCase.class, tags = {
            "TestLink", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = ExternalTestCase.class),
            @ApiResponse(code = 404, message = "Resources not found") })
    @RequestMapping(value = "/testlink/external/testcase/{caseId}", produces = {
            "application/json" }, method = RequestMethod.GET)
    ResponseEntity<ExternalTestCase> getExternalTestCaseByTestCaseId(
            @ApiParam(value = "ID of the Test Case.", required = true) @PathVariable("caseId") Integer caseId);

    @ApiOperation(value = "Returns an External Test Execution By Given Execution Id", notes = "Returns an External Case By Given Execution Id.", response = ExternalTestExecution.class, tags = {
            "TestLink", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = ExternalTestExecution.class),
            @ApiResponse(code = 404, message = "Resources not found") })
    @RequestMapping(value = "/testlink/external/testexec/{execId}", produces = {
            "application/json" }, method = RequestMethod.GET)
    ResponseEntity<ExternalTestExecution> getExternalTestExecutionByExecutionId(
            @ApiParam(value = "ID of the Execution.", required = true) @PathVariable("execId") Integer execId);
}
