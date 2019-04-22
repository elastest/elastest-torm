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
package io.elastest.etm.test.e2e.logcomparator;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;

import io.elastest.etm.test.base.EtmBaseTest;
import io.github.bonigarcia.seljup.BrowserType;
import io.github.bonigarcia.seljup.DockerBrowser;
import io.github.bonigarcia.seljup.SeleniumExtension;

/**
 * Test that executes an existent TJob and checks the logs in LogAnalyzer when
 * the execution has finished. Requirements tested: ETM6, ETM7, ETM8, ETM10,
 * ETM18
 *
 * @author EduJG(https://github.com/EduJGURJC)
 * @since 0.1.1
 */
@Tag("e2e")
@DisplayName("LogComparator E2E tests")
@ExtendWith(SeleniumExtension.class)
public class EtmLogComparatorE2eTest extends EtmBaseTest {
    String projectName = "Unit Tests";
    String tJobName = "LogComparator JUnit5 Unit Test";

    void createProject(WebDriver driver) throws InterruptedException {
        navigateToTorm(driver);
        if (!etProjectExists(driver, projectName)) {
            createNewETProject(driver, projectName);
        }
    }

    void createTJob(WebDriver driver) throws InterruptedException {
        navigateToETProject(driver, projectName);
        if (etTJobExistsIntoProject(driver, projectName, tJobName)) {
            navigateToTJobFromETProjectPage(driver, tJobName);
            deleteTJob(driver, tJobName);
            navigateToETProject(driver, projectName);
        }

        String tJobTestResultPath = "/demo-projects/unit/junit5-unit-test/target/surefire-reports";
        String sutName = null;
        String tJobImage = "elastest/test-etm-alpinegitjava";
        String commands = "git clone https://github.com/elastest/demo-projects; cd /demo-projects/unit/junit5-unit-test; mvn -B test;";
        Map<String, String> params = new HashMap<>();
        params.put("LEFT", "15");
        params.put("RIGHT", "10");
        params.put("EXPECTED_SUM_RESULT", "25");
        params.put("EXPECTED_SUB_RESULT", "5");

        createNewTJob(driver, tJobName, tJobTestResultPath, sutName, tJobImage,
                false, commands, params, null, null, null);

    }

    @Test
    @DisplayName("Executes a TJob twice (one success and one failed) and compares the logs")
    void testExecuteAndCompareLogsWithLogComparator(
            @DockerBrowser(type = BrowserType.CHROME) RemoteWebDriver localDriver,
            TestInfo testInfo) throws Exception {
        setupTestBrowser(testInfo, BrowserType.CHROME, localDriver);

        // Setting up the TJob used in the test
        this.createProject(driver);
        this.createTJob(driver);

        // Prepare test
        navigateToRoot(driver);
        navigateToETProject(driver, projectName);
        Thread.sleep(1500);

        // Exec 1 (success)
        // Default params
        runTJobFromProjectPage(driver, tJobName, true, null);
        this.checkFinishTJobExec(driver, 180, "SUCCESS", false);

        // Exec 2 (fail)
        navigateToRoot(driver);
        navigateToETProject(driver, projectName);
        Thread.sleep(1500);

        Map<String, String> failExecParams = new HashMap<>();
        failExecParams.put("LEFT", "15");
        failExecParams.put("RIGHT", "10");
        failExecParams.put("EXPECTED_SUM_RESULT", "26");
        failExecParams.put("EXPECTED_SUB_RESULT", "5");

        runTJobFromProjectPage(driver, tJobName, true, failExecParams);
        this.checkFinishTJobExec(driver, 180, "FAIL", false);

        // Navigate to TJob Page
        navigateToRoot(driver);
        navigateToTJobFromETProjectsPage(driver, projectName, tJobName);

        // Select all executions and compare
        Thread.sleep(1000);
        selectAllTJobExecutions();
        compareExecutions();

        /* ****************************************** */
        /* *** Executions Comparator page actions *** */
        /* ****************************************** */
        waitForLogComparatorCard();

        // Assert all tables in all combinations of view/comparison are not
        // empty
        assertLogComparatorAllCombinationsTablesNotEmpty();
    }

}
