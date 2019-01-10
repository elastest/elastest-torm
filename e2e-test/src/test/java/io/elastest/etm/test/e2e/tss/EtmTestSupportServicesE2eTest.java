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

import static io.github.bonigarcia.BrowserType.CHROME;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.openqa.selenium.support.ui.ExpectedConditions.invisibilityOfElementLocated;

import java.io.IOException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import io.elastest.etm.test.base.EtmBaseTest;
import io.github.bonigarcia.BrowserType;
import io.github.bonigarcia.DockerBrowser;
import io.github.bonigarcia.SeleniumExtension;

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

        navigateToTorm(driver);
        navigateToTssPage(driver);

        log.debug("Select a Test Support Service (EUS)");
        selectItem(driver, "EUS", "Select a Service");

        log.debug("Start Test Support Service (EUS)");
        getElementById(driver, "create_instance").click();

        String instanceRowXpath = "//esm-instance-manager//td-data-table//tr[1]";

        String statusXpath = instanceRowXpath + "/td[3]/span";

        log.debug("Waiting for the Test Support Service to be ready");
        getElementByXpath(driver, statusXpath + "[contains(string(),'Ready')]",
                120).getText();

        String firstTSSButtonsXpath = instanceRowXpath + "//button";

        // log.debug("Navigate to view of Test Support Service");
        assertNotNull(getElementByXpath(driver,
                firstTSSButtonsXpath + "[@title='View Service Detail']"));
        // .click();

        // log.debug("Check if Test Support Service iframe exists");
        // getElementByXpath(driver, "//iframe[@name='engine']").get(0);

        // log.debug("Return to Test Support Services page");
        // navigateToTestEnginesPage(driver);

        log.debug("Stop Test Support Service");
        WebElement removeButton = getElementByXpath(driver,
                firstTSSButtonsXpath + "[@title='Deprovision Service']");
        sleep(2000);

        By sessionId = By.xpath(
                "//*[@id=\"tss-instances\"]/div/table/tbody/tr[1]/td[1]/span");
        driver.findElement(sessionId).getText();
        log.info("Browser session id: {}",
                driver.findElement(sessionId).getText());
        By deleteServices = By.id("deleteService-"
                + driver.findElement(sessionId).getText().trim());
        assertNotNull(removeButton);
        removeButton.click();
        log.debug("Wait for Test Support Service to be stopped");
        WebDriverWait waitEnd = new WebDriverWait(driver, 120);
        waitEnd.until(invisibilityOfElementLocated(deleteServices));
    }

}
