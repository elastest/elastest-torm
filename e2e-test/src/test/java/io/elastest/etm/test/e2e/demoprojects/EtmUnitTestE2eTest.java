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
package io.elastest.etm.test.e2e.demoprojects;

import static io.github.bonigarcia.seljup.BrowserType.CHROME;

import java.io.IOException;

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
 * E2E ETM test.
 *
 * @author EduJG(https://github.com/EduJGURJC)
 * @since 0.1.1
 */
@Tag("e2e")
@DisplayName("ETM E2E test of Unit Test project")
@ExtendWith(SeleniumExtension.class)
public class EtmUnitTestE2eTest extends EtmBaseTest {
    String projectName = "Unit Tests";

    void createProject(WebDriver driver) throws InterruptedException {
        navigateToTorm(driver);
        if (!etProjectExists(driver, projectName)) {
            createNewETProject(driver, projectName);
        }
    }

    @Test
    @DisplayName("Create Unit Test project Test")
    void testCreateUnitTest(
            @DockerBrowser(type = CHROME) RemoteWebDriver localDriver,
            TestInfo testInfo)
            throws InterruptedException, IOException, SecurityException {
        setupTestBrowser(testInfo, BrowserType.CHROME, localDriver);

        // Setting up the TJob used in the test
        this.createProject(driver);
        navigateToETProject(driver, projectName);
        String tJobName = "JUnit5 Unit Test";
        if (!etTJobExistsIntoProject(driver, projectName, tJobName)) {
            String tJobTestResultPath = "/demo-projects/unit/junit5-unit-test/target/surefire-reports";
            String sutName = null;
            String tJobImage = "elastest/test-etm-alpinegitjava";
            String commands = "git clone https://github.com/elastest/demo-projects; cd /demo-projects/unit/junit5-unit-test; mvn -B test;";
            createNewTJob(driver, tJobName, tJobTestResultPath, sutName,
                    tJobImage, false, commands, null, null, null, null);
        }
        // Run the TJob and check its result
        runTJobFromProjectPage(driver, tJobName);
        this.checkFinishTJobExec(driver, 240, "SUCCESS", false);
    }

}
