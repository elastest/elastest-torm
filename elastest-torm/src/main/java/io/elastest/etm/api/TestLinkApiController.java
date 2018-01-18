package io.elastest.etm.api;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import br.eti.kinoshita.testlinkjavaapi.model.Build;
import br.eti.kinoshita.testlinkjavaapi.model.TestCase;
import br.eti.kinoshita.testlinkjavaapi.model.TestPlan;
import br.eti.kinoshita.testlinkjavaapi.model.TestProject;
import br.eti.kinoshita.testlinkjavaapi.model.TestSuite;
import br.eti.kinoshita.testlinkjavaapi.util.TestLinkAPIException;
import io.elastest.etm.service.TestLinkService;
import io.swagger.annotations.ApiParam;

@Controller
public class TestLinkApiController implements TestLinkApi {
    @Autowired
    TestLinkService testLinkService;

    /* ************************************************************************/
    /* **************************** Test Projects *****************************/
    /* ************************************************************************/
    // @JsonView()
    public ResponseEntity<TestProject[]> getAllTestProjects() {

        return new ResponseEntity<TestProject[]>(testLinkService.getProjects(),
                HttpStatus.OK);
    }

    public ResponseEntity<TestProject> getProjectByName(
            @ApiParam(value = "Name of the project.", required = true) @PathVariable("projectName") String projectName) {
        return new ResponseEntity<TestProject>(
                testLinkService.getProjectByName(projectName), HttpStatus.OK);
    }

    public ResponseEntity<TestProject> getProjectById(
            @ApiParam(value = "Name of the project.", required = true) @PathVariable("projectId") Integer projectId) {
        return new ResponseEntity<TestProject>(
                testLinkService.getProjectById(projectId), HttpStatus.OK);
    }

    public ResponseEntity<TestProject> createProject(
            @ApiParam(value = "Object with the test project data to create.", required = true) @Valid @RequestBody TestProject body) {
        TestProject project = null;
        try {
            project = testLinkService.createProject(body);
            return new ResponseEntity<TestProject>(project, HttpStatus.OK);
        } catch (TestLinkAPIException e) {
            return new ResponseEntity<TestProject>(project,
                    HttpStatus.CONFLICT);
        }
    }

    /* ************************************************************************/
    /* ***************************** Test Suites ******************************/
    /* ************************************************************************/

    public ResponseEntity<TestSuite> getTestSuiteById(
            @ApiParam(value = "Id of Test suite.", required = true) @PathVariable("suiteId") Integer suiteId) {
        return new ResponseEntity<TestSuite>(
                testLinkService.getTestSuiteById(suiteId), HttpStatus.OK);
    }

    public ResponseEntity<TestSuite[]> getProjectTestSuites(
            @ApiParam(value = "ID of the Test Project.", required = true) @PathVariable("projectId") Integer projectId) {
        return new ResponseEntity<TestSuite[]>(
                testLinkService.getProjectTestSuites(projectId), HttpStatus.OK);

    }

    public ResponseEntity<TestSuite> createSuite(
            @ApiParam(value = "Object with the Test Suite data to create.", required = true) @Valid @RequestBody TestSuite body) {
        TestSuite suite = null;
        try {
            suite = testLinkService.createTestSuite(body);
            return new ResponseEntity<TestSuite>(suite, HttpStatus.OK);
        } catch (TestLinkAPIException e) {
            return new ResponseEntity<TestSuite>(suite, HttpStatus.CONFLICT);
        }
    }

    /* ***********************************************************************/
    /* ***************************** Test Cases ******************************/
    /* ***********************************************************************/

    public ResponseEntity<TestCase> getTestcase(
            @ApiParam(value = "Id of Test Suite.", required = true) @PathVariable("suiteId") Integer suiteId,
            @ApiParam(value = "Id of Test case.", required = true) @PathVariable("caseId") Integer caseId) {
        return new ResponseEntity<TestCase>(
                testLinkService.getTestCaseById(suiteId, caseId),
                HttpStatus.OK);
    }

    public ResponseEntity<TestCase[]> getSuiteTestCases(
            @ApiParam(value = "Id of Test Suite.", required = true) @PathVariable("suiteId") Integer suiteId) {
        return new ResponseEntity<TestCase[]>(
                testLinkService.getSuiteTestCases(suiteId), HttpStatus.OK);
    }

    public ResponseEntity<TestCase> createTestCase(
            @ApiParam(value = "Object with the Test Case data to create.", required = true) @Valid @RequestBody TestCase body) {
        TestCase testCase = null;
        try {
            testCase = testLinkService.createTestCase(body);
            return new ResponseEntity<TestCase>(testCase, HttpStatus.OK);
        } catch (TestLinkAPIException e) {
            return new ResponseEntity<TestCase>(testCase, HttpStatus.CONFLICT);
        }
    }
    /* ***********************************************************************/
    /* ***************************** Test Plans ******************************/
    /* ***********************************************************************/

    public ResponseEntity<TestPlan[]> getProjectTestPlans(
            @ApiParam(value = "ID of the project.", required = true) @PathVariable("id") Integer id) {
        return new ResponseEntity<TestPlan[]>(
                testLinkService.getProjectTestPlans(id), HttpStatus.OK);
    }

    public ResponseEntity<TestPlan> getPlanByName(
            @ApiParam(value = "Name of the project.", required = true) @PathVariable("projectName") String projectName,
            @ApiParam(value = "Name of the plan.", required = true) @PathVariable("planName") String planName) {
        return new ResponseEntity<TestPlan>(
                testLinkService.getTestPlanByName(planName, projectName),
                HttpStatus.OK);
    }

    public ResponseEntity<TestPlan> createPlan(
            @ApiParam(value = "Object with the Test Plan data to create.", required = true) @Valid @RequestBody TestPlan body) {
        TestPlan plan = null;
        try {
            plan = testLinkService.createTestPlan(body);
            return new ResponseEntity<TestPlan>(plan, HttpStatus.OK);
        } catch (TestLinkAPIException e) {
            return new ResponseEntity<TestPlan>(plan, HttpStatus.CONFLICT);
        }
    }

    /* **********************************************************************/
    /* **************************** Plan Builds *****************************/
    /* **********************************************************************/

    public ResponseEntity<Build[]> getPlanBuilds(
            @ApiParam(value = "Name of the project.", required = true) @PathVariable("projectName") String projectName,
            @ApiParam(value = "ID of the plan.", required = true) @PathVariable("planId") Integer planId) {
        return new ResponseEntity<Build[]>(
                testLinkService.getPlanBuilds(planId), HttpStatus.OK);
    }

    public ResponseEntity<Build> getLatestPlanBuild(
            @ApiParam(value = "Name of the project.", required = true) @PathVariable("projectName") String projectName,
            @ApiParam(value = "ID of the plan.", required = true) @PathVariable("planId") Integer planId) {
        return new ResponseEntity<Build>(
                testLinkService.getLatestPlanBuild(planId), HttpStatus.OK);
    }

    public ResponseEntity<Build> createBuild(
            @ApiParam(value = "Name of the project.", required = true) @PathVariable("projectName") String projectName,
            @ApiParam(value = "ID of the plan.", required = true) @PathVariable("planId") Integer planId,
            @ApiParam(value = "Object with the Test Plan data to create.", required = true) @Valid @RequestBody Build body) {
        Build build = null;
        try {
            build = testLinkService.createBuild(body);
            return new ResponseEntity<Build>(build, HttpStatus.OK);
        } catch (TestLinkAPIException e) {
            return new ResponseEntity<Build>(build, HttpStatus.CONFLICT);
        }
    }

}
