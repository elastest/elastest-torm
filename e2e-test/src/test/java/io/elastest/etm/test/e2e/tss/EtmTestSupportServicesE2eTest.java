package io.elastest.etm.test.e2e.tss;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
 * Test that interacts with Test Support Services page. Requirements tested:
 * ETM14
 *
 * @author EduJG(https://github.com/EduJGURJC)
 * @since 0.1.1
 */
@Tag("e2e")
@DisplayName("ETM E2E test of GUI Test Support Services page")
@ExtendWith(SeleniumExtension.class)
public class EtmTestSupportServicesE2eTest extends EtmBaseTest {

    @Test
    @DisplayName("Navigate to Test Support Services page and start/view/stop Ece")
    void startAndStopTss(
            @DockerBrowser(type = CHROME) RemoteWebDriver localDriver,
            TestInfo testInfo)
            throws InterruptedException, IOException, SecurityException {
        setupTestBrowser(testInfo, BrowserType.CHROME, localDriver);

        navigateToTssPage(driver);

        log.debug("Select a Test Support Service (EUS)");
        selectItem(driver, "EUS", "Select a Service");

        log.debug("Start Test Support Service (EUS)");
        getElementById(driver, "create_instance").click();

        String instanceRowXpath = "//esm-instance-manager//td-data-table//tr[1]";

        String statusXpath = instanceRowXpath + "/td[3]/div/span";

        log.debug("Waiting for the Test Support Service to be ready");
        getElementByXpath(driver, statusXpath + "[contains(string(),'Ready')]",
                120).getText();

        String firstTSSButtonsXpath = instanceRowXpath + "//button";

        // log.debug("Navigate to view of Test Support Service");
        assertNotNull(getElementByXpath(driver,
                firstTSSButtonsXpath + "[@title='View Service Detail']"));

        log.debug("Stop Test Support Service");
        deleteTSSInstance(driver);
    }

}
