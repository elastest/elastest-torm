/*
 * (C) Copyright 2017-2019 ElasTest (http://elastest.io/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package io.elastest.etm.test.base.testlink;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.eti.kinoshita.testlinkjavaapi.model.Build;
import br.eti.kinoshita.testlinkjavaapi.model.TestCase;
import br.eti.kinoshita.testlinkjavaapi.model.TestPlan;
import br.eti.kinoshita.testlinkjavaapi.model.TestProject;
import br.eti.kinoshita.testlinkjavaapi.model.TestSuite;
import io.elastest.etm.test.base.EtmBaseTest;

public class TestLinkBaseTest extends EtmBaseTest {

    final Logger log = getLogger(lookup().lookupClass());

    protected String tlApiPath = "/testlink";

    protected void navigateToTestlinkPage(WebDriver driver) {
        this.navigateToTorm(driver);
        log.info("Navigate to TestLink Section");

        driver.findElement(By.xpath("//a[@id='nav_testlink']")).click();
    }

    /* *************** */
    /* *** Project *** */
    /* *************** */

    protected TestProject getTLTestProject(WebDriver driver, String projectName)
            throws JsonParseException, JsonMappingException, IOException {
        ResponseEntity<String> response = this.restClient
                .get(tlApiPath + "/project/name/" + projectName);

        return this.getObjectFromJson(response.getBody(), TestProject.class);
    }

    protected boolean tlTestProjectExists(WebDriver driver, String projectName)
            throws JsonParseException, JsonMappingException, IOException {
        return this.getTLTestProject(driver, projectName) != null;
    }

    protected TestProject createTlTestProject(WebDriver driver,
            TestProject project) throws IOException {
        if (this.tlTestProjectExists(driver, project.getName())) {
            return this.getTLTestProject(driver, project.getName());
        } else {
            String jsonPj = this.objectToJson(project);
            ResponseEntity<String> response = this.restClient
                    .post(tlApiPath + "/project", jsonPj);

            return this.getObjectFromJson(response.getBody(),
                    TestProject.class);
        }
    }

    /* ************* */
    /* *** Suite *** */
    /* ************* */

    protected TestSuite getTLTestSuite(WebDriver driver, String suiteName)
            throws JsonParseException, JsonMappingException, IOException {
        ResponseEntity<String> response = this.restClient
                .get(tlApiPath + "/project/suite/name/" + suiteName);

        return this.getObjectFromJson(response.getBody(), TestSuite.class);
    }

    protected boolean tlTestSuiteExists(WebDriver driver, String suiteName)
            throws JsonParseException, JsonMappingException, IOException {
        return this.getTLTestSuite(driver, suiteName) != null;
    }

    protected TestSuite createTlTestSuite(WebDriver driver, TestSuite suite)
            throws IOException {
        if (this.tlTestSuiteExists(driver, suite.getName())) {
            return this.getTLTestSuite(driver, suite.getName());
        } else {
            String jsonSuite = this.objectToJson(suite);
            ResponseEntity<String> response = this.restClient.post(tlApiPath
                    + "/project/" + suite.getTestProjectId() + "/suite",
                    jsonSuite);

            return this.getObjectFromJson(response.getBody(), TestSuite.class);
        }
    }

    /* **************** */
    /* *** TestCase *** */
    /* **************** */

    protected TestCase getTLTestCase(WebDriver driver, String caseName,
            Integer suiteId)
            throws JsonParseException, JsonMappingException, IOException {
        ResponseEntity<String> response = this.restClient.get(tlApiPath
                + "/project/suite/" + suiteId + "/case/name/" + caseName);

        return this.getTestCaseFromJson(response.getBody());
    }

    protected TestCase getTLTestCase(WebDriver driver, TestCase testCase)
            throws JsonParseException, JsonMappingException, IOException {
        return this.getTLTestCase(driver, testCase.getName(),
                testCase.getTestSuiteId());
    }

    protected boolean tlTestCaseExists(WebDriver driver, String caseName,
            Integer suiteId)
            throws JsonParseException, JsonMappingException, IOException {
        return this.getTLTestCase(driver, caseName, suiteId) != null;
    }

    protected boolean tlTestCaseExists(WebDriver driver, TestCase testCase)
            throws JsonParseException, JsonMappingException, IOException {
        return this.getTLTestCase(driver, testCase) != null;
    }

    protected TestCase createTlTestCase(WebDriver driver, TestCase testCase)
            throws IOException {
        if (this.tlTestCaseExists(driver, testCase)) {
            return this.getTLTestCase(driver, testCase);
        } else {
            String jsonCase = this.objectToJson(testCase);
            ResponseEntity<String> response = this.restClient.post(tlApiPath
                    + "/project/suite/" + testCase.getTestSuiteId() + "/case",
                    jsonCase);
            return this.getTestCaseFromJson(response.getBody());
        }
    }

    protected Integer addTLTestCaseToTestPlan(WebDriver driver,
            TestCase testCase, Integer planId)
            throws JsonParseException, JsonMappingException, IOException {
        String jsonCase = this.objectToJson(testCase);
        ResponseEntity<String> response = this.restClient.post(
                tlApiPath + "/project/plan/" + planId + "/case/add", jsonCase);

        return this.getObjectFromJson(response.getBody(), Integer.class);
    }

    /* **************** */
    /* *** TestPlan *** */
    /* **************** */

    protected TestPlan getTLTestPlan(WebDriver driver, String planName)
            throws JsonParseException, JsonMappingException, IOException {
        ResponseEntity<String> response = this.restClient
                .get(tlApiPath + "/project/plan/name/" + planName);
        return this.getObjectFromJson(response.getBody(), TestPlan.class);
    }

    protected boolean tlTestPlanExists(WebDriver driver, String planName)
            throws JsonParseException, JsonMappingException, IOException {
        return this.getTLTestPlan(driver, planName) != null;
    }

    protected TestPlan createTlTestPlan(WebDriver driver, TestPlan testPlan)
            throws IOException {
        if (this.tlTestPlanExists(driver, testPlan.getName())) {
            return this.getTLTestPlan(driver, testPlan.getName());
        } else {
            String jsonPlan = this.objectToJson(testPlan);

            ResponseEntity<String> response = this.restClient
                    .post(tlApiPath + "/project/plan", jsonPlan);

            return this.getObjectFromJson(response.getBody(), TestPlan.class);
        }
    }

    /* ************* */
    /* *** Build *** */
    /* ************* */

    protected Build getTLBuild(WebDriver driver, String buildName)
            throws JsonParseException, JsonMappingException, IOException {
        ResponseEntity<String> response = this.restClient
                .get(tlApiPath + "/project/plan/build/name/" + buildName);

        return this.getObjectFromJson(response.getBody(), Build.class);
    }

    protected boolean tlBuildExists(WebDriver driver, String buildName)
            throws JsonParseException, JsonMappingException, IOException {
        return this.getTLBuild(driver, buildName) != null;
    }

    protected Build createTlBuild(WebDriver driver, Build build)
            throws IOException {
        if (this.tlBuildExists(driver, build.getName())) {
            return this.getTLBuild(driver, build.getName());
        } else {
            String jsonBuild = this.objectToJson(build);
            ResponseEntity<String> response = this.restClient
                    .post(tlApiPath + "/project/plan/build", jsonBuild);

            return this.getObjectFromJson(response.getBody(), Build.class);
        }
    }

    /* ************* */
    /* *** Utils *** */
    /* ************* */

    protected <T> T getObjectFromJson(String json, Class<T> clazz)
            throws JsonParseException, JsonMappingException, IOException {
        if (json != null) {
            ObjectMapper mapper = new ObjectMapper();
            mapper.setSerializationInclusion(Include.NON_EMPTY);
            return mapper.readValue(json, clazz);
        }
        return null;
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
