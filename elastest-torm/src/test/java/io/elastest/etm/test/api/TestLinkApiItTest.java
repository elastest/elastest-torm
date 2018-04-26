package io.elastest.etm.test.api;

import static java.lang.invoke.MethodHandles.lookup;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.eti.kinoshita.testlinkjavaapi.constants.ActionOnDuplicate;
import br.eti.kinoshita.testlinkjavaapi.model.Build;
import br.eti.kinoshita.testlinkjavaapi.model.TestCase;
import br.eti.kinoshita.testlinkjavaapi.model.TestPlan;
import br.eti.kinoshita.testlinkjavaapi.model.TestProject;
import br.eti.kinoshita.testlinkjavaapi.model.TestSuite;
import io.elastest.etm.ElasTestTormApp;;

@RunWith(JUnitPlatform.class)
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = ElasTestTormApp.class, webEnvironment = WebEnvironment.RANDOM_PORT)
public class TestLinkApiItTest extends EtmApiItTest {
    final Logger log = getLogger(lookup().lookupClass());

    protected String tlApiPath = "/testlink";

    @Test
    @DisplayName("Create TestLink Data and Test In ElasTest (Integration Test)")
    void createSampleTLDataTest() throws InterruptedException, IOException {
        log.info("Creating Sample Data in TestLink...");

        // Create Project
        String pjName = "Test Sample Project";
        log.info("Creating TL Project with name: {}", pjName);
        TestProject project = new TestProject(0, pjName, "TSP",
                "This is a note", false, false, false, false, true, true);
        project = this.createTlTestProject(project);

        // Create Suite
        String suiteName = "Test Sample Suite";
        log.info("Creating TL Suite with name: {}", suiteName);
        TestSuite suite = new TestSuite(0, project.getId(), suiteName,
                "There are the suite details", null, null, true,
                ActionOnDuplicate.BLOCK);
        suite = this.createTlTestSuite(suite);

        // Create Plan
        String planName = "Test Sample Plan";
        log.info("Creating TL Plan with name: {}", planName);
        TestPlan plan = new TestPlan(0, planName, project.getName(),
                "This is a note", true, true);
        plan = this.createTlTestPlan(plan);

        // Create Build
        String buildName = "Test Sample build";
        log.info("Creating TL Build with name: {}", buildName);
        Build build = new Build(0, plan.getId(), buildName, "This is a note");
        build = this.createTlBuild(build);

        // Create TestCases
        int numberOfCases = 2;

        List<TestCase> cases = new ArrayList<>();
        for (int i = 0; i < numberOfCases; i++) {
            String caseName = "Test Sample Case " + (i + 1);
            log.info("Creating TL Case with name: {}", caseName);
            TestCase testCase = new TestCase();
            testCase.setId(0);
            testCase.setName(caseName);
            testCase.setTestSuiteId(suite.getId());
            testCase.setTestProjectId(project.getId());
            testCase.setAuthorLogin("admin");
            testCase.setSummary("This is a Summary");
            testCase.setPreconditions("This is a precondition");

            testCase = this.createTlTestCase(testCase);
            testCase.setTestProjectId(project.getId());
            log.info("Associating TL Case '{}' to Test Plan", caseName);
            this.addTLTestCaseToTestPlan(testCase, plan.getId());
            cases.add(testCase);
        }

        assertNotNull(project);
        assertNotNull(suite);
        assertNotNull(plan);
        assertNotNull(build);
        assertThat(cases.size() > 0);
    }

    /* ************************* Api ************************* */

    /* *************** */
    /* *** Project *** */
    /* *************** */

    protected TestProject getTLTestProject(String projectName)
            throws JsonParseException, JsonMappingException, IOException {
        return this.httpClient
                .getForEntity(tlApiPath + "/project/name/" + projectName,
                        TestProject.class)
                .getBody();
    }

    protected boolean tlTestProjectExists(String projectName)
            throws JsonParseException, JsonMappingException, IOException {
        return this.getTLTestProject(projectName) != null;
    }

    protected TestProject createTlTestProject(TestProject project)
            throws IOException {
        if (this.tlTestProjectExists(project.getName())) {
            return this.getTLTestProject(project.getName());
        } else {
            String jsonPj = this.objectToJson(project);
            return this.httpClient.postForEntity(tlApiPath + "/project", jsonPj,
                    TestProject.class).getBody();
        }
    }

    /* ************* */
    /* *** Suite *** */
    /* ************* */

