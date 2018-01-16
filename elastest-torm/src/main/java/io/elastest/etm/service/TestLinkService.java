
package io.elastest.etm.service;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import br.eti.kinoshita.testlinkjavaapi.TestLinkAPI;
import br.eti.kinoshita.testlinkjavaapi.constants.ExecutionStatus;
import br.eti.kinoshita.testlinkjavaapi.constants.TestCaseDetails;
import br.eti.kinoshita.testlinkjavaapi.model.Build;
import br.eti.kinoshita.testlinkjavaapi.model.ReportTCResultResponse;
import br.eti.kinoshita.testlinkjavaapi.model.TestCase;
import br.eti.kinoshita.testlinkjavaapi.model.TestPlan;
import br.eti.kinoshita.testlinkjavaapi.model.TestProject;
import br.eti.kinoshita.testlinkjavaapi.model.TestSuite;
import br.eti.kinoshita.testlinkjavaapi.util.TestLinkAPIException;

@Service
public class TestLinkService {
    private static final Logger logger = LoggerFactory
            .getLogger(TestLinkService.class);

    @Value("${et.etm.testlink.host}")
    public String etEtmTestLinkHost;

    @Value("${et.etm.testlink.port}")
    public String etEtmTestLinkPort;

    String devKey = "20b9a66e17597842404062c3b628b938";
    TestLinkAPI api = null;
    URL testlinkURL = null;

    @PostConstruct
    public void init() {
        String url = "http://" + etEtmTestLinkHost + ":" + etEtmTestLinkPort
                + "/lib/api/xmlrpc/v1/xmlrpc.php";

        try {
            testlinkURL = new URL(url);
        } catch (MalformedURLException mue) {
            mue.printStackTrace(System.err);
        }

        try {
            api = new TestLinkAPI(testlinkURL, devKey);
        } catch (TestLinkAPIException te) {
            te.printStackTrace(System.err);
        }
        System.out.println(api.ping());
    }

    /**
     * @return the API info
     */
    public String getTestLinkInfo() {
        return this.api.about();
    }

    /**
     * ping method is an alias for sayHello.
     * 
     * @return Hello message
     */
    public String sayHello() {
        return this.api.ping();
    }

    /*************************************************************************/
    /***************************** Test Projects *****************************/
    /*************************************************************************/

    /**
     * Get All Test Projects
     * 
     * @return test projects
     */
    public TestProject[] getProjects() {
        return this.api.getProjects();
    }

    /**
     * Get Test Project by Name
     * 
     * @param projectName
     * @return test projects
     */
    public TestProject getProjectByName(String projectName) {
        return this.api.getTestProjectByName(projectName);
    }

    /**
     * Create Test Project
     * 
     * @param TestProject
     * @return TestProject
     */
    public TestProject createProject(TestProject project) {
        return this.api.createTestProject(project.getName(),
                project.getPrefix(), project.getNotes(),
                project.isEnableRequirements(), project.isEnableTestPriority(),
                project.isEnableAutomation(), project.isEnableInventory(),
                project.isActive(), project.isPublic());
    }

    /************************************************************************/
    /****************************** Test Plans ******************************/
    /************************************************************************/

    /**
     * Get All Test Plans of a Test Project
     * 
     * @param projectId
     * @return Project Test plans
     */
    public TestPlan[] getProjectTestPlans(Integer projectId) {
        return this.api.getProjectTestPlans(projectId);
    }

    /**
     * Get Test Plan of a Test Project
     * 
     * @param projectId
     * @return Project Test plans
     */
    public TestPlan getTestPlanByName(String planName, String projectName) {
        return this.api.getTestPlanByName(planName, projectName);
    }

    /**
     * Create Test Plan
     * 
     * @param TestPlan
     * @return TestPlan
     */
    public TestPlan createTestPlan(TestPlan plan) {
        return this.api.createTestPlan(plan.getName(), plan.getProjectName(),
                plan.getNotes(), plan.isActive(), plan.isPublic());
    }

