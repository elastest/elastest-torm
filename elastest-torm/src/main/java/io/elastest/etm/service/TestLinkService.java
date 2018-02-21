
package io.elastest.etm.service;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import br.eti.kinoshita.testlinkjavaapi.TestLinkAPI;
import br.eti.kinoshita.testlinkjavaapi.constants.ExecutionStatus;
import br.eti.kinoshita.testlinkjavaapi.constants.TestCaseDetails;
import br.eti.kinoshita.testlinkjavaapi.model.Build;
import br.eti.kinoshita.testlinkjavaapi.model.Execution;
import br.eti.kinoshita.testlinkjavaapi.model.ReportTCResultResponse;
import br.eti.kinoshita.testlinkjavaapi.model.TestCase;
import br.eti.kinoshita.testlinkjavaapi.model.TestPlan;
import br.eti.kinoshita.testlinkjavaapi.model.TestProject;
import br.eti.kinoshita.testlinkjavaapi.model.TestSuite;
import br.eti.kinoshita.testlinkjavaapi.util.TestLinkAPIException;
import io.elastest.etm.dao.external.ExternalProjectRepository;
import io.elastest.etm.dao.external.ExternalTJobRepository;
import io.elastest.etm.dao.external.ExternalTestCaseRepository;
import io.elastest.etm.dao.external.ExternalTestExecutionRepository;
import io.elastest.etm.model.SocatBindedPort;
import io.elastest.etm.model.external.ExternalProject;
import io.elastest.etm.model.external.ExternalTestCase;
import io.elastest.etm.model.external.ExternalTestExecution;
import io.elastest.etm.utils.UtilTools;
import net.minidev.json.JSONObject;
import io.elastest.etm.model.external.ExternalProject.TypeEnum;
import io.elastest.etm.model.external.ExternalTJob;

@Service
public class TestLinkService {
    private static final Logger logger = LoggerFactory
            .getLogger(TestLinkService.class);

    @Value("${et.etm.testlink.host}")
    public String etEtmTestLinkHost;

    @Value("${et.etm.testlink.port}")
    public String etEtmTestLinkPort;

    @Value("${elastest.docker.network}")
    private String etDockerNetwork;

    @Value("${et.public.host}")
    public String etPublicHost;

    @Autowired
    TestLinkDBService testLinkDBService;

    public String testLinkHost;
    public String testLinkPort;

    private final ExternalProjectRepository externalProjectRepository;
    private final ExternalTestCaseRepository externalTestCaseRepository;
    private final ExternalTestExecutionRepository externalTestExecutionRepository;
    private final ExternalTJobRepository externalTJobRepository;
    private final DockerService2 dockerService;

    String devKey = "20b9a66e17597842404062c3b628b938";
    TestLinkAPI api = null;
    URL testlinkURL = null;

    public TestLinkService(ExternalProjectRepository externalProjectRepository,
            ExternalTestCaseRepository externalTestCaseRepository,
            ExternalTestExecutionRepository externalTestExecutionRepository,
            ExternalTJobRepository externalTJobRepository,
            DockerService2 dockerService) {
        this.externalProjectRepository = externalProjectRepository;
        this.externalTestCaseRepository = externalTestCaseRepository;
        this.externalTestExecutionRepository = externalTestExecutionRepository;
        this.externalTJobRepository = externalTJobRepository;
        this.dockerService = dockerService;
    }

    @PostConstruct
    public void init() {
        if (!etEtmTestLinkHost.equals("none")) {

            try {
                // Default development
                this.testLinkHost = this.dockerService.getContainerIpByNetwork(
                        etEtmTestLinkHost, etDockerNetwork);
                this.testLinkPort = etEtmTestLinkPort;

                // If not development, start socat
                if (!UtilTools.checkIfUrlIsUp(this.getTestLinkUrl())) {
                    try {
                        String testLinkIp = UtilTools.doPing(etEtmTestLinkHost);
                        logger.info("Real TestLink Ip: {}", testLinkIp);
                        SocatBindedPort socatBindedPort = dockerService
                                .bindingPort(testLinkIp, etEtmTestLinkPort,
                                        etDockerNetwork);
                        this.testLinkHost = etPublicHost;
                        this.testLinkPort = socatBindedPort.getListenPort();
                    } catch (Exception e) {
                        logger.error("Cannot get Testlink socat data {}", e);
                        this.testLinkHost = etEtmTestLinkHost;
                        this.testLinkPort = etEtmTestLinkPort;
                    }
                }

                String url = this.getTestLinkUrl()
                        + "/lib/api/xmlrpc/v1/xmlrpc.php";

                try {
                    testlinkURL = new URL(url);
                } catch (MalformedURLException mue) {
                    mue.printStackTrace();
                }

                try {
                    api = new TestLinkAPI(testlinkURL, devKey);
                } catch (TestLinkAPIException te) {
                    logger.error(te.getMessage());
                }
            } catch (Exception e) {
                logger.error("Cannot get TL container ip", e);
            }
        }
    }

