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
package io.elastest.etm.test.base;

import static java.lang.System.getProperty;
import static java.lang.invoke.MethodHandles.lookup;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.logging.Level.ALL;
import static org.openqa.selenium.logging.LogType.BROWSER;
import static org.openqa.selenium.remote.CapabilityType.LOGGING_PREFS;
import static org.openqa.selenium.remote.DesiredCapabilities.chrome;
import static org.openqa.selenium.support.ui.ExpectedConditions.invisibilityOfElementLocated;
import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated;
import static org.openqa.selenium.support.ui.ExpectedConditions.textToBePresentInElementLocated;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;

import io.elastest.etm.test.utils.RestClient;
import io.github.bonigarcia.BrowserType;
import io.github.bonigarcia.DriverCapabilities;
import io.github.bonigarcia.wdm.ChromeDriverManager;
import io.github.bonigarcia.wdm.FirefoxDriverManager;

public class EtmBaseTest {
    protected final Logger log = getLogger(lookup().lookupClass());

    protected String tormUrl = "http://172.17.0.1:37000/"; // local by default
    protected String secureTorm = "http://user:pass@172.17.0.1:37000/";
    protected String apiPath = "api";
    protected String tormApiUrl;
    protected String eUser = null;
    protected String ePassword = null;
    protected boolean secureElastest = false;
    public WebDriver driver;
    protected RestClient restClient;
    protected String eusURL;

    @DriverCapabilities
    DesiredCapabilities capabilities = chrome();
    {
        LoggingPreferences logPrefs = new LoggingPreferences();
        logPrefs.enable(BROWSER, ALL);
        capabilities.setCapability(LOGGING_PREFS, logPrefs);
    }

    @BeforeEach
    void setup() {
        String etmApi = getProperty("etEtmApi");
        if (etmApi != null) {
            tormUrl = etmApi;
        }
        String elastestUser = getProperty("eUser");
        if (elastestUser != null) {
            log.info("Elastest User received: {}", elastestUser);
            eUser = elastestUser;

            String elastestPassword = getProperty("ePass");
            if (elastestPassword != null) {
                log.info("Elastest Password received: {}", elastestPassword);
                ePassword = elastestPassword;
                secureElastest = true;
            }

        }

        this.tormApiUrl = this.tormUrl
                + (this.tormUrl.endsWith("/") ? this.apiPath
                        : "/" + this.apiPath);

        if (secureElastest) {
            String split_url[] = tormUrl.split("//");
            secureTorm = split_url[0] + "//" + eUser + ":" + ePassword + "@"
                    + split_url[1];

            this.tormApiUrl = secureTorm
                    + (secureTorm.endsWith("/") ? this.apiPath
                            : "/" + this.apiPath);

        }

        log.info("Using URL {} to connect to {} TORM", tormApiUrl,
                secureElastest ? "secure" : "unsecure");

        this.restClient = new RestClient(this.tormApiUrl, eUser, ePassword,
                secureElastest);

        eusURL = System.getenv("ET_EUS_API");

        if (eusURL == null) {
            // Outside ElasTest
            ChromeDriverManager.getInstance().setup();
            FirefoxDriverManager.getInstance().setup();
        }
    }

    @AfterEach
    void teardown(TestInfo testInfo) throws IOException {
        String testName = testInfo.getTestMethod().get().getName();
        log.info("##### Finish test: {}", testName);
        if (driver != null) {
            if (eusURL != null) {
                log.info("Clearing Messages...");
                driver.quit();
            } else {
                log.info("Browser console at the end of the test");
                LogEntries logEntries = driver.manage().logs().get(BROWSER);
                logEntries.forEach((entry) -> log.info("[{}] {} {}",
                        new Date(entry.getTimestamp()), entry.getLevel(),
                        entry.getMessage()));
            }
        }
    }

    protected void navigateTo(WebDriver driver, String url) {
        log.info("Navigating to: {}", url);
        driver.manage().window().setSize(new Dimension(1024, 1024));
        driver.manage().timeouts().implicitlyWait(5, SECONDS);
        driver.get(url);
    }

    protected void navigateToTorm(WebDriver driver) {
        log.info("Navigating to TORM");
        driver.manage().window().setSize(new Dimension(1024, 1024));
        driver.manage().timeouts().implicitlyWait(5, SECONDS);
        if (secureElastest) {
            driver.get(secureTorm);
        } else {
            driver.get(tormUrl);
        }
    }

