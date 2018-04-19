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

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;

public class EtmTestLinkBaseTest extends TestLinkBaseTest {

    final Logger log = getLogger(lookup().lookupClass());

    protected void navigateToTestlinkSection(WebDriver driver) {
        this.navigateToTorm(driver);
        log.info("Navigate to TestLink Section");

        driver.findElement(By.xpath("//a[@id='nav_testlink']")).click();
    }

    protected void openTestlinkPage(WebDriver driver) {
        this.navigateToTestlinkSection(driver);
        log.info("Open TestLink Page");

        driver.findElement(By.xpath("//a[@id='openTestLink']")).click();
    }

    protected String getTestlinkPageUrl(WebDriver driver) {
        this.navigateToTestlinkSection(driver);
        log.info("Getting TestLink Page");

        return driver.findElement(By.xpath("//a[@id='openTestLink']"))
                .getAttribute("href");
    }

    protected void syncTestlink(WebDriver driver) {
        this.navigateToTestlinkSection(driver);
        log.info("Sync TestLink With ElasTest");

        driver.findElement(By.xpath("//button[@id='syncTestLink']")).click();
    }

    /* *************** */
    /* *** Project *** */
    /* *************** */

    protected void navigateToTLProject(WebDriver driver, String projectName) {
        this.navigateToTestlinkSection(driver);

        String id = "tlProjects";
        String xpath = "//td-data-table[@id='" + id
                + "']//*/td/div[contains(string(), '" + projectName + "')]";

        this.navigateToElement(driver, id, xpath);
    }

    protected boolean tlProjectExists(WebDriver driver, String projectName) {
        this.navigateToTestlinkSection(driver);

        String id = "tlProjects";
        String xpath = "//td-data-table[@id='" + id
                + "']//*/td/div[contains(string(), '" + projectName + "')]";

        return this.elementExists(driver, id, xpath);
    }

    /* ***************** */
    /* *** Test Plan *** */
    /* ***************** */

    protected void navigateToTLPlan(WebDriver driver, String planName) {
        this.navigateToTestlinkSection(driver);

        String id = "tlTestPlans";
        String xpath = "//td-data-table[@id='" + id
                + "']//*/td/div[contains(string(), '" + planName + "')]";
        this.navigateToElement(driver, id, xpath);
    }

    protected boolean tlPlanExists(WebDriver driver, String planName) {
        this.navigateToTestlinkSection(driver);

        String id = "tlTestPlans";
        String xpath = "//td-data-table[@id='" + id
                + "']//*/td/div[contains(string(), '" + planName + "')]";

        return this.elementExists(driver, id, xpath);
    }

    /* ****************** */
    /* *** Test Suite *** */
    /* ****************** */

    protected void navigateToTLSuite(WebDriver driver, String suiteName) {
        this.navigateToTestlinkSection(driver);

        String id = "tlTestSuites";
        String xpath = "//td-data-table[@id='" + id
                + "']//*/td/div[contains(string(), '" + suiteName + "')]";
        this.navigateToElement(driver, id, xpath);
    }

    protected boolean tlSuiteExists(WebDriver driver, String suiteName) {
        this.navigateToTestlinkSection(driver);

        String id = "tlTestSuites";
        String xpath = "//td-data-table[@id='" + id
                + "']//*/td/div[contains(string(), '" + suiteName + "')]";

        return this.elementExists(driver, id, xpath);
    }

    /* ***************** */
    /* *** Test Case *** */
    /* ***************** */

    protected void navigateToTLCase(WebDriver driver, String caseName) {
        this.navigateToTestlinkSection(driver);

        String id = "tlTestCases";
        String xpath = "//td-data-table[@id='" + id
                + "']//*/td/div[contains(string(), '" + caseName + "')]";
        this.navigateToElement(driver, id, xpath);
    }

    protected boolean tlCaseExists(WebDriver driver, String caseName) {
        this.navigateToTestlinkSection(driver);

        String id = "tlTestCases";
        String xpath = "//td-data-table[@id='" + id
                + "']//*/td/div[contains(string(), '" + caseName + "')]";

        return this.elementExists(driver, id, xpath);
    }

    /* ****************** */
    /* *** Test Build *** */
    /* ****************** */

    protected void navigateToTLBuild(WebDriver driver, String buildName) {
        this.navigateToTestlinkSection(driver);

        String id = "tlTestBuilds";
        String xpath = "//td-data-table[@id='" + id
                + "']//*/td/div[contains(string(), '" + buildName + "')]";
        this.navigateToElement(driver, id, xpath);
    }

    protected boolean BuildExists(WebDriver driver, String buildName) {
        this.navigateToTestlinkSection(driver);

        String id = "tlTestBuilds";
        String xpath = "//td-data-table[@id='" + id
                + "']//*/td/div[contains(string(), '" + buildName + "')]";

        return this.elementExists(driver, id, xpath);
    }
}