    public String getTestLinkInfo() {
        return this.api.about();
    }

    public String sayHello() {
        return this.api.ping();
    }

    public String getTestLinkUrl() {
        return "http://" + this.testLinkHost + ":" + this.testLinkPort;
    }

    /* *****************************************************************/
    /* ************************* Test Projects *************************/
    /* *****************************************************************/

    public TestProject[] getProjects() {
        TestProject[] projects = null;
        try {
            projects = this.api.getProjects();
        } catch (TestLinkAPIException e) {
            // EMPTY
        }
        return projects;
    }

    public TestProject getProjectByName(String projectName) {
        TestProject project = null;
        try {
            project = this.api.getTestProjectByName(projectName);
        } catch (TestLinkAPIException e) {
            // EMPTY
        }
        return project;

    }

    public TestProject getProjectById(Integer projectId) {
        TestProject project = null;
        try {
            for (TestProject currentProject : this.getProjects()) {
                if (currentProject.getId() == projectId) {
                    project = currentProject;
                    break;
                }
            }
        } catch (Exception e) {
        }

        if (project == null) {
            logger.error("Test Project with id " + projectId + " not found");
        }
        return project;
    }

    public TestProject createProject(TestProject project) {
        return this.api.createTestProject(project.getName(),
                project.getPrefix(), project.getNotes(),
                project.isEnableRequirements(), project.isEnableTestPriority(),
                project.isEnableAutomation(), project.isEnableInventory(),
                project.isActive(), project.isPublic());
    }

    /* ******************************************************************/
    /* ************************** Test Suites ***************************/
    /* ******************************************************************/

    public TestSuite[] getTestSuitesForTestPlan(Integer testPlanId) {
        TestSuite[] suites = null;
        try {
            suites = this.api.getTestSuitesForTestPlan(testPlanId);
        } catch (TestLinkAPIException e) {
            // EMPTY
        }
        return suites;
    }

