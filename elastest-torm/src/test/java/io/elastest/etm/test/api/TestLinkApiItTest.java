package io.elastest.etm.test.api;

import static java.lang.invoke.MethodHandles.lookup;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
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

    protected String tlApiPath = "/api/testlink";

    @Test
    @DisplayName("Get TestLink Url")
    void getTestLinkUrlTest() throws InterruptedException, IOException {
        String url = this.httpClient
                .getForEntity(tlApiPath + "/url", String.class).getBody();

        assertNotNull(url);
    }

    @Test
    @DisplayName("Create TestLink Data and Test In ElasTest (Integration Test)")
    @Disabled
    void createSampleTLDataTest() throws InterruptedException, IOException {
        log.info("Creating Sample Data in TestLink...");

        // Create Project
        String pjName = "Test Sample Project";
        log.info("Creating TL Project with name: {}", pjName);
        TestProject project = new TestProject(0, pjName, "TSP",
                "This is a note", false, false, false, false, true, true);
        project = this.createTlTestProject(project);
        assertNotNull(project);

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
            testCase.setAuthorLogin("elastest");
            testCase.setSummary("This is a Summary");
            testCase.setPreconditions("This is a precondition");
            testCase.setKeywords(new ArrayList<>());

            testCase = this.createTlTestCase(testCase);
            testCase.setTestProjectId(project.getId());
            log.info("Associating TL Case '{}' to Test Plan", testCase);
            this.addTLTestCaseToTestPlan(testCase, plan.getId());
            cases.add(testCase);
        }

        assertTrue(this.syncTestLink());

        log.debug("TL Data: ");
        log.debug("Project:{} ", project);
        assertNotNull(project);
        log.debug("Suite:{} ", suite);
        assertNotNull(suite);
        log.debug("Plan:{} ", plan);
        assertNotNull(plan);
        log.debug("build:{} ", build);
        assertNotNull(build);
        log.debug("Cases:{} ", cases);
        assertThat(cases.size() > 0);

        log.debug("Testing other api methods...");

        log.debug("Getting Project By Id ({})", project.getId());
        project = this.getTLTestProjectById(project.getId());
        assertNotNull(project);
        log.debug("Getting Project By Name ({})", project.getName());
        project = this.getTLTestProjectByName(project.getName());
        assertNotNull(project);

        log.debug("Getting Suite By Id ({})", suite.getId());
        suite = this.getTLTestSuiteById(suite.getId());
        assertNotNull(suite);