    protected void navigateToRoot(WebDriver driver) {
        log.info("Navigating to Root Path (/)");
        driver.findElement(By.xpath(
                "//*[@id='main_nav']/div/md-toolbar/div/md-toolbar-row/span"))
                .click();
    }

    protected void navigateToProjects(WebDriver driver) {
        log.info("Navigating to Projects Path (/project)");
        this.getElementById(driver, "nav_projects").get(0).click();
    }

    protected void navigateToHelpPage(WebDriver driver) {
        log.debug("Navigating to Help page");
        getElementById(driver, "help").get(0).click();
    }

    protected void navigateToTestEnginesPage(WebDriver driver) {
        log.debug("Navigating to Test Engines page");
        getElementById(driver, "nav_test_engines").get(0).click();
    }

    protected void navigateToTssPage(WebDriver driver) {
        log.debug("Navigating to Test Support Services page");
        getElementById(driver, "nav_support_services").get(0).click();
    }

    protected void navigateToElementByIdXpath(WebDriver driver, String id,
            String xpath) {
        this.getElementByIdXpath(driver, id, xpath).get(0).click();
    }

    protected void navigateToElementByXpath(WebDriver driver, String xpath) {
        this.getElementByXpath(driver, xpath).get(0).click();
    }

    protected boolean elementExistsByIdXpath(WebDriver driver, String id,
            String xpath) {
        return this.getElementByIdXpath(driver, id, xpath).size() != 0;
    }

    protected boolean elementExistsByXpath(WebDriver driver, String xpath) {
        By elementAvailable = By.xpath(xpath);
        return driver.findElements(elementAvailable).size() != 0;
    }

    protected List<WebElement> getElementById(WebDriver driver, String id,
            int secondsTimeout) {
        String xpath = "//*[@id='" + id + "']";
        return this.getElementByIdXpath(driver, id, xpath, secondsTimeout);
    }

    protected List<WebElement> getElementById(WebDriver driver, String id) {
        return this.getElementById(driver, id, 30);
    }

    protected List<WebElement> getElementByIdXpath(WebDriver driver, String id,
            String xpath) {
        return this.getElementByIdXpath(driver, id, xpath, 30);
    }

    protected List<WebElement> getElementByIdXpath(WebDriver driver, String id,
            String xpath, int secondsTimeout) {
        WebDriverWait waitService = new WebDriverWait(driver, secondsTimeout);
        By elementAvailable = By.id(id);
        waitService.until(presenceOfElementLocated(elementAvailable));

        return driver.findElements(By.xpath(xpath));
    }

    protected List<WebElement> getElementByXpath(WebDriver driver,
            String xpath) {
        return this.getElementByXpath(driver, xpath, 30);
    }

    protected List<WebElement> getElementByXpath(WebDriver driver, String xpath,
            int secondsTimeout) {
        WebDriverWait waitService = new WebDriverWait(driver, secondsTimeout);
        By elementAvailable = By.xpath(xpath);
        waitService.until(presenceOfElementLocated(elementAvailable));

        return driver.findElements(elementAvailable);
    }

    protected List<WebElement> getElementsByTagName(WebDriver driver,
            String tagName) {
        return this.getElementsByTagName(driver, tagName, 30);
    }

    protected List<WebElement> getElementsByName(WebDriver driver, String name,
            int secondsTimeout) {
        WebDriverWait waitService = new WebDriverWait(driver, secondsTimeout);
        waitService.until(presenceOfElementLocated(By.name(name)));

        return driver.findElements(By.name(name));
    }

    protected List<WebElement> getElementsByName(WebDriver driver,
            String name) {
        return this.getElementsByName(driver, name, 30);
    }

    protected List<WebElement> getElementsByTagName(WebDriver driver,
            String tagName, int secondsTimeout) {
        WebDriverWait waitService = new WebDriverWait(driver, secondsTimeout);
        waitService.until(presenceOfElementLocated(By.tagName(tagName)));

        return driver.findElements(By.tagName(tagName));
    }

    /* *************** */
    /* *** Project *** */
    /* *************** */
    protected void createNewETProject(WebDriver driver, String projectName) {
        log.info("Create project");
        getElementById(driver, "newProjectBtn").get(0).click();
        driver.findElement(By.name("project.name")).sendKeys(projectName);
        driver.findElement(By.xpath("//button[contains(string(), 'SAVE')]"))
                .click();
    }

    protected void removeETProject(WebDriver driver, String projectName) {
        this.navigateToRoot(driver);
        // TODO
    }

