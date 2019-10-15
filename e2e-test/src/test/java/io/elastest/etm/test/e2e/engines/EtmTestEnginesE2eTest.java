package io.elastest.etm.test.e2e.engines;
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

import static io.github.bonigarcia.seljup.BrowserType.CHROME;

import java.io.IOException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.remote.RemoteWebDriver;

import io.elastest.etm.test.base.EtmBaseTest;
import io.github.bonigarcia.seljup.BrowserType;
import io.github.bonigarcia.seljup.DockerBrowser;
import io.github.bonigarcia.seljup.SeleniumExtension;

/**
 * Test that interacts with Test Engines page. Requirements tested: ETM14
 *
 * @author EduJG(https://github.com/EduJGURJC)
 * @since 0.1.1
 */
@Tag("e2e")
@DisplayName("ETM E2E test of GUI Test Engines page")
@ExtendWith(SeleniumExtension.class)
public class EtmTestEnginesE2eTest extends EtmBaseTest {

    @Test
    @DisplayName("Navigate to Test Engines page and start/view/stop Ece")
    void startAndStopTestEngine(
            @DockerBrowser(type = CHROME) RemoteWebDriver localDriver,
            TestInfo testInfo)
            throws InterruptedException, IOException, SecurityException {
        setupTestBrowser(testInfo, BrowserType.CHROME, localDriver);

        navigateToTorm(driver);
        navigateToTestEnginesPage(driver);

        // Start button
        log.debug("Start first Test Engine (ECE)");
        getElementByXpath(driver, "//*[@id='start-engine-ece']").click();

        String statusXpath = "//*[@id='ece-status']";

        log.debug("Waiting for the Test Engine (ECE) to be ready");
        getElementByXpath(driver, statusXpath + "[contains(string(),'Ready')]",
                300).getText();

        // log.debug("Navigate to view of Test Engine (ECE)");
        getElementByXpath(driver, "//*[@id='view-engine-ece']");
        // .click();

        // log.debug("Check if Test Engine (ECE) iframe exists");
        // getElementByXpath(driver, "//iframe[@name='engine']");

        // log.debug("Return to Test Engines page");
        // navigateToTestEnginesPage(driver);

        log.debug("Stop Test Engine (ECE)");
        getElementByXpath(driver, "//*[@id='stop-engine-ece']").click();
        log.debug("Wait for Test Engine (ECE) to be stopped");
        getElementByXpath(driver,
                statusXpath + "[contains(string(),'Not initialized')]", 120)
                        .getText();
    }

}
