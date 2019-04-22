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
package io.elastest.etm.test.e2e.loganalyzer;

import static io.github.bonigarcia.seljup.BrowserType.CHROME;

import java.net.MalformedURLException;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.WebElement;
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
@DisplayName("LogAnalyzer E2E tests")
@ExtendWith(SeleniumExtension.class)
public class EtmLogAnalyzerE2eTest extends EtmBaseTest {
    String projectName = "Unit Tests";
    String tJobName = "JUnit5 Unit Test";

    @Test
    @DisplayName("Check TJob Execution logs in Log Analyzer")
    void testExecuteAndCheckLogsInLogAnalyzer(
            @DockerBrowser(type = CHROME) RemoteWebDriver localDriver,
            TestInfo testInfo)
            throws InterruptedException, MalformedURLException {
        setupTestBrowser(testInfo, BrowserType.CHROME, localDriver);

        // Prepare test
        navigateToTorm(driver);
        navigateToETProject(driver, projectName);
        Thread.sleep(1500);
        runTJobFromProjectPage(driver, tJobName);
        this.checkFinishTJobExec(driver, 180, "SUCCESS", false);

        // Check the LogAnalyzer operation
        Thread.sleep(1000);

        log.info("View execution in LogAnalyzer");
        getElementById(driver, "viewExecutionInLogAnalyzer").click();

        log.info("Wait for the Log Analyzer page to show");
        this.getElementsByTagName(driver, "elastest-log-analyzer", 40);

        log.info("Check for logs in LogAnalyzer");
        List<WebElement> elements = getElementsById(driver, "logsGrid", 15);
        if (elements.size() > 0) {
            log.info("There are logs in LogAnalyzer!");
        }
    }

}