    protected String getProjectsTableXpathFromProjectPage() {
        String id = "projects";
        String xpath = "//td-data-table[@id='" + id + "']";
        return xpath;
    }

    protected String getProjectXpathFromProjectPage(String projectName) {
        String xpath = getProjectsTableXpathFromProjectPage();
        xpath += "//*/td/div[text()='" + projectName + "']";

        return xpath;
    }

    protected void navigateToETProject(WebDriver driver, String projectName) {
        this.navigateToProjects(driver);
        log.info("Navigating to {} project", projectName);

        String xpath = getProjectXpathFromProjectPage(projectName);
        this.navigateToElementByXpath(driver, xpath);
    }

    protected boolean etProjectExists(WebDriver driver, String projectName) {
        log.info("Checking if Project {} exists", projectName);
        String projectsTableXpath = getProjectsTableXpathFromProjectPage();

        // If exist PJ table (if there are some pj)
        if (this.driver.findElements(By.xpath(projectsTableXpath)).size() > 0) {
            String projectXpath = getProjectXpathFromProjectPage(projectName);
            boolean projectExist = this.elementExistsByXpath(driver,
                    projectXpath);
            String existStr = projectExist ? "already exist" : "does not exist";
            log.info("Project {} {} ", projectName, existStr);
            return projectExist;
        } else {
            return false;
        }
    }

    /* *************** */
    /* ***** Sut ***** */
    /* *************** */
    protected void createSutAndInsertCommonFields(WebDriver driver,
            String sutName, String desc) {
        log.info("Creating new SuT");
        this.getElementById(driver, "newSutBtn").get(0).click();
        this.getElementsByName(driver, "sutName").get(0).sendKeys(sutName);
        this.getElementsByName(driver, "sutDesc").get(0).sendKeys(desc);
    }

    protected void createNewSutDeployedByElastestWithCommands(
            WebDriver driver) {

    }

    protected void createNewSutDeployedByElastestWithImage(WebDriver driver,
            String sutName, String desc, String image, String port,
            Map<String, String> params) throws InterruptedException {
        this.createSutAndInsertCommonFields(driver, sutName, desc);

        this.getElementsByName(driver, "managedSut").get(0).click();
        this.getElementsByName(driver, "dockerImageRadio").get(0).click();
        this.getElementsByName(driver, "specification").get(0).sendKeys(image);

        if (port != null && !"".equals(port)) {
            this.getElementsByName(driver, "port").get(0).sendKeys(port);
        }

        // Parameters TODO

        // Save
        this.clickSaveSut(driver);
    }

    protected void createNewSutDeployedByElastestWithCompose(WebDriver driver,
            String sutName, String desc, String compose, String mainServiceName,
            String port, Map<String, String> params, boolean https)
            throws InterruptedException {
        this.createSutAndInsertCommonFields(driver, sutName, desc);

        this.getElementsByName(driver, "managedSut").get(0).click();
        this.getElementsByName(driver, "dockerComposeRadio").get(0).click();
        this.getElementById(driver, "composeSpec").get(0).sendKeys(compose);
        this.getElementsByName(driver, "mainService").get(0)
                .sendKeys(mainServiceName);

        if (https) {
            selectItem(driver, "https", "Select a protocol");
        }
        if (port != null && !"".equals(port)) {
            this.getElementsByName(driver, "port").get(0).sendKeys(port);
        }

        // Parameters TODO

        // Save
        this.clickSaveSut(driver);
    }

    protected void createNewSutDeployedOutsideWithManualInstrumentation(
            WebDriver driver, String sutName, String desc, String ip,
            Map<String, String> params) throws InterruptedException {
        this.createSutAndInsertCommonFields(driver, sutName, desc);

        this.getElementsByName(driver, "managedSut").get(0).click();
        this.getElementsByName(driver, "dockerImageRadio").get(0).click();
        this.getElementsByName(driver, "specification").get(0).sendKeys(ip);

        // Parameters TODO

        // Save
        this.clickSaveSut(driver);
    }

    protected void clickSaveSut(WebDriver driver) throws InterruptedException {
        Thread.sleep(2000);
        log.debug("Saving Sut");
        this.getElementByXpath(driver, "//button[contains(string(), 'SAVE')]")
                .get(0).click();
        Thread.sleep(1000);
    }

    protected String getSutsTableXpathFromProjectPage() {
        String id = "sutsTable";
        String xpath = "//td-data-table[@id='" + id + "']";
        return xpath;
    }

