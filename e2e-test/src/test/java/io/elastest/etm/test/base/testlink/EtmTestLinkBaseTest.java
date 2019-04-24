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
import static org.openqa.selenium.support.ui.ExpectedConditions.invisibilityOfElementLocated;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;

public class EtmTestLinkBaseTest extends TestLinkBaseTest {

    public final Logger log = getLogger(lookup().lookupClass());

    protected static final String TL_PROJECTS_TABLE_ID = "tlProjects";
    protected static final String TL_PLANS_TABLE_ID = "tlTestPlans";
    protected static final String TL_SUITES_TABLE_ID = "tlTestSuites";
    protected static final String TL_CASES_TABLE_ID = "tlCases";
    protected static final String TL_BUILDS_TABLE_ID = "tlBuilds";

    protected void navigateToTestlinkSection(WebDriver driver) {
        log.info("Navigate to TestLink Section");
        this.getElementByXpath(driver, "//a[@id='nav_testlink']").click();
        this.getElementById(driver, "testlinkPage", 5);
        sleep(1500);
    }

    protected void openTestlinkPage(WebDriver driver) {
        this.navigateToTestlinkSection(driver);
        log.info("Open TestLink Page");

        this.getElementById(driver, "openTestLink", 10).click();
    }

    protected String getTestlinkPageUrl(WebDriver driver) {
        this.navigateToTestlinkSection(driver);
        log.info("Getting TestLink Page");

        return this.getElementById(driver, "openTestLink", 15)
                .getAttribute("href");
    }

    protected void syncTestlink(WebDriver driver) {
        this.navigateToTestlinkSection(driver);
        String id = "syncTestLink";
        String xpath = "//button[@id='" + id + "']";

        log.info("Synchronizing TestLink With ElasTest");
        this.getElementByIdXpath(driver, id, xpath).click();

        // Wait to sync ends
        WebDriverWait waitService = new WebDriverWait(driver, 45);
        waitService.until(elementToBeClickable(
                this.getElementByIdXpath(driver, id, xpath)));

    }

    protected void startTestLinkIfNecessary(WebDriver driver) {
        By startTestLinkBtnId = By.id("startTestLink");
        WebElement startTestLinkBtn = driver.findElements(startTestLinkBtnId)
                .size() > 0 ? driver.findElements(startTestLinkBtnId).get(0)
                        : null;
        if (startTestLinkBtn != null) {
            log.debug("Starting TestLink container...");
            startTestLinkBtn.click();
            new WebDriverWait(driver, 120)
                    .until(invisibilityOfElementLocated(startTestLinkBtnId));

        }
    }

    protected void startTestLinkIfNecessaryWithNavigate(WebDriver driver) {
        // Start TestLink if necessary
        this.navigateToTorm(driver);
        this.navigateToTestlinkSection(driver);
        this.startTestLinkIfNecessary(driver);
    }

    /* *************** */
    /* *** Project *** */
    /* *************** */

    protected String getTLEtmProjectXpath(String projectName) {
        return "//td-data-table[@id='" + TL_PROJECTS_TABLE_ID
                + "']//*/td/div[contains(string(), '" + projectName + "')]";
    }

    protected void navigateToTLEtmProject(WebDriver driver,
            String projectName) {
        this.navigateToTestlinkSection(driver);

        String xpath = this.getTLEtmProjectXpath(projectName);
        this.navigateToElementByIdXpath(driver, TL_PROJECTS_TABLE_ID, xpath);
        sleep(1500);
    }

    protected boolean tlEtmProjectExists(WebDriver driver, String projectName) {
        this.navigateToTestlinkSection(driver);

        String xpath = this.getTLEtmProjectXpath(projectName);
        return this.elementExistsByIdXpath(driver, TL_PROJECTS_TABLE_ID, xpath,
                30, true);
    }

    /* ***************** */
    /* *** Test Plan *** */
    /* ***************** */

    protected String getTLEtmPlanXpath(String planName) {
        return "//td-data-table[@id='" + TL_PLANS_TABLE_ID
                + "']//*/td/div[contains(string(), '" + planName + "')]";
    }

    protected void navigateToTLEtmPlan(WebDriver driver, String planName) {
        String xpath = this.getTLEtmPlanXpath(planName);
        this.navigateToElementByIdXpath(driver, TL_PLANS_TABLE_ID, xpath);
    }

    protected void navigateToTLEtmPlanByAbsolute(WebDriver driver,
            String projectName, String planName) {
        this.navigateToTLEtmProject(driver, projectName);
        this.navigateToTLEtmPlan(driver, planName);
    }

    protected boolean tlEtmPlanExists(WebDriver driver, String planName) {
        String xpath = this.getTLEtmPlanXpath(planName);
        return this.elementExistsByIdXpath(driver, TL_PLANS_TABLE_ID, xpath);
    }

    protected boolean tlEtmPlanExistsByAbsolute(WebDriver driver,
            String projectName, String planName) {
        this.navigateToTLEtmProject(driver, projectName);
        return this.tlEtmPlanExists(driver, planName);
    }

    protected void startTLEtmPlanExecution(WebDriver driver,
            List<String> extraHosts) {
        String runPlanBtnId = "runTestPlan";
        String runPlanBtnXpath = "//button[@id='" + runPlanBtnId + "']";

        // Run and Modal Open
        this.getElementByIdXpath(driver, runPlanBtnId, runPlanBtnXpath).click();
        sleep(1000);

        // Add Extra hosts
        if (extraHosts != null) {
            if (extraHosts.size() > 1) {
                String addExtraHostBtnXPath = "//*[@id=\"mat-dialog-0\"]/select-build-modal//string-list-view/button[contains(string(), 'Add Extra Host')]";
                int currentHost = 0;
                for (String host : extraHosts) {
                    if (currentHost > 0) {
                        getElementByXpath(driver, addExtraHostBtnXPath).click();
                        sleep(500);
                    }

                    // Set host
                    getElementById(driver, "ExtraHost" + currentHost, true)
                            .sendKeys(host);
                }
            }
        }

        this.runTLEtmPlanExecutionFromModal(driver);
    }