    public TestSuite[] getProjectTestSuites(Integer projectId) {
        TestSuite[] suites = null;
        try {
            suites = this.api.getFirstLevelTestSuitesForTestProject(projectId);
        } catch (TestLinkAPIException e) {
            // EMPTY
        }
        return suites;
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

    /* *****************************************************************/
    /* ************************** Test Cases ***************************/
    /* *****************************************************************/

    public TestCase getTestCaseById(Integer caseId) {
        TestCase testCase = this.api.getTestCase(caseId, null, null);
        testCase = this.getSuiteTestCaseById(testCase.getTestSuiteId(), caseId);
        return testCase;
    }

    public TestCase getSuiteTestCaseById(Integer suiteId, Integer caseId) {
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
        TestCase[] cases = null;
        try {
            cases = this.api.getTestCasesForTestSuite(suiteId, true,
                    TestCaseDetails.FULL);
        } catch (TestLinkAPIException e) {
            // EMPTY
        }
        return cases;
    }

    public TestCase[] getPlanTestCases(Integer planId) {
        TestCase[] cases = null;
        try {
            cases = (TestCase[]) ArrayUtils.addAll(cases,
                    this.api.getTestCasesForTestPlan(planId, null, null, null,
                            null, null, null, null, null, null,
                            TestCaseDetails.FULL));
        } catch (TestLinkAPIException e) {
            // EMPTY
        }

        return cases;
    }

    public TestCase[] getPlanBuildTestCases(Integer planId) {
        TestCase[] cases = null;
        Build[] builds = this.getPlanBuilds(planId);
        if (builds != null) {
            for (Build currentBuild : builds) {
                try {
                    cases = (TestCase[]) ArrayUtils.addAll(cases,
                            this.api.getTestCasesForTestPlan(planId, null,
                                    currentBuild.getId(), null, null, null,
                                    null, null, null, null,
                                    TestCaseDetails.FULL));
                } catch (TestLinkAPIException e) {
                    // EMPTY
                }

            }
        }
        return cases;
    }

    public TestCase createTestCase(TestCase testCase) {
        return this.api.createTestCase(testCase.getName(),
                testCase.getTestSuiteId(), testCase.getTestProjectId(),
                testCase.getAuthorLogin(), testCase.getSummary(),
                testCase.getSteps(), testCase.getPreconditions(),
                testCase.getTestCaseStatus(), testCase.getTestImportance(),
                testCase.getExecutionType(), testCase.getOrder(),
                testCase.getInternalId(), testCase.getCheckDuplicatedName(),
                testCase.getActionOnDuplicatedName());
    }

    public TestCase[] getPlanBuildTestCases(Integer testPlanId,
            Integer buildId) {
        TestCase[] cases = null;
        try {
            cases = this.api.getTestCasesForTestPlan(testPlanId, null, buildId,
                    null, null, null, null, null, null, true,
                    TestCaseDetails.FULL);

            cases = this.getFullDetailedTestCases(cases); // To get more info...

        } catch (TestLinkAPIException e) {
            // EMPTY
        }

        return cases;
    }

    public TestCase[] getBuildTestCases(Build build) {
        if (build == null) {
            return null;
        }
        return this.getPlanBuildTestCases(build.getTestPlanId(), build.getId());
    }

    public TestCase[] getBuildTestCasesById(Integer buildId) {
        Build build = this.getBuildById(buildId);
        return this.getBuildTestCases(build);
    }

    public TestCase[] getProjectTestCases(Integer projectId) {
        TestCase[] testCase = null;
        for (TestSuite currentSuite : this.getProjectTestSuites(projectId)) {
            TestCase[] currentTestCases = this
                    .getSuiteTestCases(currentSuite.getId());
            if (currentTestCases != null) {
                testCase = (TestCase[]) ArrayUtils.addAll(testCase,
                        currentTestCases);
            }
        }
        return testCase;
    }

    public TestCase[] getFullDetailedTestCases(TestCase[] testCases) {
        TestCase[] fullDetailedCases = null;
        try {
            for (TestCase currentCase : testCases) {
                currentCase = this.getFullDetailedTestCase(currentCase);

                fullDetailedCases = (TestCase[]) ArrayUtils
                        .add(fullDetailedCases, currentCase);
            }
        } catch (TestLinkAPIException e) {
            // EMPTY
        }
        if (fullDetailedCases == null) {
            fullDetailedCases = testCases;
        }
        return fullDetailedCases;
    }

    public TestCase getFullDetailedTestCase(TestCase testCase) {
        TestCase auxTestCase = this.api.getTestCaseByExternalId(
                testCase.getFullExternalId(), testCase.getVersion());

        testCase.setSummary(auxTestCase.getSummary());
        testCase.setPreconditions(auxTestCase.getPreconditions());
        testCase.setOrder(auxTestCase.getOrder());

        return testCase;
    }

    /* *****************************************************************/
    /* ************************** Executions ***************************/
    /* *****************************************************************/

    public ReportTCResultResponse saveExecution(Execution execution,
            Integer testCaseId) {
        // Save TestCase Execution in Testlink
        ReportTCResultResponse response = this.executeTest(testCaseId,
                execution.getTestPlanId(), execution.getBuildId(),
                execution.getNotes(), execution.getStatus());
        execution.setId(response.getExecutionId());

        // Get Associated ExternalTestCase And Save ExternalTestCaseExecution
        ExternalTestCase externalTestCase = this.externalTestCaseRepository
                .findByExternalIdAndExternalSystemId(testCaseId.toString(),
                        this.getSystemId());

        this.syncTestCaseExec(execution, externalTestCase);

        return response;
    }

    public ReportTCResultResponse executeTest(Integer testCaseId,
            Integer testPlanId, Integer buildId, String notes,
            ExecutionStatus status) {
        ReportTCResultResponse response = this.api.reportTCResult(testCaseId,
                null, testPlanId, status, buildId, null, notes, null, null,
                null, null, null, null);
        return response;
    }

    public Execution getLastExecutionOfPlan(Integer testPlanId,
            Integer testCaseId) {
        return this.api.getLastExecutionResult(testPlanId, testCaseId, null);
    }

    public Execution[] getAllExecs() {
        return this.testLinkDBService.getAllExecs();
    }

    public Execution[] getTestCaseExecs(Integer testCaseId) {
        return this.testLinkDBService.getExecsByCase(testCaseId);
    }

    public Execution getTestExecById(Integer testCaseId, Integer testExecId) {
        return this.testLinkDBService.getTestExecById(testCaseId, testExecId);
    }

    public Execution[] getPlanTestCaseExecs(Integer testPlanId,
            Integer testCaseId) {
        return this.testLinkDBService.getExecsByPlanCase(testCaseId,
                testPlanId);
    }

    public Execution[] getBuildTestCaseExecs(Integer buildId,
            Integer testCaseId) {
        return this.testLinkDBService.getExecsByBuildCase(buildId, testCaseId);
    }

    /* *****************************************************************/
    /* ************************** Test Plans ***************************/
    /* *****************************************************************/

    public TestPlan[] getProjectTestPlans(Integer projectId) {
        TestPlan[] plans = null;

        try {
            plans = this.api.getProjectTestPlans(projectId);
        } catch (TestLinkAPIException e) {
            // EMPTY
        }
        return plans;
    }

    public TestPlan getTestPlanByName(String planName, String projectName) {
        TestPlan plan = null;
        try {
            plan = this.api.getTestPlanByName(planName, projectName);
        } catch (TestLinkAPIException e) {
            // EMPTY
        }
        return plan;
    }

    public TestPlan createTestPlan(TestPlan plan) {
        return this.api.createTestPlan(plan.getName(), plan.getProjectName(),
                plan.getNotes(), plan.isActive(), plan.isPublic());
    }

    // Additional
    public TestPlan[] getAllTestPlans() {
        TestPlan[] plans = null;
        try {
            for (TestProject currentProject : this.getProjects()) {
                TestPlan[] projectPlans = this
                        .getProjectTestPlans(currentProject.getId());
                if (projectPlans != null) {
                    plans = (TestPlan[]) ArrayUtils.addAll(plans, projectPlans);

                }
            }
        } catch (Exception e) {
        }

        return plans;
    }

    public TestPlan getTestPlanById(Integer planId) {
        TestPlan plan = null;
        try {
            TestPlan[] plans = this.getAllTestPlans();
            if (plans != null) {
                for (TestPlan currentPlan : plans) {
                    if (currentPlan.getId() == planId) {
                        plan = currentPlan;
                        break;
                    }
                }
            }
        } catch (Exception e) {
        }
        if (plan == null) {
            logger.info("Test Plan with id {} does not exist", planId);
        }
        return plan;

    }

    public TestPlan getTestPlanByPlanIdAndProjectId(Integer planId,
            Integer projectId) {
        TestPlan plan = null;
        try {
            for (TestPlan currentPlan : this.getProjectTestPlans(projectId)) {
                if (currentPlan.getId() == planId) {
                    plan = currentPlan;
                    break;
                }
            }
        } catch (TestLinkAPIException e) {
            // EMPTY
        }
        return plan;
    }

    /* ****************************************************************/
    /* ************************* Plan Builds **************************/
    /* ****************************************************************/

    public Build[] getPlanBuilds(Integer planId) {
        Build[] builds = null;
        try {
            builds = this.api.getBuildsForTestPlan(planId);
        } catch (TestLinkAPIException e) {
            // EMPTY
        }
        return builds;
    }

    public Build getLatestPlanBuild(Integer planId) {
        Build build = null;
        try {
            build = this.api.getLatestBuildForTestPlan(planId);
        } catch (TestLinkAPIException e) {
            // EMPTY
        }
        return build;
    }

    public Build createBuild(Build build) {
        return this.api.createBuild(build.getTestPlanId(), build.getName(),
                build.getNotes());
    }

    public Build[] getAllBuilds() {
        return this.testLinkDBService.getAllBuilds();
    }

    public Build getBuildById(Integer buildId) {
        Build build = null;
        try {
            Build[] builds = this.getAllBuilds();
            if (builds != null) {
                for (Build currentBuild : builds) {
                    if (currentBuild.getId() == buildId) {
                        build = currentBuild;
                        break;
                    }
                }
            }
        } catch (Exception e) {
        }
        if (build == null) {
            logger.info("Build with id {} does not exist", buildId);
        }
        return build;
    }

    public Build[] getProjectBuilds(Integer projectId) {
        Build[] build = null;
        for (TestPlan currentPlan : this.getProjectTestPlans(projectId)) {
            Build[] currentBuilds = this.getPlanBuilds(currentPlan.getId());
            if (currentBuilds != null) {
                build = (Build[]) ArrayUtils.addAll(build, currentBuilds);
            }
        }
        return build;
    }

    /* ************************************************************/
    /* ************************ External *************************/
    /* ************************************************************/

    public ExternalProject getExternalProjectByTestProjectId(
            Integer projectId) {
        String externalSystemId = this.getSystemId();
        return this.externalProjectRepository
                .findByExternalIdAndExternalSystemId(projectId.toString(),
                        externalSystemId);
    }

    public ExternalTJob getExternalTJobByPlanId(Integer planId) {
        String externalSystemId = this.getSystemId();
        return this.externalTJobRepository.findByExternalIdAndExternalSystemId(
                planId.toString(), externalSystemId);
    }

    public ExternalTestCase getExternalTestCaseByTestCaseId(
            Integer testCaseId) {
        String externalSystemId = this.getSystemId();
        return this.externalTestCaseRepository
                .findByExternalIdAndExternalSystemId(testCaseId.toString(),
                        externalSystemId);
    }

    public ExternalTestExecution getExternalTestExecByExecutionId(
            Integer execId) {
        String externalSystemId = this.getSystemId();
        return this.externalTestExecutionRepository
                .findByExternalIdAndExternalSystemId(execId.toString(),
                        externalSystemId);
    }

    /* ****************************************************************/
    /* ********************* External Conversion **********************/
    /* ****************************************************************/

    public String getSystemId() {
        return this.devKey;
    }

    public boolean syncTestLink() {
        this.syncProjects();
        return true;

    }

    /* ** Project ** */
    public void syncProjects() {
        TestProject[] projectsList = this.getProjects();
        if (projectsList != null) {
            for (TestProject currentTestProject : projectsList) {
                this.syncProject(currentTestProject);
            }
        }
    }

    public void syncProject(TestProject project) {
        ExternalProject externalProject = new ExternalProject(new Long(0));
        externalProject.setName(project.getName());
        externalProject.setType(TypeEnum.TESTLINK);
        externalProject.setExternalId(project.getId().toString());
        externalProject.setExternalSystemId(this.getSystemId());

        try {
            externalProject = externalProjectRepository.save(externalProject);
        } catch (DataIntegrityViolationException existException) {
            ExternalProject savedPj = externalProjectRepository
                    .findByExternalIdAndExternalSystemId(
                            externalProject.getExternalId(),
                            externalProject.getExternalSystemId());
            externalProject.setId(savedPj.getId());
            externalProject = externalProjectRepository.save(externalProject);
        }

        this.syncProjectTestPlans(project.getId(), externalProject);
    }

    /* ** Plan-TJob ** */

    public void syncProjectTestPlans(Integer projectId,
            ExternalProject externalProject) {
        TestPlan[] plansList = this.getProjectTestPlans(projectId);
        if (plansList != null) {
            for (TestPlan currentTestPlan : plansList) {
                this.syncProjectTestPlan(currentTestPlan, externalProject);
            }
        }
    }

    public void syncProjectTestPlan(TestPlan testPlan,
            ExternalProject externalProject) {
        ExternalTJob externalTJob = new ExternalTJob(new Long(0));
        externalTJob.setExProject(externalProject);
        externalTJob.setName(testPlan.getName());
        externalTJob.setExternalId(testPlan.getId().toString());
        externalTJob.setExternalSystemId(this.getSystemId());

        try {
            externalTJob = externalTJobRepository.save(externalTJob);
        } catch (DataIntegrityViolationException existException) {
            ExternalTJob savedTJob = externalTJobRepository
                    .findByExternalIdAndExternalSystemId(
                            externalTJob.getExternalId(),
                            externalTJob.getExternalSystemId());

            externalTJob.setId(savedTJob.getId());
            externalTJob.setSut(savedTJob.getSut());
            externalTJob
                    .setExecDashboardConfig(savedTJob.getExecDashboardConfig());

            externalTJob = externalTJobRepository.save(externalTJob);
        }
        this.syncTestPlanCases(testPlan.getId(), externalTJob);
    }

    /* ** TestCase ** */

    public void syncTestPlanCases(Integer planId, ExternalTJob externalTJob) {
        TestCase[] casesList = this.getPlanTestCases(planId);
        if (casesList != null) {
            for (TestCase currentTestCase : casesList) {
                this.syncProjectTestCase(currentTestCase, externalTJob);
            }
        }
    }

    public void syncProjectTestCase(TestCase testCase,
            ExternalTJob externalTJob) {
        ExternalTestCase externalTestCase = new ExternalTestCase(new Long(0));
        externalTestCase.setExTJob(externalTJob);
        externalTestCase.setName(testCase.getName());
        externalTestCase.setFields(this.getTestCaseFields(testCase));
        externalTestCase.setExternalId(testCase.getId().toString());
        externalTestCase.setExternalSystemId(this.getSystemId());

        try {
            externalTestCase = externalTestCaseRepository
                    .save(externalTestCase);
        } catch (DataIntegrityViolationException existException) {
            ExternalTestCase savedTestCase = externalTestCaseRepository
                    .findByExternalIdAndExternalSystemId(
                            externalTestCase.getExternalId(),
                            externalTestCase.getExternalSystemId());
            externalTestCase.setId(savedTestCase.getId());
            externalTestCase = externalTestCaseRepository
                    .save(externalTestCase);
        }
        this.syncTestCaseExecs(testCase.getId(), externalTestCase);
    }

    /* ** Exec ** */
    public void syncTestCaseExecs(Integer testCaseId,
            ExternalTestCase externalTestCase) {
        Execution[] execsList = this.getTestCaseExecs(testCaseId);
        if (execsList != null) {
            for (Execution currentExec : execsList) {
                this.syncTestCaseExec(currentExec, externalTestCase);
            }
        }
    }

    public void syncTestCaseExec(Execution exec,
            ExternalTestCase externalTestCase) {
        ExternalTestExecution externalTestExec = new ExternalTestExecution(
                new Long(0));
        externalTestExec.setExTestCase(externalTestCase);
        externalTestExec.setFields(this.getTestExecFields(exec));
        externalTestExec.setResult(exec.getStatus().name());
        externalTestExec.setExternalId(exec.getId().toString());
        externalTestExec.setExternalSystemId(this.getSystemId());
        try {
            externalTestExec = externalTestExecutionRepository
                    .save(externalTestExec);
        } catch (DataIntegrityViolationException existException) {
            ExternalTestExecution savedTestExec = externalTestExecutionRepository
                    .findByExternalIdAndExternalSystemId(
                            externalTestExec.getExternalId(),
                            externalTestExec.getExternalSystemId());
            externalTestExec.setId(savedTestExec.getId());
            externalTestExec = externalTestExecutionRepository
                    .save(externalTestExec);
        }
    }

    /* *** Conversion utils *** */

    public String getTestCaseFields(TestCase testCase) {
        JSONObject fields = new JSONObject();
        JSONObject suite = new JSONObject();
        suite.put("id", testCase.getTestSuiteId());

        fields.put("suite", suite);
        return fields.toString();
    }

    public String getTestExecFields(Execution exec) {
        JSONObject fields = new JSONObject();
        JSONObject plan = new JSONObject();
        JSONObject build = new JSONObject();

        plan.put("id", exec.getTestPlanId());
        build.put("id", exec.getBuildId());

        fields.put("plan", plan);
        fields.put("build", build);
        return fields.toString();
    }

}