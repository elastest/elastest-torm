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
package io.elastest.etm.test.e2e.example;

import static java.lang.Thread.sleep;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.openqa.selenium.Keys.RETURN;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import br.eti.kinoshita.testlinkjavaapi.constants.ActionOnDuplicate;
import br.eti.kinoshita.testlinkjavaapi.model.Build;
import br.eti.kinoshita.testlinkjavaapi.model.TestCase;
import br.eti.kinoshita.testlinkjavaapi.model.TestPlan;
import br.eti.kinoshita.testlinkjavaapi.model.TestProject;
import br.eti.kinoshita.testlinkjavaapi.model.TestSuite;
import io.elastest.etm.test.base.testlink.EtmTestLinkBaseTest;
import io.elastest.etm.test.utils.SampleTLData;
import io.github.bonigarcia.SeleniumExtension;
import io.github.bonigarcia.wdm.ChromeDriverManager;

/**
 * E2E ETM TestLink test.
 *
 * @author EduJG(https://github.com/EduJGURJC)
 * @since 0.1.1
 */
@Tag("ETinET")
@DisplayName("ETM ETinET E2E Demo test")
@ExtendWith(SeleniumExtension.class)
@TestInstance(Lifecycle.PER_CLASS)
public class EtInEtDemoTest extends EtmTestLinkBaseTest {

    static String fullteachingUrl = null;

    /* *** SuT 1 *** */
    final String sut1Name = "FullTeaching";
    String sut1Desc = "FullTeaching Software under Test";
    String sut1Compose = "version: '3'\r\n" + "services:\r\n"
            + " full-teaching-mysql:\r\n" + "   image: mysql:5.7.21\r\n"
            + "   environment:\r\n" + "     - MYSQL_ROOT_PASSWORD=pass\r\n"
            + "     - MYSQL_DATABASE=full_teaching\r\n"
            + "     - MYSQL_USER=ft-root\r\n" + "     - MYSQL_PASSWORD=pass\r\n"
            + " full-teaching-openvidu-server-kms:\r\n"
            + "   image: openvidu/openvidu-server-kms:1.7.0\r\n"
            + "   expose:\r\n" + "     - 8443\r\n" + "   environment:\r\n"
            + "     - KMS_STUN_IP=stun.l.google.com\r\n"
            + "     - KMS_STUN_PORT=19302\r\n"
            + "     - openvidu.secret=MY_SECRET\r\n"
            + "     - openvidu.publicurl=docker\r\n" + " full-teaching:\r\n"
            + "   image: codeurjc/full-teaching:demo\r\n" + "   depends_on:\r\n"
            + "     - full-teaching-mysql\r\n"
            + "     - full-teaching-openvidu-server-kms\r\n" + "   expose:\r\n"
            + "     - 5000\r\n" + "   environment:\r\n"
            + "     - WAIT_HOSTS=full-teaching-mysql:3306\r\n"
            + "     - WAIT_HOSTS_TIMEOUT=120\r\n"
            + "     - MYSQL_PORT_3306_TCP_ADDR=full-teaching-mysql\r\n"
            + "     - MYSQL_PORT_3306_TCP_PORT=3306\r\n"
            + "     - MYSQL_ENV_MYSQL_DATABASE=full_teaching\r\n"
            + "     - MYSQL_ENV_MYSQL_USER=ft-root\r\n"
            + "     - MYSQL_ENV_MYSQL_PASSWORD=pass\r\n"
            + "     - server.port=5000\r\n"
            + "     - openvidu.url=https://full-teaching-openvidu-server-kms:8443\r\n"
            + "     - openvidu.secret=MY_SECRET\r\n";

    String sut1MainService = "full-teaching";
    String sut1Port = "5000";
    
    @BeforeEach
    public void setupTest(TestInfo testInfo) throws MalformedURLException {
        log.info("Executing ");
        ChromeOptions options = new ChromeOptions();
        //options.addArguments("start-maximized");
        //other Selenium options you need 

        String eusURL = System.getenv("ET_EUS_API");
        if (eusURL == null) {
            //ElasTest EUS is not present
            ChromeDriverManager.getInstance().setup();
            driver = new ChromeDriver(options);
        } else {
            //ElasTest EUS is present
            DesiredCapabilities caps = new DesiredCapabilities();
            caps.setBrowserName("chrome");
            caps.setCapability("testName", testInfo.getTestMethod().get().getName());
            caps.setCapability(ChromeOptions.CAPABILITY, options);
            driver = new RemoteWebDriver(new URL(eusURL), caps);
        }
    }
    