    /***********************************************************************/
    /***************************** Plan Builds *****************************/
    /***********************************************************************/

    /**
     * Get all builds of a Test Plan
     * 
     * @param testPlanId
     * @return Test plans builds
     */
    public Build[] getTestPlansBuilds(Integer planId) {
        return this.api.getBuildsForTestPlan(planId);
    }

    /**
     * Get latest build of a Test Plan
     * 
     * @param testPlanId
     * @return Test plan build
     */
    public Build getLatestPlanBuild(Integer planId) {
        return this.api.getLatestBuildForTestPlan(planId);
    }

    /*************************************************************************/
    /****************************** Test Suites ******************************/
    /*************************************************************************/

    /**
     * Get All Test Suites of a Test Plan
     * 
     * @param testPlanId
     * @return TestSuite[]
     */
    public TestSuite[] getTestSuitesForTestPlan(Integer testPlanId) {
        return this.api.getTestSuitesForTestPlan(testPlanId);
    }

    /**
     * Get a Test Suite By Id
     * 
     * @param suiteId
     * @return TestSuite
     */
    public TestSuite getTestSuiteById(Integer suiteId) {
        List<Integer> testSuitesIds = new ArrayList<>();
        testSuitesIds.add(suiteId);
        TestSuite[] testSuites = this.api.getTestSuiteByID(testSuitesIds);
        if (testSuites.length == 1) {
            return testSuites[0];
        }

        return null;
    }

    /**
     * Create a TestSuite
     * 
     * @param TestSuite
     * @return TestSuite
     */
    public TestSuite createTestSuite(TestSuite suite) {
        return this.api.createTestSuite(suite.getTestProjectId(),
                suite.getName(), suite.getDetails(), suite.getParentId(),
                suite.getOrder(), suite.getCheckDuplicatedName(),
                suite.getActionOnDuplicatedName());
    }

    /************************************************************************/
    /****************************** Test Cases ******************************/
    /************************************************************************/

    /**
     * Get all Test Cases of a Test Plan
     * 
     * @param testPlanId
     * @param buildId
     * @return TestCase[]
     */
    public TestCase[] getTestCasesForTestPlan(Integer testPlanId,
            Integer buildId) {
        return this.api.getTestCasesForTestPlan(testPlanId, null, buildId, null,
                null, null, null, null, null, true, null);
    }

    /**
     * Get Test Case
     * 
     * @param testcase
     * @return TestCase
     */
    public TestCase getTestCaseById(Integer suiteId, Integer caseId) {
        TestCase foundTestCase = null;
        try {
            TestCase[] testCases = this.api.getTestCasesForTestSuite(suiteId,
                    true, TestCaseDetails.FULL);
            for (TestCase testCase : testCases) {
                if (testCase.getId() == caseId) {
                    foundTestCase = testCase;
                }
            }
        } catch (TestLinkAPIException e) {
            logger.error("Error during getting TestSuite: {}", e.getMessage());
        }

        return foundTestCase;
    }

    public TestCase createTestCase(TestCase testCase) {
        return this.api.createTestCase(testCase.getName(),
                testCase.getTestSuiteId(), testCase.getTestProjectId(),
                testCase.getAuthorLogin(), testCase.getSummary(),
                testCase.getSteps(), testCase.getPreconditions(),
                testCase.getTestImportance(), testCase.getExecutionType(),
                testCase.getOrder(), testCase.getInternalId(),
                testCase.getCheckDuplicatedName(),
                testCase.getActionOnDuplicatedName());
    }

    /**
     * Execute a Test of Test Plan
     * 
     * @param testCaseId
     * @param testPlanId
     * @param buildId
     * @param notes
     * @param status
     * 
     *            Reports a TestCase result.
     */
    public ReportTCResultResponse executeTest(Integer testCaseId,
            Integer testPlanId, Integer buildId, String notes,
            ExecutionStatus status) {
        ReportTCResultResponse response = this.api.reportTCResult(testCaseId,
                null, testPlanId, status, buildId, null, notes, null, null,
                null, null, null, null);

        return response;
    }

}