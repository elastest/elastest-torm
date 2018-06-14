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
import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;

import io.elastest.etm.test.utils.RestClient;
import io.github.bonigarcia.DriverCapabilities;

public class EtmBaseTest {

    final Logger log = getLogger(lookup().lookupClass());

    protected String tormUrl = "http://172.17.0.1:37000/"; // local by default
    protected String secureTorm = "http://user:pass@172.17.0.1:37000/";

    protected String apiPath = "api";
    protected String tormApiUrl;

    protected String eUser = null;
    protected String ePassword = null;

    protected boolean secureElastest = false;

    protected WebDriver driver;

    protected RestClient restClient;

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
    }

    @AfterEach
    void teardown() throws IOException {
        if (driver != null) {
            log.info("Browser console at the end of the test");
            LogEntries logEntries = driver.manage().logs().get(BROWSER);
            logEntries.forEach((entry) -> log.info("[{}] {} {}",
                    new Date(entry.getTimestamp()), entry.getLevel(),
                    entry.getMessage()));
        }
    }

    protected void navigateTo(WebDriver driver, String url) {
        log.info("Navigate to: {}", url);
        driver.manage().window().setSize(new Dimension(1024, 1024));
        driver.manage().timeouts().implicitlyWait(5, SECONDS);
        driver.get(url);
    }

    protected void navigateToTorm(WebDriver driver) {
        log.info("Navigate to TORM");
        driver.manage().window().setSize(new Dimension(1024, 1024));
        driver.manage().timeouts().implicitlyWait(5, SECONDS);
        if (secureElastest) {
            driver.get(secureTorm);
        } else {
            driver.get(tormUrl);
        }
    }

    protected void navigateToRoot(WebDriver driver) {
        log.info("Navigate to Root Path (/)");
        driver.findElement(By.xpath(
                "//*[@id='main_nav']/div/md-toolbar/div/md-toolbar-row/span"))
                .click();
    }

    protected void navigateToProjects(WebDriver driver) {
        log.info("Navigate to Projects Path (/project)");
        this.getElementByXpath(driver, "//*[@id=\"nav_projects\"]").get(0)
                .click();
    }

    protected void navigateToElement(WebDriver driver, String id,
            String xpath) {
        this.getElementByIdXpath(driver, id, xpath).get(0).click();
    }

    protected boolean elementExists(WebDriver driver, String id, String xpath) {
        return this.getElementByIdXpath(driver, id, xpath).size() != 0;
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
        driver.findElement(
                By.xpath("//button[contains(string(), 'New Project')]"))
                .click();
        driver.findElement(By.name("project.name")).sendKeys(projectName);
        driver.findElement(By.xpath("//button[contains(string(), 'SAVE')]"))
                .click();
    }

    protected void removeETProject(WebDriver driver, String projectName) {
        this.navigateToRoot(driver);
        // TODO
    }

    protected void navigateToETProject(WebDriver driver, String projectName) {
        this.navigateToProjects(driver);
        log.info("Navigate to {} project", projectName);

        String id = "projects";
        String xpath = "//td-data-table[@id='" + id
                + "']//*/td/div[contains(string(), '" + projectName + "')]";
        this.navigateToElement(driver, id, xpath);
    }

    protected boolean etProjectExists(WebDriver driver, String projectName) {
        log.info("Checking if Project {} exists", projectName);
        // this.navigateToRoot(driver);
        String id = "projects";
        String xpath = "//td-data-table[@id='" + id + "']";

        // If exist PJ table (if there are some pj)

        if (getElementByXpath(driver, xpath).size() > 0) {
            xpath = xpath + "//*/td/div[contains(string(), '" + projectName
                    + "')]";
            return this.elementExists(driver, id, xpath);
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
        this.getElementByXpath(driver,
                "//button[contains(string(), 'New SuT')]").get(0).click();
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
        Thread.sleep(2000);
        log.debug("Saving Sut");
        this.getElementByXpath(driver, "//button[contains(string(), 'SAVE')]")
                .get(0).click();
        Thread.sleep(1000);
    }

    protected void createNewSutDeployedByElastestWithCompose(WebDriver driver,
            String sutName, String desc, String compose, String mainServiceName,
            String port, Map<String, String> params) {
        this.createSutAndInsertCommonFields(driver, sutName, desc);

        this.getElementsByName(driver, "managedSut").get(0).click();
        this.getElementsByName(driver, "dockerComposeRadio").get(0).click();
        this.getElementById(driver, "composeSpec").get(0).sendKeys(compose);
        this.getElementsByName(driver, "mainService").get(0)
                .sendKeys(mainServiceName);

        if (port != null && !"".equals(port)) {
            this.getElementsByName(driver, "port").get(0).sendKeys(port);
        }

        // Parameters TODO

        // Save
        this.getElementByXpath(driver, "//button[contains(string(), 'SAVE')]")
                .get(0).click();
    }

    protected void createNewSutDeployedOutsideWithManualInstrumentation(
            WebDriver driver, String sutName, String desc, String ip,
            Map<String, String> params) {
        this.createSutAndInsertCommonFields(driver, sutName, desc);

        this.getElementsByName(driver, "managedSut").get(0).click();
        this.getElementsByName(driver, "dockerImageRadio").get(0).click();
        this.getElementsByName(driver, "specification").get(0).sendKeys(ip);

        // Parameters TODO

        // Save
        this.getElementByXpath(driver, "//button[contains(string(), 'SAVE')]")
                .get(0).click();
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

        String id = "sutsTable";
        String xpath = "//td-data-table[@id='" + id + "']";

        // If sut table exist
        if (this.driver.findElements(By.xpath(xpath)).size() > 0) {
            xpath += "//*/td/span[text()='" + sutName + "']";
            boolean sutExist = this.elementExists(driver, id, xpath);
            String existStr = sutExist ? "exist" : "does not exist";
            log.info("Sut {} {} into Project {}", sutName, existStr,
                    projectName);
            return sutExist;
        } else {
            log.warn("Sut table does not exist");
            return false;
        }
    }

    /* ************** */
    /* **** TJob **** */
    /* ************** */

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

        String id = "tJobs";
        String xpath = "//td-data-table[@id='" + id + "']";

        // If tjob table exist
        if (this.driver.findElements(By.xpath(xpath)).size() > 0) {
            xpath += "//*/td/span[text()='" + tJobName + "']";
            return this.elementExists(driver, id, xpath);
        } else {
            return false;
        }
    }

    protected void createNewTJob(WebDriver driver, String tJobName,
            String testResultPath, String sutName, String dockerImage,
            boolean imageCommands, String commands,
            Map<String, String> parameters, List<String> tssList) {
        log.info("Wait for the \"New TJob\" button ");

        WebDriverWait waitService = new WebDriverWait(driver, 10); // seconds
        By serviceDetailButton = By
                .xpath("//button[contains(string(), 'New TJob')]");
        waitService.until(visibilityOfElementLocated(serviceDetailButton));
        driver.findElement(serviceDetailButton).click();

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

        // Parameters TODO

        // TSS
        if (tssList != null) {
            for (String tss : tssList) {
                driver.findElement(
                        By.xpath("//md-checkbox[@title='Select " + tss + "']"))
                        .click();
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

        driver.findElement(By
                .xpath("//td/div/span[contains(string(), '" + tJobName + "')]"))
                .click();
        driver.findElement(By.xpath("//button[@title='Run TJob']")).click();
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

    protected void executeJob(WebDriver driver) throws InterruptedException {
        log.info("Run Job");
        driver.findElement(By.xpath("//a[contains(string(), 'Build Now')]"))
                .click();

        log.info("Waiting for thes start of Job execution");
        By newBuildHistory = By
                .xpath("//*[@id=\"buildHistory\"]/div[2]/table/tbody/tr[2]");
        WebDriverWait waitService = new WebDriverWait(driver, 10);
        waitService.until(visibilityOfElementLocated(newBuildHistory));
        driver.findElement(By.xpath(
                "//*[@id=\"buildHistory\"]/div[2]/table/tbody/tr[2]/td/div[1]/div/a"))
                .click();

    }

    protected void deleteJob(WebDriver drive, String jobName) {
        // http://172.17.0.2:8080/job/FJob1/doDelete
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

}