//        log.debug("Getting Suite By Name ({})", suite.getName());
//        suite = this.getTLTestSuiteByName(suite.getName(), project.getId());
//        assertNotNull(suite);

        for (TestCase tCase : cases) {
            log.debug("Getting Test Case By Id ({})", tCase.getId());
            tCase = this.getTLTestCaseById(tCase.getId());
            assertNotNull(tCase);
            // log.debug("Getting Test Case By Name ({})", tCase.getName());
            // tCase = this.getTLTestCaseByName(tCase.getName(), suite.getId());
            // assertNotNull(tCase);
        }
    }

    /* ******************************************************* */
    /* ************************* Api ************************* */
    /* ******************************************************* */

    /* *************** */
    /* *** Project *** */
    /* *************** */

    protected TestProject getTLTestProjectByName(String projectName)
            throws JsonParseException, JsonMappingException, IOException {
        return this.httpClient
                .getForEntity(tlApiPath + "/project/name/" + projectName,
                        TestProject.class)
                .getBody();
    }

    protected TestProject getTLTestProjectById(Integer projectId)
            throws JsonParseException, JsonMappingException, IOException {
        return this.httpClient.getForEntity(tlApiPath + "/project/" + projectId,
                TestProject.class).getBody();
    }

    protected boolean tlTestProjectExists(String projectName)
            throws JsonParseException, JsonMappingException, IOException {
        return this.getTLTestProjectByName(projectName) != null;
    }

    protected TestProject createTlTestProject(TestProject project)
            throws IOException {
        if (this.tlTestProjectExists(project.getName())) {
            return this.getTLTestProjectByName(project.getName());
        } else {
            return this.httpClient.postForEntity(tlApiPath + "/project",
                    project, TestProject.class).getBody();
        }
    }

    /* ************* */
    /* *** Suite *** */
    /* ************* */

    protected TestSuite getTLTestSuiteByName(String suiteName,
            Integer projectId)
            throws JsonParseException, JsonMappingException, IOException {
        return this.httpClient
                .getForEntity(tlApiPath + "/project/" + projectId
                        + "/suite/name/" + suiteName, TestSuite.class)
                .getBody();
    }

    protected TestSuite getTLTestSuiteById(Integer suiteId)
            throws JsonParseException, JsonMappingException, IOException {
        return this.httpClient
                .getForEntity(tlApiPath + "/project/suite/" + suiteId,
                        TestSuite.class)
                .getBody();
    }

    protected boolean tlTestSuiteExists(TestSuite suite)
            throws JsonParseException, JsonMappingException, IOException {
        return this.getTLTestSuiteByName(suite.getName(),
                suite.getTestProjectId()) != null;
    }

    protected TestSuite createTlTestSuite(TestSuite suite) throws IOException {
        if (this.tlTestSuiteExists(suite)) {
            return this.getTLTestSuiteByName(suite.getName(),
                    suite.getTestProjectId());
        } else {
            return this.httpClient.postForEntity(tlApiPath + "/project/"
                    + suite.getTestProjectId() + "/suite", suite,
                    TestSuite.class).getBody();
        }
    }

    /* **************** */
    /* *** TestCase *** */
    /* **************** */

    protected TestCase getTLTestCaseById(Integer caseId)
            throws JsonParseException, JsonMappingException, IOException {
        String jsonTCase = this.httpClient
                .getForEntity(tlApiPath + "/project/suite/case/" + caseId,
                        String.class)
                .getBody();
        return this.getTestCaseFromJson(jsonTCase);
    }

    protected TestCase getTLTestCaseByName(String caseName, Integer suiteId)
            throws JsonParseException, JsonMappingException, IOException {
        String jsonTCase = this.httpClient
                .getForEntity(tlApiPath + "/project/suite/" + suiteId
                        + "/case/name/" + caseName, String.class)
                .getBody();
        return this.getTestCaseFromJson(jsonTCase);
    }

    protected TestCase getTLTestCase(TestCase testCase)
            throws JsonParseException, JsonMappingException, IOException {
        return this.getTLTestCaseByName(testCase.getName(),
                testCase.getTestSuiteId());
    }

    protected boolean tlTestCaseExists(String caseName, Integer suiteId)
            throws JsonParseException, JsonMappingException, IOException {
        return this.getTLTestCaseByName(caseName, suiteId) != null;
    }

    protected boolean tlTestCaseExists(TestCase testCase)
            throws JsonParseException, JsonMappingException, IOException {
        return this.getTLTestCase(testCase) != null;
    }

    protected TestCase createTlTestCase(TestCase testCase) throws IOException {
        if (this.tlTestCaseExists(testCase)) {
            return this.getTLTestCase(testCase);
        } else {
            HttpEntity<String> jsonCaseRequest = this
                    .getTestCaseRequest(testCase);
            ResponseEntity<String> response = this.httpClient.exchange(
                    tlApiPath + "/project/suite/" + testCase.getTestSuiteId()
                            + "/case",
                    HttpMethod.POST, jsonCaseRequest, String.class);
            return this.getTestCaseFromJson(response.getBody());
        }
    }

    protected Integer addTLTestCaseToTestPlan(TestCase testCase, Integer planId)
            throws JsonParseException, JsonMappingException, IOException {
        HttpEntity<String> jsonCaseRequest = this.getTestCaseRequest(testCase);

        ResponseEntity<String> responseEntity = this.httpClient.exchange(
                tlApiPath + "/project/plan/" + planId + "/case/add",
                HttpMethod.POST, jsonCaseRequest, String.class);
        return this.getObjectFromJson(responseEntity.getBody(), Integer.class);
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
            return this.httpClient.postForEntity(tlApiPath + "/project/plan",
                    testPlan, TestPlan.class).getBody();
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
            return this.httpClient
                    .postForEntity(tlApiPath + "/project/plan/build", build,
                            Build.class)
                    .getBody();
        }
    }

    /* ************* */
    /* *** Utils *** */
    /* ************* */

    protected boolean syncTestLink() {
        return this.httpClient.getForEntity(tlApiPath + "/sync", boolean.class)
                .getBody();
    }

    protected <T> T getObjectFromJson(String json, Class<T> clazz)
            throws JsonParseException, JsonMappingException, IOException {
        if (json != null) {
            ObjectMapper mapper = new ObjectMapper();
            mapper.setSerializationInclusion(Include.NON_EMPTY);
            return mapper.readValue(json, clazz);
        }
        return null;
    }

    protected HttpEntity<String> getTestCaseRequest(TestCase testCase)
            throws JsonProcessingException {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        headers.add("Accept", "*/*");

        String jsonCase = this.objectToJson(testCase);

        return new HttpEntity<String>(jsonCase, headers);
    }

    protected TestCase getTestCaseFromJson(String json)
            throws JsonParseException, JsonMappingException, IOException {
        // Fix for fails on get keyword
        @SuppressWarnings("unchecked")
        Map<String, Object> tcMap = this.getObjectFromJson(json, Map.class);
        if (tcMap != null && tcMap.containsKey("keywords")) {
            tcMap.remove("keywords");
        }

        return this.getObjectFromJson(this.objectToJson(tcMap), TestCase.class);
    }

    protected String objectToJson(Object obj) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(Include.NON_EMPTY);
        return mapper.writeValueAsString(obj);
    }
}
