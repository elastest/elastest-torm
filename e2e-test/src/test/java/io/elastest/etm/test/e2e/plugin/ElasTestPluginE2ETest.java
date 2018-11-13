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
package io.elastest.etm.test.e2e.plugin;

import static java.lang.invoke.MethodHandles.lookup;
import static org.openqa.selenium.support.ui.ExpectedConditions.textToBePresentInElementLocated;
import static org.slf4j.LoggerFactory.getLogger;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;

import io.elastest.etm.test.base.EtmPluginBaseTest;
import io.github.bonigarcia.BrowserType;
import io.github.bonigarcia.SeleniumExtension;

/**
 * E2E ElasTest Jenkins Plugin test.
 *
 * @author franciscoRdiaz(https://github.com/franciscoRdiaz)
 * @since 0.1.1
 */
@Tag("e2e")
@DisplayName("E2E test for the ElasTest Jenkins plugin")
@TestInstance(Lifecycle.PER_CLASS)
@ExtendWith(SeleniumExtension.class)
public class ElasTestPluginE2ETest extends EtmPluginBaseTest {

    final Logger log = getLogger(lookup().lookupClass());

    final String unitTestScript = "node{\n" + "    elastest(tss: ['EUS']) {\n"
            + "        stage ('Executing Test') {\n"
            + "            echo 'Print env variables'\n"
            + "            sh 'env'\n" + "            mvnHome = tool 'M3.3.9'\n"
            + "            echo 'Cloning repository'\n"
            + "            git 'https://github.com/elastest/demo-projects'\n"
            + "            echo 'Run test'\n"
            + "            sh \"cd ./unit-java-test/;'${mvnHome}/bin/mvn' -DforkCount=0 test\"\n";// +

    @Test
    @DisplayName("Standar Job Plugin")
    @Disabled
    void testInstallElasTestPlugin(ChromeDriver driver) throws Exception {
        this.driver = driver;
        navigateTo(driver, jenkinsPluginManagerAd);
        installElasTestPlugin(driver);
        navigateTo(driver, pluginSettings);
        pluginConfiguration(driver);
        navigateTo(driver, jenkinsCIUrl);

        // Creation of a new Free style Job
        driver.findElement(By.linkText("New Item")).click();
        createFreestyleJob(driver, "FJob1");
    }

    @Test
    @DisplayName("Pipeline plugin")
    void testPipelineJob(ChromeDriver defaultDriver) throws Exception {
        String testName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        
        if (eusURL != null) {
            this.setupTest(testName, BrowserType.CHROME);
        } else {
            driver = defaultDriver;
        }
        navigateTo(driver, jenkinsPluginManagerAd);
        loginOnJenkins(driver);
        installElasTestPlugin(driver);
        navigateTo(driver, pluginSettings);
        pluginConfiguration(driver);
        navigateTo(driver, jenkinsCIUrl);

        // Creation of a new Pipeline Job
        driver.findElement(By.linkText("New Item")).click();
        createPipelineJob(driver, "PJob_1", unitTestScript);

        executeJob(driver);

        String linkElasTest = driver
                .findElement(By.linkText("Open in ElasTest"))
                .getAttribute("href");
        if (secureElastest) {
            String split_url[] = linkElasTest.split("//");
            linkElasTest = split_url[0] + "//" + eUser + ":" + ePassword + "@"
                    + split_url[1];
            navigateTo(driver, linkElasTest);
        } else {
            driver.findElement(By.linkText("Open in ElasTest")).click();
        }

        WebDriverWait waitLogs = new WebDriverWait(driver, 60);
        log.info("Wait for build sucess traces");
        checkFinishTJobExec(driver, 180, "SUCCESS", false);
        WebElement logsView = driver.findElement(By.xpath(
                "//logs-view"));
        JavascriptExecutor jse2 = (JavascriptExecutor) driver;
        try {
            jse2.executeScript("arguments[0].scrollIntoView()", logsView);
            waitLogs.until(textToBePresentInElementLocated(
                    By.tagName("logs-view"), "BUILD SUCCESS"));
        } catch (Exception te) {
            jse2.executeScript("arguments[0].scrollIntoView()", logsView);
            waitLogs.until(textToBePresentInElementLocated(
                    By.tagName("logs-view"), "BUILD SUCCESS"));
        }
    }
    
    @Test
    @DisplayName("ETInET-Test: use plugin in a pipeline")
    void testETInETPluginInPipelineJob(ChromeDriver defaultDriver) throws Exception {
        String testName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        
        if (eusURL != null) {
            this.setupTest(testName, BrowserType.CHROME);
        } else {
            driver = defaultDriver;
        }
        navigateTo(driver, jenkinsCIUrl);
        loginOnJenkins(driver);
        // Creation of a new Pipeline Job
        driver.findElement(By.linkText("New Item")).click();
        createPipelineJob(driver, "PJob_1", unitTestScript);
        executeJob(driver);
        String linkElasTest = driver
                .findElement(By.linkText("Open in ElasTest"))
                .getAttribute("href");
        if (secureElastest) {
            String split_url[] = linkElasTest.split("//");
            linkElasTest = split_url[0] + "//" + eUser + ":" + ePassword + "@"
                    + split_url[1];
            navigateTo(driver, linkElasTest);
        } else {
            driver.findElement(By.linkText("Open in ElasTest")).click();
        }

        WebDriverWait waitLogs = new WebDriverWait(driver, 60);
        log.info("Wait for build sucess traces");
        checkFinishTJobExec(driver, 180, "SUCCESS", false);
        WebElement logsView = driver.findElement(By.xpath(
                "//logs-view"));
        JavascriptExecutor jse2 = (JavascriptExecutor) driver;
        try {
            jse2.executeScript("arguments[0].scrollIntoView()", logsView);
            waitLogs.until(textToBePresentInElementLocated(
                    By.tagName("logs-view"), "BUILD SUCCESS"));
        } catch (Exception te) {
            jse2.executeScript("arguments[0].scrollIntoView()", logsView);
            waitLogs.until(textToBePresentInElementLocated(
                    By.tagName("logs-view"), "BUILD SUCCESS"));
        }
    }
}
