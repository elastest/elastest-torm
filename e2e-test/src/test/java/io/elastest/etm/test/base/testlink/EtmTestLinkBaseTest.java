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
import static org.openqa.selenium.support.ui.ExpectedConditions.elementToBeClickable;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated;
import static org.slf4j.LoggerFactory.getLogger;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;

public class EtmTestLinkBaseTest extends TestLinkBaseTest {

    public final Logger log = getLogger(lookup().lookupClass());

    String projectsTableId = "tlProjects";
    String plansTableId = "tlTestPlans";
    String suitesTableId = "tlTestSuites";
    String casesTableId = "tlCases";
    String buildsTableId = "tlBuilds";

    protected void navigateToTestlinkSection(WebDriver driver) {
        this.navigateToRoot(driver);
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
        String id = "syncTestLink";
        String xpath = "//button[@id='" + id + "']";

        log.info("Synchronizing TestLink With ElasTest");
        this.getElementByIdXpath(driver, id, xpath).get(0).click();

        // Wait to sync ends
        WebDriverWait waitService = new WebDriverWait(driver, 45);
        waitService.until(elementToBeClickable(
                this.getElementByIdXpath(driver, id, xpath).get(0)));

    }

    /* *************** */
    /* *** Project *** */
    /* *************** */

    protected String getTLEtmProjectXpath(String projectName) {
        return "//td-data-table[@id='" + this.projectsTableId
                + "']//*/td/div[contains(string(), '" + projectName + "')]";
    }

    protected void navigateToTLEtmProject(WebDriver driver,
            String projectName) {
        this.navigateToTestlinkSection(driver);

        String xpath = this.getTLEtmProjectXpath(projectName);
        this.navigateToElementByIdXpath(driver, this.projectsTableId, xpath);
    }

    protected boolean tlEtmProjectExists(WebDriver driver, String projectName) {
        this.navigateToTestlinkSection(driver);

        String xpath = this.getTLEtmProjectXpath(projectName);
        return this.elementExistsByIdXpath(driver, this.projectsTableId, xpath);
    }

    /* ***************** */
    /* *** Test Plan *** */
    /* ***************** */

    protected String getTLEtmPlanXpath(String planName) {
        return "//td-data-table[@id='" + this.plansTableId
                + "']//*/td/div[contains(string(), '" + planName + "')]";
    }

    protected void navigateToTLEtmPlan(WebDriver driver, String planName) {
        String xpath = this.getTLEtmPlanXpath(planName);
        this.navigateToElementByIdXpath(driver, this.plansTableId, xpath);
    }

    protected void navigateToTLEtmPlanByAbsolute(WebDriver driver,
            String projectName, String planName) {
        this.navigateToTLEtmProject(driver, projectName);
        this.navigateToTLEtmPlan(driver, planName);
    }

    protected boolean tlEtmPlanExists(WebDriver driver, String planName) {
        String xpath = this.getTLEtmPlanXpath(planName);
        return this.elementExistsByIdXpath(driver, this.plansTableId, xpath);
    }

    protected boolean tlEtmPlanExistsByAbsolute(WebDriver driver,
            String projectName, String planName) {
        this.navigateToTLEtmProject(driver, projectName);
        return this.tlEtmPlanExists(driver, planName);
    }

    protected void startTLEtmPlanExecution(WebDriver driver) {
        String runPlanBtnId = "runTestPlan";
        String runPlanBtnXpath = "//button[@id='" + runPlanBtnId + "']";
        this.getElementByIdXpath(driver, runPlanBtnId, runPlanBtnXpath).get(0)
                .click();

        String selectBuildId = "selectBuild";
        String selectBuildXpath = "//*[@id='" + selectBuildId + "']";
        this.getElementByIdXpath(driver, selectBuildId, selectBuildXpath).get(0)
                .click();

        this.getElementsByTagName(driver, "md-option").get(0).click();

        String runPlanModalBtnId = "runPlanModalBtn";
        String runPlanModalXpath = "//*[@id='" + runPlanModalBtnId + "']";
        this.getElementByIdXpath(driver, runPlanModalBtnId, runPlanModalXpath)
                .get(0).click();
    }

    protected void startTLEtmPlanExecutionWithNavigate(WebDriver driver,
            String projectName, String planName) {
        this.navigateToTLEtmPlanByAbsolute(driver, projectName, planName);
        this.startTLEtmPlanExecution(driver);
    }

    protected void waitForBrowserStarted(WebDriver driver) {
        WebDriverWait waitElement = new WebDriverWait(driver, 240); // seconds
        By vncCanvas = By.id("vnc_canvas");
        waitElement.until(visibilityOfElementLocated(vncCanvas));
    }

    protected void saveTLEtmPlanCurrentCaseExecution(WebDriver driver) {
        String saveAndNextBtnId = "saveAndNext";
        String saveAndNextBtnXpath = "//button[@id='" + saveAndNextBtnId + "']";

        this.getElementByIdXpath(driver, saveAndNextBtnId, saveAndNextBtnXpath,
                180).get(0).click();
        try {
            // Wait for save TestCase
            log.debug("Sleep to wait for save TestCase");
            Thread.sleep(4500);
        } catch (InterruptedException e) {
        }
    }