    protected void startTLEtmPlanExecutionWithNavigate(WebDriver driver,
            String projectName, String planName, List<String> extraHosts) {
        this.navigateToTLEtmPlanByAbsolute(driver, projectName, planName);
        this.startTLEtmPlanExecution(driver, extraHosts);
    }

    protected void startTLEtmPlanExecutionWithNavigate(WebDriver driver,
            String projectName, String planName) {
        this.startTLEtmPlanExecutionWithNavigate(driver, projectName, planName,
                null);
    }

    protected void runTLEtmPlanExecutionFromModal(WebDriver driver) {
        String selectBuildId = "selectBuild";
        String selectBuildXpath = "//*[@id='" + selectBuildId + "']";
        this.getElementByIdXpath(driver, selectBuildId, selectBuildXpath)
                .click();

        this.getElementsByTagName(driver, "mat-option").get(0).click();

        String runPlanModalBtnId = "runPlanModalBtn";
        String runPlanModalXpath = "//*[@id='" + runPlanModalBtnId + "']";

        WebElement runBtn = this.getElementByIdXpath(driver, runPlanModalBtnId,
                runPlanModalXpath);
        WebDriverWait waitService = new WebDriverWait(driver, 45);
        waitService.until(elementToBeClickable(runBtn));
        runBtn.click();
    }

    protected void waitForBrowserStarted(WebDriver driver) {
        WebDriverWait waitElement = new WebDriverWait(driver, 320); // seconds
        By vncCanvas = By.id("vnc_canvas");
        waitElement.until(visibilityOfElementLocated(vncCanvas));
    }

    protected void saveTLEtmPlanCurrentCaseExecution(WebDriver driver) {
        String saveAndNextBtnId = "saveAndNext";
        String saveAndNextBtnXpath = "//button[@id='" + saveAndNextBtnId + "']";

        this.getElementByIdXpath(driver, saveAndNextBtnId, saveAndNextBtnXpath,
                320, false).click();
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
                tJobExecResultIconXpath, 60, false).click();
    }

    protected void selectInternalSut(WebDriver driver, String sutName) {
        String sutSelectXpath = "//mat-select/div/div/span[contains(string(), 'Select a SuT')]";
        this.getElementByXpath(driver, sutSelectXpath).click();

        if (sutName != null) {
            this.getElementByXpath(driver,
                    "//mat-option/span[contains(string(), '" + sutName + "')]")
                    .click();
        } else {
            this.getElementByXpath(driver,
                    "//mat-option/span[contains(string(), 'None')]").click();
        }

        getElementByXpath(driver, "//button[contains(string(), 'SAVE')]")
                .click();

    }

    /* ****************** */
    /* *** Test Suite *** */
    /* ****************** */

    protected String getTLEtmSuiteXpath(String suiteName) {
        return "//td-data-table[@id='" + TL_SUITES_TABLE_ID
                + "']//*/td/div/div[contains(string(), '" + suiteName + "')]";
    }

    protected void navigateToTLEtmSuite(WebDriver driver, String suiteName) {
        String xpath = this.getTLEtmSuiteXpath(suiteName);
        log.info("Navigate to Suite case with id {} and path {}",
                TL_SUITES_TABLE_ID, xpath);
        this.navigateToElementByIdXpath(driver, TL_SUITES_TABLE_ID, xpath);
    }

    protected void navigateToTLEtmSuiteByAbsolute(WebDriver driver,
            String projectName, String suiteName) {
        this.navigateToTLEtmProject(driver, projectName);
        this.navigateToTLEtmSuite(driver, suiteName);
    }

    protected boolean tlEtmSuiteExists(WebDriver driver, String suiteName) {
        String xpath = this.getTLEtmSuiteXpath(suiteName);
        return this.elementExistsByIdXpath(driver, TL_SUITES_TABLE_ID, xpath);
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
        return "//td-data-table[@id='" + TL_BUILDS_TABLE_ID
                + "']//*/td/div[contains(string(), '" + buildName + "')]";
    }

    protected void navigateToTLEtmBuild(WebDriver driver, String buildName) {
        String xpath = this.getTLEtmBuildXpath(buildName);
        this.navigateToElementByIdXpath(driver, TL_BUILDS_TABLE_ID, xpath);
    }

    protected void navigateToTLEtmBuildByAbsolute(WebDriver driver,
            String projectName, String planName, String buildName) {
        this.navigateToTLEtmPlanByAbsolute(driver, projectName, planName);
        this.navigateToTLEtmBuild(driver, buildName);
    }

    protected boolean tlEtmBuildExists(WebDriver driver, String buildName) {
        String xpath = this.getTLEtmBuildXpath(buildName);
        return this.elementExistsByIdXpath(driver, TL_BUILDS_TABLE_ID, xpath);
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
        return "//td-data-table[@id='" + TL_CASES_TABLE_ID
                + "']//*/td/div[contains(string(), '" + caseName + "')]";
    }

    protected void navigateToTLEtmCase(WebDriver driver, String caseName) {
        String xpath = this.getTLEtmCaseXpath(caseName);
        this.navigateToElementByIdXpath(driver, TL_CASES_TABLE_ID, xpath);
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
        return this.elementExistsByIdXpath(driver, TL_CASES_TABLE_ID, xpath);
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
