package io.elastest.etm.api;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import br.eti.kinoshita.testlinkjavaapi.model.TestCase;
import br.eti.kinoshita.testlinkjavaapi.model.TestPlan;
import br.eti.kinoshita.testlinkjavaapi.model.TestProject;
import br.eti.kinoshita.testlinkjavaapi.model.TestSuite;
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

    public ResponseEntity<TestProject> createProject(
            @ApiParam(value = "Object with the test project data to create.", required = true) @Valid @RequestBody TestProject body) {
        return new ResponseEntity<TestProject>(
                testLinkService.createProject(body), HttpStatus.OK);
    }

    /* ***********************************************************************/
    /* ***************************** Test Plans ******************************/
    /* ***********************************************************************/

    public ResponseEntity<TestPlan[]> getProjectTestPlans(
            @ApiParam(value = "ID of the project.", required = true) @PathVariable("id") Integer id) {
        return new ResponseEntity<TestPlan[]>(
                testLinkService.getProjectTestPlans(id), HttpStatus.OK);
    }

    public ResponseEntity<TestPlan> getPlanByName(String projectName,
            String planName) {
        return new ResponseEntity<TestPlan>(
                testLinkService.getTestPlanByName(planName, projectName),
                HttpStatus.OK);
    }

    public ResponseEntity<TestPlan> createPlan(
            @ApiParam(value = "Object with the Test Plan data to create.", required = true) @Valid @RequestBody TestPlan body) {
        return new ResponseEntity<TestPlan>(
                testLinkService.createTestPlan(body), HttpStatus.OK);
    }

    /* **********************************************************************/
    /* **************************** Plan Builds *****************************/
    /* **********************************************************************/

    /* ************************************************************************/
    /* ***************************** Test Suites ******************************/
    /* ************************************************************************/

    public ResponseEntity<TestSuite> getTestSuiteById(Integer suiteId) {
        return new ResponseEntity<TestSuite>(
                testLinkService.getTestSuiteById(suiteId), HttpStatus.OK);
    }

    public ResponseEntity<TestSuite[]> getProjectTestSuites(Integer projectId) {
        return new ResponseEntity<TestSuite[]>(
                testLinkService.getProjectTestSuites(projectId), HttpStatus.OK);

    }

    public ResponseEntity<TestSuite> createSuite(
            @ApiParam(value = "Object with the Test Suite data to create.", required = true) @Valid @RequestBody TestSuite body) {
        return new ResponseEntity<TestSuite>(
                testLinkService.createTestSuite(body), HttpStatus.OK);
    }

    /* ***********************************************************************/
    /* ***************************** Test Cases ******************************/
    /* ***********************************************************************/

    public ResponseEntity<TestCase> getTestcase(Integer suiteId,
            Integer caseId) {
        return new ResponseEntity<TestCase>(
                testLinkService.getTestCaseById(suiteId, caseId),
                HttpStatus.OK);
    }

    public ResponseEntity<TestCase[]> getSuiteTestCases(Integer suiteId) {
        return new ResponseEntity<TestCase[]>(
                testLinkService.getSuiteTestCases(suiteId), HttpStatus.OK);
    }

    public ResponseEntity<TestCase> createTestCase(
            @ApiParam(value = "Object with the Test Case data to create.", required = true) @Valid @RequestBody TestCase body) {
        return new ResponseEntity<TestCase>(
                testLinkService.createTestCase(body), HttpStatus.OK);
    }

}