    protected String getSutXpathFromProjectPage(String sutName) {
        String xpath = getSutsTableXpathFromProjectPage();
        xpath += "//*/td/span[text()='" + sutName + "']";

        return xpath;
    }

    protected boolean etSutExistsIntoProject(WebDriver driver,
            String projectName, String sutName) {
        log.info("Checking if Sut {} exists into Project {}", sutName,
                projectName);
        this.navigateToETProject(driver, projectName);

        try {
            // Sleep for wait to load tables
            Thread.sleep(1500);
        } catch (InterruptedException e) {
        }

        String sutsTableXpath = getSutsTableXpathFromProjectPage();

        // If sut table exist
        if (this.driver.findElements(By.xpath(sutsTableXpath)).size() > 0) {
            String sutXpath = getSutXpathFromProjectPage(sutName);
            boolean sutExist = this.elementExistsByXpath(driver, sutXpath);
            String existStr = sutExist ? "already exist" : "does not exist";
            log.info("Sut {} {} into Project {}", sutName, existStr,
                    projectName);
            return sutExist;
        } else {
            log.warn("Sut table does not exist");
            return false;
        }
    }

    protected boolean etSutExistsIntoTLProject(WebDriver driver,
            String projectName, String sutName) {
        log.info("Checking if Sut {} exists into Project TL {}", sutName,
                projectName);
        try {
            // Sleep for wait to load tables
            Thread.sleep(1500);
        } catch (InterruptedException e) {
        }

        String sutsTableXpath = getSutsTableXpathFromProjectPage();

        // If sut table exist
        if (this.driver.findElements(By.xpath(sutsTableXpath)).size() > 0) {
            String sutXpath = getSutXpathFromProjectPage(sutName);
            boolean sutExist = this.elementExistsByXpath(driver, sutXpath);
            String existStr = sutExist ? "already exist" : "does not exist";
            log.info("Sut {} {} into TL Project {}", sutName, existStr,
                    projectName);
            return sutExist;
        } else {
            log.warn("Sut table does not exist");
            return false;
        }
    }

    protected void selectItem(WebDriver driver, String item,
            String selectDesc) {
        String sutSelectXpath = "//md-select/div/span[contains(string(), '"
                + selectDesc + "')]";
        this.getElementByXpath(driver, sutSelectXpath).get(0).click();

        if (item != null) {
            this.getElementByXpath(driver,
                    "//md-option[contains(string(), '" + item + "')]").get(0)
                    .click();
        }
    }

    /* ************** */
    /* **** TJob **** */
    /* ************** */

    protected String getTJobsTableXpathFromProjectPage() {
        String id = "tJobs";
        String xpath = "//td-data-table[@id='" + id + "']";
        return xpath;
    }

    protected String getTJobXpathFromProjectPage(String tJobName) {
        String xpath = getTJobsTableXpathFromProjectPage();
        xpath += "//*/td/span[text()='" + tJobName + "']";

        return xpath;
    }

    protected boolean etTJobExistsIntoProject(WebDriver driver,
            String projectName, String tJobName) {
        log.info("Checking if TJob {} exists into Project {}", tJobName,
                projectName);
        this.navigateToETProject(driver, projectName);

        try {
            // Sleep for wait to load tables
            Thread.sleep(1500);
        } catch (InterruptedException e) {
        }

        String tJobsTableXpath = getTJobsTableXpathFromProjectPage();

        // If tjob table exist
        if (this.driver.findElements(By.xpath(tJobsTableXpath)).size() > 0) {
            String tJobXpath = getTJobXpathFromProjectPage(tJobName);
            boolean tJobExist = this.elementExistsByXpath(driver, tJobXpath);
            String existStr = tJobExist ? "already exist" : "does not exist";
            log.info("TJob {} {} into Project {}", tJobName, existStr,
                    projectName);
            return tJobExist;

        } else {
            return false;
        }
    }