    @Test
    @DisplayName("EtInEtDemoTest-Creates Test Data on TestLink and run manual test from ElasTest")
    void tlFullteachingDataTest(TestInfo info)
            throws InterruptedException, IOException {
        
        this.startTestLinkIfNecessaryWithNavigate(driver);

        log.info("Creating Fullteaching Data in TestLink...");
        SampleTLData fullteachingTLData = this
                .createFullteachingTLDataTest(driver);

        log.info("Opening ElasTest");
        this.navigateToTorm(driver);

        log.info("Syncronizing Fullteaching Data in TestLink...");
        this.syncTestlink(driver);

        String projectName = fullteachingTLData.getProject().getName();
        String suiteName = fullteachingTLData.getSuite().getName();
        String planName = fullteachingTLData.getPlan().getName();
        String buildName = fullteachingTLData.getBuild().getName();

        log.info("Checking if Fullteaching TL Project exists in ElasTest");
        this.tlEtmProjectExists(driver, projectName);
        log.info("Checking if Fullteaching TL Suite exists in ElasTest");
        this.tlEtmSuiteExistsByAbsolute(driver, projectName, suiteName);
        log.info("Checking if Fullteaching TL Plan exists in ElasTest");
        this.tlEtmPlanExistsByAbsolute(driver, projectName, planName);
        log.info("Checking if Fullteaching TL Build exists in ElasTest");
        this.tlEtmBuildExistsByAbsolute(driver, projectName, planName,
                buildName);

        String caseName = fullteachingTLData.getTestCases().get(0).getName();
        log.info(
                "Checking if Fullteaching Test Case '{}' exists in Suite - ElasTest",
                caseName);
        this.tlEtmSuiteCaseExists(driver, projectName, suiteName, caseName);
        log.info(
                "Checking if Fullteaching Test Case '{}' is associated to a Plan in ElasTest",
                caseName);
        this.tlEtmPlanCaseExists(driver, projectName, planName, caseName);
        log.info(
                "Checking if Fullteaching Test Case '{}' exists in Build - ElasTest",
                caseName);
        this.tlEtmBuildCaseExists(driver, projectName, planName, buildName,
                caseName);

        this.createFullTeachingSutOnEtTlGui(driver, fullteachingTLData);
        this.setTestPlanSut(driver, sut1Name, planName);
        this.executeFullteachingTestPlan(driver, fullteachingTLData, null);
    }

    void createFullTeachingSutOnEtTlGui(WebDriver driver,
            SampleTLData fullteachingTLData) throws InterruptedException {
        log.info("Creating the internal SUT: {}", sut1Name);
        String projectName = fullteachingTLData.getProject().getName();
        navigateToTLEtmProject(driver, projectName);
        if (!etSutExistsIntoTLProject(driver, projectName, sut1Name)) {
            createNewSutDeployedByElastestWithCompose(driver, sut1Name,
                    sut1Desc, sut1Compose, sut1MainService, sut1Port, null,
                    true);
            log.info("Created the new internal SUT");
        } else {
            log.info("Created the new internal SUT");
        }
    }

    void setTestPlanSut(WebDriver driver, String sutName, String planName) {
        navigateToTLEtmPlan(driver, planName);
        WebElement btnNewSut = getElementById(driver, "btnEditPlan").get(0);
        btnNewSut.click();
        selectInternalSut(driver, sutName);
    }

    void executeFullteachingTestPlan(WebDriver driver,
            SampleTLData fullteachingTLData, String sutName)
            throws InterruptedException {
        String projectName = fullteachingTLData.getProject().getName();
        String planName = fullteachingTLData.getPlan().getName();

        log.info("Starting Fullteaching Plan '{}' execution", planName);
        this.startTLEtmPlanExecutionWithNavigate(driver, projectName, planName);
        TestCase tCase = fullteachingTLData.getTestCases().get(0);
        log.info("Executing Fullteaching TestCase '{}'", tCase.getName());
        this.executeFullteachingLogInTCase(driver);

        log.info("Wait To ETM Plan Execution Ends");
        this.waitToTLEtmPlanExecutionEnds(driver);
    }