    protected TestSuite getTLTestSuite(String suiteName)
            throws JsonParseException, JsonMappingException, IOException {
        return this.httpClient
                .getForEntity(tlApiPath + "/project/suite/name/" + suiteName,
                        TestSuite.class)
                .getBody();
    }

    protected boolean tlTestSuiteExists(String suiteName)
            throws JsonParseException, JsonMappingException, IOException {
        return this.getTLTestSuite(suiteName) != null;
    }

    protected TestSuite createTlTestSuite(TestSuite suite) throws IOException {
        if (this.tlTestSuiteExists(suite.getName())) {
            return this.getTLTestSuite(suite.getName());
        } else {
            String jsonSuite = this.objectToJson(suite);
            return this.httpClient.postForEntity(tlApiPath + "/project/"
                    + suite.getTestProjectId() + "/suite", jsonSuite,
                    TestSuite.class).getBody();
        }
    }

    /* **************** */
    /* *** TestCase *** */
    /* **************** */

    protected TestCase getTLTestCase(String caseName, Integer suiteId)
            throws JsonParseException, JsonMappingException, IOException {
        return this.httpClient.getForEntity(tlApiPath + "/project/suite/"
                + suiteId + "/case/name/" + caseName, TestCase.class).getBody();
    }

    protected TestCase getTLTestCase(TestCase testCase)
            throws JsonParseException, JsonMappingException, IOException {
        return this.getTLTestCase(testCase.getName(),
                testCase.getTestSuiteId());
    }

    protected boolean tlTestCaseExists(String caseName, Integer suiteId)
            throws JsonParseException, JsonMappingException, IOException {
        return this.getTLTestCase(caseName, suiteId) != null;
    }

    protected boolean tlTestCaseExists(TestCase testCase)
            throws JsonParseException, JsonMappingException, IOException {
        return this.getTLTestCase(testCase) != null;
    }

    protected TestCase createTlTestCase(TestCase testCase) throws IOException {
        if (this.tlTestCaseExists(testCase)) {
            return this.getTLTestCase(testCase);
        } else {
            String jsonCase = this.objectToJson(testCase);
            return this.httpClient
                    .postForEntity(
                            tlApiPath + "/project/suite/"
                                    + testCase.getTestSuiteId() + "/case",
                            jsonCase, TestCase.class)
                    .getBody();
        }
    }

    protected void addTLTestCaseToTestPlan(TestCase testCase, Integer planId)
            throws JsonParseException, JsonMappingException, IOException {
        String jsonCase = this.objectToJson(testCase);
        this.httpClient.postForEntity(
                tlApiPath + "/project/plan/" + planId + "/case/add", jsonCase,
                Object.class);
    }

    /* **************** */
    /* *** TestPlan *** */
    /* **************** */

    protected TestPlan getTLTestPlan(String planName)
            throws JsonParseException, JsonMappingException, IOException {
        return this.httpClient
                .getForEntity(tlApiPath + "/project/plan/name/" + planName,
                        TestPlan.class)
                .getBody();
    }

    protected boolean tlTestPlanExists(String planName)
            throws JsonParseException, JsonMappingException, IOException {
        return this.getTLTestPlan(planName) != null;
    }

    protected TestPlan createTlTestPlan(TestPlan testPlan) throws IOException {
        if (this.tlTestPlanExists(testPlan.getName())) {
            return this.getTLTestPlan(testPlan.getName());
        } else {
            String jsonPlan = this.objectToJson(testPlan);

            return this.httpClient.postForEntity(tlApiPath + "/project/plan",
                    jsonPlan, TestPlan.class).getBody();
        }
    }

    /* ************* */
    /* *** Build *** */
    /* ************* */

    protected Build getTLBuild(String buildName)
            throws JsonParseException, JsonMappingException, IOException {
        return this.httpClient.getForEntity(
                tlApiPath + "/project/plan/build/name/" + buildName,
                Build.class).getBody();
    }

    protected boolean tlBuildExists(String buildName)
            throws JsonParseException, JsonMappingException, IOException {
        return this.getTLBuild(buildName) != null;
    }

    protected Build createTlBuild(Build build) throws IOException {
        if (this.tlBuildExists(build.getName())) {
            return this.getTLBuild(build.getName());
        } else {
            String jsonBuild = this.objectToJson(build);
            return this.httpClient
                    .postForEntity(tlApiPath + "/project/plan/build", jsonBuild,
                            Build.class)
                    .getBody();
        }
    }

    /* ************* */
    /* *** Utils *** */
    /* ************* */

    protected String objectToJson(Object obj) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(Include.NON_EMPTY);
        return mapper.writeValueAsString(obj);
    }
}