    protected void createNewTJob(WebDriver driver, String tJobName,
            String testResultPath, String sutName, String dockerImage,
            boolean imageCommands, String commands,
            Map<String, String> parameters, List<String> tssList,
            Map<String, List<String>> multiConfigurations) {
        log.info("Wait for the \"New TJob\" button ");
        getElementById(driver, "newTJobBtn").get(0).click();

        WebDriverWait waitService2 = new WebDriverWait(driver, 20); //
        By serviceFieldTJobName = By.name("tJobName");
        waitService2.until(visibilityOfElementLocated(serviceFieldTJobName));
        driver.findElement(serviceFieldTJobName).sendKeys(tJobName);

        if (testResultPath != null) {
            driver.findElement(By.name("resultsPath")).sendKeys(testResultPath);
        }

        // Select SuT
        String sutSelectXpath = "//md-select/div/span[contains(string(), 'Select a SuT')]";
        this.getElementByXpath(driver, sutSelectXpath).get(0).click();

        if (sutName != null) {
            this.getElementByXpath(driver,
                    "//md-option[contains(string(), '" + sutName + "')]").get(0)
                    .click();
        } else {
            this.getElementByXpath(driver,
                    "//md-option[contains(string(), 'None')]").get(0).click();
        }

        // Image and commands
        driver.findElement(By.name("tJobImageName")).sendKeys(dockerImage);

        if (imageCommands) {
            driver.findElement(By.name("toggleCommands")).click();
        } else {
            driver.findElement(By.name("commands")).sendKeys(commands);
        }

        // Parameters TODO id=addParameter

        if (parameters != null) {
            int currentParam = 0;

            for (HashMap.Entry<String, String> param : parameters.entrySet()) {
                // Add new param
                driver.findElement(By.id("addNewParameter")).click();

                // Set name
                driver.findElement(By.id("parameterName" + currentParam))
                        .sendKeys(param.getKey());

                // Set value
                driver.findElement(By.id("parameterValue" + currentParam))
                        .sendKeys(param.getValue());
            }
        }

        // MultiConfigurations
        if (multiConfigurations != null && multiConfigurations.size() > 0) {
            getElementById(driver, "input-multiConfigCheckbox").get(0)
                    .sendKeys(Keys.SPACE);

            int currentMultiConfig = 0;
            for (HashMap.Entry<String, List<String>> multiConfig : multiConfigurations
                    .entrySet()) {
                if (multiConfig.getValue().size() > 0) {
                    // Add new multi config
                    getElementById(driver, "addNewMultiConfiguration").get(0)
                            .click();

                    // Set name
                    getElementById(driver,
                            "multiConfigName" + currentMultiConfig).get(0)
                                    .sendKeys(multiConfig.getKey());

                    int currentValue = 0;
                    for (String value : multiConfig.getValue()) {
                        if (currentValue > 0) {
                            getElementById(driver,
                                    "addValueToMultiConfig"
                                            + currentMultiConfig).get(0)
                                                    .click();
                        }
                        // Set value
                        driver.findElement(By.id("multiConfig"
                                + currentMultiConfig + "Value" + currentValue))
                                .sendKeys(value);
                        currentValue++;

                    }

                    currentMultiConfig++;
                }
            }
        }

        // TSS
        if (tssList != null) {
            for (String tss : tssList) {
                String tssCheckbox = "//md-checkbox[@title='Select " + tss
                        + "']";
                this.getElementByXpath(driver, tssCheckbox).get(0).click();
            }
        }

        // Save
        driver.findElement(By.xpath("//button[contains(string(), 'SAVE')]"))
                .click();
    }

    protected void runTJobFromProjectPage(WebDriver driver, String tJobName) {
        log.info("Run TJob");

        WebDriverWait waitService = new WebDriverWait(driver, 30); // seconds
        By projectAvailable = By.id("tJobs");
        waitService.until(presenceOfElementLocated(projectAvailable));

        String xpath = getTJobXpathFromProjectPage(tJobName);

        // Navigate to tjob
        driver.findElement(By.xpath(xpath)).click();
        try {
            Thread.sleep(1200);
        } catch (InterruptedException e) {
        }
        this.getElementByXpath(driver, "//button[@title='Run TJob']").get(0)
                .click();
    }

    protected void startTestSupportService(WebDriver driver,
            String supportServiceLabel) {
        WebElement tssNavButton = driver
                .findElement(By.id("nav_support_services"));
        if (!tssNavButton.isDisplayed()) {
            driver.findElement(By.id("main_menu")).click();
        }
        tssNavButton.click();

        WebDriverWait waitElement = new WebDriverWait(driver, 3);
        By supportService;
        int numRetries = 1;
        do {
            driver.findElement(By.className("mat-select-trigger")).click();
            supportService = By.xpath("//md-option[contains(string(), '"
                    + supportServiceLabel + "')]");
            try {
                waitElement.until(visibilityOfElementLocated(supportService));
                log.info("Element {} already available", supportService);
                break;

            } catch (Exception e) {
                numRetries++;
                if (numRetries > 6) {
                    log.warn("Max retries ({}) reached ... leaving",
                            numRetries);
                    break;
                }
                log.warn("Element {} not available ... retrying",
                        supportService);
            }
        } while (true);
        driver.findElement(supportService).click();

        log.info("Create and wait instance");
        driver.findElement(By.id("create_instance")).click();
        WebDriverWait waitService = new WebDriverWait(driver, 120); // seconds
        By serviceDetailButton = By
                .xpath("//button[@title='View Service Detail']");
        waitService.until(visibilityOfElementLocated(serviceDetailButton));
        driver.findElement(serviceDetailButton).click();
    }