    protected void executeFullteachingLogInTCase(WebDriver driver)
            throws InterruptedException {
        this.waitForBrowserStarted(driver);
        By vncCanvas = By.id("vnc_canvas");
        WebElement canvas = driver.findElement(vncCanvas);
        sleep(SECONDS.toMillis(2));

        log.info("Click browser navigation bar and navigate");
        int navigationTimeSec = 9;
        log.info("Waiting {} seconds (simulation of manual navigation)",
                navigationTimeSec);
        sleep(SECONDS.toMillis(navigationTimeSec));

        log.info("Click To Log In button");
        new Actions(driver).moveToElement(canvas, 325, 29).click().build()
                .perform();
        sleep(SECONDS.toMillis(2));

        log.info("Click To Email and type value");
        new Actions(driver).moveToElement(canvas, 166, 76).click()
                .sendKeys("teacher@gmail.com" + RETURN).build().perform();
        sleep(SECONDS.toMillis(1));

        log.info("Click To Password and type value");
        new Actions(driver).moveToElement(canvas, 166, 98).click()
                .sendKeys("pass" + RETURN).build().perform();
        sleep(SECONDS.toMillis(1));

        log.info("Click To Modal window Log In button");
        new Actions(driver).moveToElement(canvas, 188, 130).click().build()
                .perform();
        sleep(SECONDS.toMillis(navigationTimeSec));

        this.saveTLEtmPlanCurrentCaseExecution(driver);
    }

    protected SampleTLData createFullteachingTLDataTest(WebDriver driver)
            throws IOException {
        this.driver = driver;

        SampleTLData fullteachingTLData = new SampleTLData();

        // Create Project
        String pjName = "FullTeaching Project";
        log.info("Creating TL Project with name: {}", pjName);
        TestProject project = new TestProject(0, pjName, "FT",
                "FullTeaching Project for manual testing", false, false, false,
                false, true, true);
        project = this.createTlTestProject(driver, project);

        // Create Suite
        String suiteName = "FullTeaching Suite";
        log.info("Creating TL Suite with name: {}", suiteName);
        TestSuite suite = new TestSuite(0, project.getId(), suiteName,
                "There are the suite details", null, null, true,
                ActionOnDuplicate.BLOCK);
        suite = this.createTlTestSuite(suite);

        // Create Plan
        String planName = "FullTeaching Plan";
        log.info("Creating TL Plan with name: {}", planName);
        TestPlan plan = new TestPlan(0, planName, project.getName(),
                "This is a Plan note", true, true);
        plan = this.createTlTestPlan(driver, plan);

        // Create Build
        String buildName = "FullTeaching build";
        log.info("Creating TL Build with name: {}", buildName);
        Build build = new Build(0, plan.getId(), buildName,
                "This is a Build note");
        build = this.createTlBuild(driver, build);

        // Create Test Case and associate to plan
        String caseName = "Log In Test Case ";
        log.info("Creating TL Case with name: {}", caseName);
        TestCase testCase = new TestCase();
        testCase.setId(0);
        testCase.setName(caseName);
        testCase.setTestSuiteId(suite.getId());
        testCase.setTestProjectId(project.getId());
        testCase.setAuthorLogin("elastest");
        testCase.setSummary("Open FullTeaching and LogIn");
        testCase.setPreconditions("FullTeaching app should be started");

        testCase = this.createTlTestCase(driver, testCase);
        testCase.setTestProjectId(project.getId());
        fullteachingTLData.getTestCases().add(testCase);
        log.info("Associating TL Case '{}' to Test Plan", caseName);
        this.addTLTestCaseToTestPlan(driver, testCase, plan.getId());

        fullteachingTLData.setProject(project);
        fullteachingTLData.setSuite(suite);
        fullteachingTLData.setPlan(plan);
        fullteachingTLData.setBuild(build);
        return fullteachingTLData;
    }
}