    protected void waitToTLEtmPlanExecutionEnds(WebDriver driver) {
        String tJobExecResultIconId = "tJobExecResultIcon";
        String tJobExecResultIconXpath = "//*[@id='" + tJobExecResultIconId
                + "']";

        this.getElementByIdXpath(driver, tJobExecResultIconId,
                tJobExecResultIconXpath, 60).get(0).click();
    }

    /* ****************** */
    /* *** Test Suite *** */
    /* ****************** */

    protected String getTLEtmSuiteXpath(String suiteName) {
        return "//td-data-table[@id='" + suitesTableId
                + "']//*/td/div[contains(string(), '" + suiteName + "')]";
    }

    protected void navigateToTLEtmSuite(WebDriver driver, String suiteName) {
        String xpath = this.getTLEtmSuiteXpath(suiteName);
        this.navigateToElementByIdXpath(driver, suitesTableId, xpath);
    }

    protected void navigateToTLEtmSuiteByAbsolute(WebDriver driver,
            String projectName, String suiteName) {
        this.navigateToTLEtmProject(driver, projectName);
        this.navigateToTLEtmSuite(driver, suiteName);
    }

    protected boolean tlEtmSuiteExists(WebDriver driver, String suiteName) {
        String xpath = this.getTLEtmSuiteXpath(suiteName);
        return this.elementExistsByIdXpath(driver, suitesTableId, xpath);
    }

    protected boolean tlEtmSuiteExistsByAbsolute(WebDriver driver,
            String projectName, String suiteName) {
        this.navigateToTLEtmProject(driver, projectName);
        return this.tlEtmSuiteExists(driver, suiteName);
    }

    /* ****************** */
    /* *** Test Build *** */
    /* ****************** */

    protected String getTLEtmBuildXpath(String buildName) {
        return "//td-data-table[@id='" + buildsTableId
                + "']//*/td/div[contains(string(), '" + buildName + "')]";
    }

    protected void navigateToTLEtmBuild(WebDriver driver, String buildName) {
        String xpath = this.getTLEtmBuildXpath(buildName);
        this.navigateToElementByIdXpath(driver, buildsTableId, xpath);
    }

    protected void navigateToTLEtmBuildByAbsolute(WebDriver driver,
            String projectName, String planName, String buildName) {
        this.navigateToTLEtmPlanByAbsolute(driver, projectName, planName);
        this.navigateToTLEtmBuild(driver, buildName);
    }

    protected boolean tlEtmBuildExists(WebDriver driver, String buildName) {
        String xpath = this.getTLEtmBuildXpath(buildName);
        return this.elementExistsByIdXpath(driver, buildsTableId, xpath);
    }

    protected boolean tlEtmBuildExistsByAbsolute(WebDriver driver,
            String projectName, String planName, String buildName) {
        this.navigateToTLEtmPlanByAbsolute(driver, projectName, planName);
        return this.tlEtmBuildExists(driver, buildName);
    }

    /* ***************** */
    /* *** Test Case *** */
    /* ***************** */

    protected String getTLEtmCaseXpath(String caseName) {
        return "//td-data-table[@id='" + casesTableId
                + "']//*/td/div[contains(string(), '" + caseName + "')]";
    }

    protected void navigateToTLEtmCase(WebDriver driver, String caseName) {
        String xpath = this.getTLEtmCaseXpath(caseName);
        this.navigateToElementByIdXpath(driver, casesTableId, xpath);
    }

    protected void navigateToTLEtmSuiteCase(WebDriver driver,
            String projectName, String suiteName, String caseName) {
        this.navigateToTLEtmSuiteByAbsolute(driver, projectName, suiteName);
        this.navigateToTLEtmCase(driver, caseName);
    }

    protected void navigateToTLEtmPlanCase(WebDriver driver, String projectName,
            String planName, String caseName) {
        this.navigateToTLEtmPlanByAbsolute(driver, projectName, planName);
        this.navigateToTLEtmCase(driver, caseName);
    }

    protected void navigateToTLEtmBuildCase(WebDriver driver,
            String projectName, String planName, String buildName,
            String caseName) {
        this.navigateToTLEtmBuildByAbsolute(driver, projectName, planName,
                buildName);
        this.navigateToTLEtmCase(driver, caseName);
    }

    protected boolean tlEtmCaseExists(WebDriver driver, String caseName) {
        String xpath = this.getTLEtmCaseXpath(caseName);
        return this.elementExistsByIdXpath(driver, casesTableId, xpath);
    }

    protected boolean tlEtmSuiteCaseExists(WebDriver driver, String projectName,
            String suiteName, String caseName) {
        this.navigateToTLEtmSuiteByAbsolute(driver, projectName, suiteName);
        return this.tlEtmCaseExists(driver, caseName);
    }

    protected boolean tlEtmPlanCaseExists(WebDriver driver, String projectName,
            String planName, String caseName) {
        this.navigateToTLEtmPlanByAbsolute(driver, projectName, planName);
        return this.tlEtmCaseExists(driver, caseName);
    }

    protected boolean tlEtmBuildCaseExists(WebDriver driver, String projectName,
            String planName, String buildName, String caseName) {
        this.navigateToTLEtmBuildByAbsolute(driver, projectName, planName,
                buildName);
        return this.tlEtmCaseExists(driver, caseName);
    }

}
