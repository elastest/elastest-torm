
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
            mue.printStackTrace();
        }

        try {
            api = new TestLinkAPI(testlinkURL, devKey);
        } catch (TestLinkAPIException te) {
            te.printStackTrace();
        }
    }

    public String getTestLinkInfo() {
        return this.api.about();
    }

    public String sayHello() {
        return this.api.ping();
    }

    /* ***********************************************************************/
    /* **************************** Test Projects ****************************/
    /* ***********************************************************************/

    public TestProject[] getProjects() {
        return this.api.getProjects();
    }

    public TestProject getProjectByName(String projectName) {
        return this.api.getTestProjectByName(projectName);
    }

    public TestProject getProjectById(Integer projectId) {
        TestProject project = null;
        for (TestProject currentProject : this.getProjects()) {
            if (currentProject.getId() == projectId) {
                project = currentProject;
                break;
            }
        }

        if (project == null) {
            throw new TestLinkAPIException(
                    "Test Project with id " + projectId + " not found");
        } else {
            return project;
        }
    }

    public TestProject createProject(TestProject project) {
        return this.api.createTestProject(project.getName(),
                project.getPrefix(), project.getNotes(),
                project.isEnableRequirements(), project.isEnableTestPriority(),
                project.isEnableAutomation(), project.isEnableInventory(),
                project.isActive(), project.isPublic());
    }

    /* ***********************************************************************/
    /* ***************************** Test Plans ******************************/
    /* ***********************************************************************/

    public TestPlan[] getProjectTestPlans(Integer projectId) {
        return this.api.getProjectTestPlans(projectId);
    }

    public TestPlan getTestPlanByName(String planName, String projectName) {
        return this.api.getTestPlanByName(planName, projectName);
    }

    public TestPlan createTestPlan(TestPlan plan) {
        return this.api.createTestPlan(plan.getName(), plan.getProjectName(),
                plan.getNotes(), plan.isActive(), plan.isPublic());
    }

    /* **********************************************************************/
    /* **************************** Plan Builds *****************************/
    /* **********************************************************************/

    public Build[] getTestPlansBuilds(Integer planId) {
        return this.api.getBuildsForTestPlan(planId);
    }

    public Build getLatestPlanBuild(Integer planId) {
        return this.api.getLatestBuildForTestPlan(planId);
    }

    /* ************************************************************************/
    /* ***************************** Test Suites ******************************/
    /* ************************************************************************/

    public TestSuite[] getTestSuitesForTestPlan(Integer testPlanId) {
        return this.api.getTestSuitesForTestPlan(testPlanId);
    }

    public TestSuite[] getProjectTestSuites(Integer projectId) {
        return this.api.getFirstLevelTestSuitesForTestProject(projectId);
    }

    public TestSuite getTestSuiteById(Integer suiteId) {
        List<Integer> testSuitesIds = new ArrayList<>();
        testSuitesIds.add(suiteId);
        TestSuite[] testSuites = this.api.getTestSuiteByID(testSuitesIds);
        if (testSuites.length == 1) {
            return testSuites[0];
        }

        return null;
    }

    public TestSuite createTestSuite(TestSuite suite) {
        return this.api.createTestSuite(suite.getTestProjectId(),
                suite.getName(), suite.getDetails(), suite.getParentId(),
                suite.getOrder(), suite.getCheckDuplicatedName(),
                suite.getActionOnDuplicatedName());
    }

    /* ***********************************************************************/
    /* ***************************** Test Cases ******************************/
    /* ***********************************************************************/

    public TestCase[] getTestCasesForTestPlan(Integer testPlanId,
            Integer buildId) {
        return this.api.getTestCasesForTestPlan(testPlanId, null, buildId, null,
                null, null, null, null, null, true, null);
    }

    public TestCase getTestCaseById(Integer suiteId, Integer caseId) {
        TestCase foundTestCase = null;
        try {
            TestCase[] testCases = getSuiteTestCases(suiteId);
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

    public TestCase[] getSuiteTestCases(Integer suiteId) {
        return this.api.getTestCasesForTestSuite(suiteId, true,
                TestCaseDetails.FULL);
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

    public ReportTCResultResponse executeTest(Integer testCaseId,
            Integer testPlanId, Integer buildId, String notes,
            ExecutionStatus status) {
        ReportTCResultResponse response = this.api.reportTCResult(testCaseId,
                null, testPlanId, status, buildId, null, notes, null, null,
                null, null, null, null);

        return response;
    }

}