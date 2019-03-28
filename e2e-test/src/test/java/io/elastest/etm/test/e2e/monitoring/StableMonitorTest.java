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
package io.elastest.etm.test.e2e.monitoring;

import static io.github.bonigarcia.BrowserType.CHROME;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;

import io.elastest.etm.test.base.EtmBaseTest;
import io.github.bonigarcia.BrowserType;
import io.github.bonigarcia.DockerBrowser;
import io.github.bonigarcia.SeleniumExtension;

@Tag("e2e")
@DisplayName("ETM E2E test of Webapp project")
@ExtendWith(SeleniumExtension.class)
public class StableMonitorTest extends EtmBaseTest {
    final String projectName = "Monitoring";
    final String sutName = "Webapp";
    String tJobImage = "elastest/test-etm-alpinegitjava";
    String tJobTestResultPath = "/demo-projects/webapp/junit5-web-multiple-browsers-test/target/surefire-reports/";

    private static final Map<String, List<String>> tssMap;
    static {
        tssMap = new HashMap<String, List<String>>();
        tssMap.put("EUS", null);
    }

    final int timeout = 600;

    void createProjectAndSut(WebDriver driver) throws InterruptedException {
        navigateToTorm(driver);
        if (!etProjectExists(driver, projectName)) {
            createNewETProject(driver, projectName);
        }
        if (!etSutExistsIntoProject(driver, projectName, sutName)) {
            // Create SuT
            String sutDesc = "Webapp Description";
            String sutImage = "elastest/demo-web-java-test-sut";
            String sutPort = "8080";
            createNewSutDeployedByElastestWithImage(driver, sutName, sutDesc,
                    sutImage, sutPort, null);
        }
    }

    @Test
    @DisplayName("Create WebApp project Chrome Test")
    void testCreateChromeTest(TestInfo testInfo)
            throws InterruptedException, MalformedURLException {
        setupTestBrowser(testInfo, BrowserType.CHROME, null);

        this.createProjectAndSut(driver);

        navigateToETProject(driver, projectName);

        String tJobName = "Chrome Test";
        if (!etTJobExistsIntoProject(driver, projectName, tJobName)) {
            String commands = "git clone https://github.com/elastest/demo-projects; cd /demo-projects/webapp/junit5-web-multiple-browsers-test; mvn -Dtest=WebAppTest -B -Dbrowser=chrome test;";

            createNewTJob(driver, tJobName, tJobTestResultPath, sutName,
                    tJobImage, false, commands, null, tssMap, null, null);
        }
        // Run TJob
        runTJobFromProjectPage(driver, tJobName);

        this.checkFinishTJobExec(driver, timeout, "FAIL", true);
    }

    @Test
    @Disabled
    @DisplayName("Create and execute a Firefox Test")
    void testCreateFirefoxTest(
            @DockerBrowser(type = BrowserType.FIREFOX) RemoteWebDriver localDriver,
            TestInfo testInfo)
            throws InterruptedException, MalformedURLException {
        setupTestBrowser(testInfo, BrowserType.CHROME, localDriver);

        this.createProjectAndSut(driver);

        navigateToETProject(driver, projectName);

        String tJobName = "Firefox Test";
        if (!etTJobExistsIntoProject(driver, projectName, tJobName)) {
            String commands = "git clone https://github.com/elastest/demo-projects; cd /demo-projects/webapp/junit5-web-multiple-browsers-test; mvn -Dtest=WebAppTest -B -Dbrowser=firefox test;";

            createNewTJob(driver, tJobName, tJobTestResultPath, sutName,
                    tJobImage, false, commands, null, tssMap, null, null);
        }
        // Run TJob
        runTJobFromProjectPage(driver, tJobName);

        this.checkFinishTJobExec(driver, timeout, "FAIL", false);
    }

    @Test
    @Disabled
    @DisplayName("Create WebApp project Multi Test")
    void testCreateMultiTest(
            @DockerBrowser(type = CHROME) RemoteWebDriver localDriver,
            TestInfo testInfo)
            throws InterruptedException, MalformedURLException {
        setupTestBrowser(testInfo, BrowserType.CHROME, localDriver);

        this.createProjectAndSut(driver);

        navigateToETProject(driver, projectName);

        String tJobName = "WebApp Multi Test";
        if (!etTJobExistsIntoProject(driver, projectName, tJobName)) {
            String commands = "git clone https://github.com/elastest/demo-projects; cd /demo-projects/webapp/junit5-web-multiple-browsers-test; mvn -Dtest=WebAppTest -B -Dbrowser=$BROWSER test;";

            List<String> multiConfig1List = new ArrayList<>(
                    Arrays.asList("chrome", "firefox"));
            Map<String, List<String>> multiConfigurations = new HashMap<>();
            multiConfigurations.put("BROWSER", multiConfig1List);

            createNewTJob(driver, tJobName, tJobTestResultPath, sutName,
                    tJobImage, false, commands, null, tssMap,
                    multiConfigurations, null);
        }
        // Run TJob
        runTJobFromProjectPage(driver, tJobName);

        this.checkFinishTJobExec(driver, timeout * 2, "FAIL", false);

        // Wait for parent report and child view
        log.info("Waiting for Child View to be present");
        getElementById(driver, "childView", 20);

        log.info("Waiting for Parent Report View to be present");
        getElementById(driver, "parentReportView", 20);
    }

}
