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

import static io.github.bonigarcia.BrowserType.CHROME;
import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;

import io.elastest.etm.test.base.EtmBaseTest;
import io.github.bonigarcia.DockerBrowser;
import io.github.bonigarcia.SeleniumExtension;

/**
 * E2E ETM test.
 *
 * @author EduJG(https://github.com/EduJGURJC)
 * @since 0.1.1
 */
@Tag("e2e")
@DisplayName("LogAnalyzer E2E tests")
@ExtendWith(SeleniumExtension.class)
public class EtmLogAnalyzerE2eTest extends EtmBaseTest {

    final Logger log = getLogger(lookup().lookupClass());
    String projectName = "Hello World";
    String tJobName = "My first TJob";

    @Test
    @DisplayName("Check TJob Execution logs in Log Analyzer")
    void testCreateUnitTest(
            @DockerBrowser(type = CHROME) RemoteWebDriver driver)
            throws InterruptedException {
        this.driver = driver;

        navigateToTorm(driver);
        // Navigate to project
        navigateToETProject(driver, projectName);
        Thread.sleep(1500);
        // Run TJob
        runTJobFromProjectPage(driver, tJobName);

        this.checkFinishTJobExec(driver, 180, "SUCCESS");

        // Refresh to redirect to results page
        this.driver.navigate().refresh();

        log.info("View execution in LogAnalyzer");
        getElementById(driver, "viewExecutionInLogAnalyzer").get(0).click();

        log.info("Check for logs in LogAnalyzer");
        List<WebElement> elements = getElementById(driver, "logsGrid", 15);
        if (elements.size() > 0) {
            log.info("There are logs in LogAnalyzer!");
        }
    }

}