    protected void checkFinishTJobExec(WebDriver driver, int timeout,
            String expectedResult, boolean waitForMetrics) {

        log.info("Wait for the execution page to show");

        WebDriverWait waitEnd = new WebDriverWait(driver, timeout);

        if (waitForMetrics) {
            WebDriverWait waitMetrics = new WebDriverWait(driver, timeout);
            log.info("Wait for metrics");
            waitMetrics.until(presenceOfElementLocated(By.className("tick")));
        }

        log.info("Wait for Execution ends");
        waitEnd.until(invisibilityOfElementLocated(By.id("runningSpinner")));

        WebDriverWait waitResult = new WebDriverWait(driver, 25);

        log.info("Check finish Execution status. Expected result {}",
                expectedResult);
        waitResult.until(textToBePresentInElementLocated(By.id("resultMsgText"),
                expectedResult));
    }

    protected void deleteJob(WebDriver drive, String jobName) {
        // http://172.17.0.2:8080/job/FJob1/doDelete
    }

//    Test Cases
    protected WebElement expandExecTestSuite(WebDriver driver, int position) {
        // Real position starts from 1 instead of 0
        String suiteExpansionXpath = "//*[@id=\"testSuitesView\"]/etm-test-suites-view//td-expansion-panel["
                + position + "]";
        log.debug("Expanding Test Suite in position {}", position);

        WebElement suiteExpansionElement = getElementByXpath(driver,
                suiteExpansionXpath).get(0);
        suiteExpansionElement.click();
        return suiteExpansionElement;
    }

    protected void navigateToExecTestCase(WebDriver driver, int suitePosition,
            int casePosition, boolean withSuiteExpand) {
        if (withSuiteExpand) {
            expandExecTestSuite(driver, suitePosition);
        }

        // Real position starts from 1 instead of 0
        String xpath = "//*[@id=\"testSuitesView\"]/etm-test-suites-view//td-expansion-panel["
                + suitePosition + "]//td-data-table//tr[" + casePosition
                + "]/td[3]/div";

        log.debug(
                "Navigating to Test Case in position {} of Test Suite in position {}",
                casePosition, suitePosition);

        getElementByXpath(driver, xpath).get(0).click();
    }

    public By byDom(String domExpression) {
        final Object o = ((JavascriptExecutor) driver)
                .executeScript("return " + domExpression + ";");

        if (o instanceof WebElement) {
            return new By() {
                @Override
                public List<WebElement> findElements(
                        SearchContext searchContext) {
                    return new ArrayList<WebElement>() {
                        private static final long serialVersionUID = 1L;

                        {
                            add((WebElement) o);
                        }
                    };
                }
            };
        } else {
            return null;
        }
    }

    public void setupTestBrowser(TestInfo testInfo, BrowserType browser,
            WebDriver driver) throws MalformedURLException {
        String testName = testInfo.getTestMethod().get().getName();

        log.info("EUS hub URL: {}", eusURL);
        if (eusURL != null) {
            DesiredCapabilities caps = new DesiredCapabilities();
            if (browser.equals(BrowserType.CHROME)) {
                DesiredCapabilities.chrome();
                caps.setBrowserName("chrome");
            } else {
                DesiredCapabilities.firefox();
                caps.setBrowserName("firefox");
            }
            caps.setCapability("testName", testName);
            this.driver = new RemoteWebDriver(new URL(eusURL), caps);
            driver = this.driver;
        } else {
            this.driver = driver;
        }
    }

    public void setupTestBrowser(TestInfo testInfo, BrowserType browser)
            throws MalformedURLException {
        String testName = testInfo.getTestMethod().get().getName();

        DesiredCapabilities caps;
        caps = browser.equals(BrowserType.CHROME) ? DesiredCapabilities.chrome()
                : DesiredCapabilities.firefox();
        caps.setCapability("testName", testName);
        driver = new RemoteWebDriver(new URL(eusURL), caps);
    }

}
