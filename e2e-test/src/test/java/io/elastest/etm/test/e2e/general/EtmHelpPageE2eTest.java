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
package io.elastest.etm.test.e2e.general;

import static io.github.bonigarcia.seljup.BrowserType.CHROME;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;

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
 * Test that interacts with Help page. Requirements tested: ETM15, ETM16
 *
 * @author EduJG(https://github.com/EduJGURJC)
 * @since 0.1.1
 */
@Tag("e2e")
@DisplayName("ETM E2E test of GUI Help Page")
@ExtendWith(SeleniumExtension.class)
public class EtmHelpPageE2eTest extends EtmBaseTest {

    @Test
    @DisplayName("Navigate to Help page and check ElasTest version")
    void checkElasTestVersion(
            @DockerBrowser(type = CHROME) RemoteWebDriver localDriver,
            TestInfo testInfo)
            throws InterruptedException, IOException, SecurityException {
        setupTestBrowser(testInfo, BrowserType.CHROME, localDriver);

        navigateToTorm(driver);
        navigateToHelpPage(driver);
        log.debug("Checking ElasTest version");
        String version = getElementById(driver, "etVersion").getText();
        log.debug("ElasTest version: {}", version);
    }

    @Test
    @DisplayName("Navigate to Help page and check ElasTest main services")
    void checkElasTestMainServices(
            @DockerBrowser(type = CHROME) RemoteWebDriver localDriver,
            TestInfo testInfo)
            throws InterruptedException, IOException, SecurityException {
        setupTestBrowser(testInfo, BrowserType.CHROME, localDriver);

        navigateToTorm(driver);
        navigateToHelpPage(driver);
        log.debug("Checking ElasTest Main Services");
        WebElement servicesTable = getElementByXpath(driver,
                "//*[@id=\"coreServicesInfo\"]//tr");
        assertNotNull(servicesTable);
    }

}
