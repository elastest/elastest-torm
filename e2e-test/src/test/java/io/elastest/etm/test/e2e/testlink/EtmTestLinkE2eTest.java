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
package io.elastest.etm.test.e2e.testlink;

import static io.github.bonigarcia.BrowserType.CHROME;
import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;

import br.eti.kinoshita.testlinkjavaapi.constants.ActionOnDuplicate;
import br.eti.kinoshita.testlinkjavaapi.model.Build;
import br.eti.kinoshita.testlinkjavaapi.model.TestCase;
import br.eti.kinoshita.testlinkjavaapi.model.TestPlan;
import br.eti.kinoshita.testlinkjavaapi.model.TestProject;
import br.eti.kinoshita.testlinkjavaapi.model.TestSuite;
import io.elastest.etm.test.base.testlink.EtmTestLinkBaseTest;
import io.elastest.etm.test.base.testlink.TestLinkBaseTest;
import io.elastest.etm.test.utils.SampleTLData;
import io.github.bonigarcia.DockerBrowser;
import io.github.bonigarcia.SeleniumExtension;

/**
 * E2E ETM TestLink test.
 *
 * @author EduJG(https://github.com/EduJGURJC)
 * @since 0.1.1
 */
@Tag("e2e")
@DisplayName("ETM E2E test of TestLink")
@ExtendWith(SeleniumExtension.class)
public class EtmTestLinkE2eTest extends EtmTestLinkBaseTest {

    final Logger log = getLogger(lookup().lookupClass());

    protected TestLinkBaseTest tlBaseTest;

    @Test
    @DisplayName("Get TestLink Url")
    void getTLUrlTest(@DockerBrowser(type = CHROME) RemoteWebDriver driver)
            throws InterruptedException {
        this.driver = driver;

        log.info("Opening ElasTest");
        this.navigateToTorm(driver);

        String url = this.getTestlinkPageUrl(driver);
        log.info("The obtained TestLink url is {}", url);
    }

    @Test
    @DisplayName("Create TestLink Data and Test In ElasTest")
    void tlDataTest(@DockerBrowser(type = CHROME) RemoteWebDriver driver)
            throws InterruptedException, IOException {
        log.info("Creating Sample Data in TestLink...");
        SampleTLData sampleTLData = this.createSampleTLDataTest(driver);

        log.info("Opening ElasTest");
        this.navigateToTorm(driver);

        log.info("Syncronizing Sample Data in TestLink...");
        this.syncTestlink(driver);

        String projectName = sampleTLData.getProject().getName();
        String suiteName = sampleTLData.getSuite().getName();
        String planName = sampleTLData.getPlan().getName();
        String buildName = sampleTLData.getBuild().getName();

        log.info("Checking if TL Project exists in ElasTest");
        this.tlEtmProjectExists(driver, projectName);
        log.info("Checking if TL Suite exists in ElasTest");
        this.tlEtmSuiteExistsByAbsolute(driver, projectName, suiteName);
        log.info("Checking if TL Plan exists in ElasTest");
        this.tlEtmPlanExistsByAbsolute(driver, projectName, planName);
        log.info("Checking if TL Build exists in ElasTest");
        this.tlEtmBuildExistsByAbsolute(driver, projectName, planName,
                buildName);

        for (TestCase currentCase : sampleTLData.getTestCases()) {
            String caseName = currentCase.getName();
            log.info("Checking if TL Test Case '{}' exists in Suite - ElasTest",
                    caseName);
            this.tlEtmSuiteCaseExists(driver, projectName, suiteName, caseName);
            log.info(
                    "Checking if TL Test Case '{}' is associated to a Plan in ElasTest",
                    caseName);
            this.tlEtmPlanCaseExists(driver, projectName, planName, caseName);
            log.info("Checking if TL Test Case '{}' exists in Build - ElasTest",
                    caseName);
            this.tlEtmBuildCaseExists(driver, projectName, planName, buildName,
                    caseName);
        }

        this.executeSampleTestPlan(driver, sampleTLData);
    }

    void executeSampleTestPlan(RemoteWebDriver driver,
            SampleTLData sampleTLData) {
        String projectName = sampleTLData.getProject().getName();
        String planName = sampleTLData.getPlan().getName();

        log.info("Executing Plan '{}' ", planName);
        this.executeTLEtmPlanWithNavigate(driver, projectName, planName);

        for (int i = 0; i < sampleTLData.getTestCases().size(); i++) {
            log.info("Executing TestCase nº {} ", i + 1);
            this.executeTLEtmPlanCurrentCase(driver);
        }
        log.info("Wait To ETM Plan Execution Ends");
        this.waitToTLEtmPlanExecutionEnds(driver);
    }

    protected SampleTLData createSampleTLDataTest(RemoteWebDriver driver)
            throws IOException {
        this.driver = driver;

        SampleTLData sampleTLData = new SampleTLData();

        // Create Project
        String pjName = "Test Sample Project";
        log.info("Creating TL Project with name: {}", pjName);
        TestProject project = new TestProject(0, pjName, "TSP",
                "This is a note", false, false, false, false, true, true);
        project = this.createTlTestProject(driver, project);

        // Create Suite
        String suiteName = "Test Sample Suite";
        log.info("Creating TL Suite with name: {}", suiteName);
        TestSuite suite = new TestSuite(0, project.getId(), suiteName,
                "There are the suite details", null, null, true,
                ActionOnDuplicate.BLOCK);
        suite = this.createTlTestSuite(driver, suite);

        // Create Plan
        String planName = "Test Sample Plan";
        log.info("Creating TL Plan with name: {}", planName);
        TestPlan plan = new TestPlan(0, planName, project.getName(),
                "This is a note", true, true);
        plan = this.createTlTestPlan(driver, plan);

        // Create Build
        String buildName = "Test Sample build";
        log.info("Creating TL Build with name: {}", buildName);
        Build build = new Build(0, plan.getId(), buildName, "This is a note");
        build = this.createTlBuild(driver, build);

        // Create TestCases
        int numberOfCases = 2;

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

            testCase = this.createTlTestCase(driver, testCase);
            testCase.setTestProjectId(project.getId());
            sampleTLData.getTestCases().add(testCase);
            log.info("Associating TL Case '{}' to Test Plan", caseName);
            this.addTLTestCaseToTestPlan(driver, testCase, plan.getId());
        }

        sampleTLData.setProject(project);
        sampleTLData.setSuite(suite);
        sampleTLData.setPlan(plan);
        sampleTLData.setBuild(build);
        return sampleTLData;
    }

}
