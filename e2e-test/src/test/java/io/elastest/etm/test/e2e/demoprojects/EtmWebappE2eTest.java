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
package io.elastest.etm.test.e2e.demoprojects;

import static io.github.bonigarcia.BrowserType.CHROME;
import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;

import io.elastest.etm.test.base.EtmBaseTest;
import io.github.bonigarcia.BrowserType;
import io.github.bonigarcia.DockerBrowser;
import io.github.bonigarcia.SeleniumExtension;

/**
 * E2E ETM test.
 *
 * @author EduJG(https://github.com/EduJGURJC)
 * @since 0.1.1
 */
@Tag("e2e")
@DisplayName("ETM E2E test of Webapp project")
@ExtendWith(SeleniumExtension.class)
public class EtmWebappE2eTest extends EtmBaseTest {

    final Logger log = getLogger(lookup().lookupClass());
    final String projectName = "E2E_test_Webapp";
    final String sutName = "Webapp";
    String tJobImage = "elastest/test-etm-alpinegitjava";
    String tJobTestResultPath = "/demo-projects/web-java-test/target/surefire-reports/";
    List<String> tssList = new ArrayList<>(Arrays.asList("EUS"));

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
    void testCreateChromeTest(
            @DockerBrowser(type = CHROME) RemoteWebDriver rDriver)
            throws InterruptedException, MalformedURLException {
        String testName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        if (eusURL != null) {
            this.setupTest(testName);
        } else {
            driver = rDriver;
        }

        this.createProjectAndSut(driver);

        navigateToETProject(driver, projectName);

        String tJobName = "Chrome Test";
        if (!etTJobExistsIntoProject(driver, projectName, tJobName)) {
            String commands = "git clone https://github.com/elastest/demo-projects; cd demo-projects/web-java-test; mvn -Dtest=MultipleWebAppTests -B -Dbrowser=chrome test;";

            createNewTJob(driver, tJobName, tJobTestResultPath, sutName,
                    tJobImage, false, commands, null, tssList, null);
        }
        // Run TJob
        runTJobFromProjectPage(driver, tJobName);

        this.checkFinishTJobExec(driver, timeout, "FAIL", true);
    }

    @Test
    @DisplayName("Create and execute a Firefox Test")
    void testCreateFirefoxTest(
            @DockerBrowser(type = BrowserType.FIREFOX) RemoteWebDriver rDriver)
            throws InterruptedException, MalformedURLException {
        String testName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        if (eusURL != null) {
            this.setupTest(testName);
        } else {
            driver = rDriver;
        }

        this.createProjectAndSut(driver);

        navigateToETProject(driver, projectName);

        String tJobName = "Firefox Test";
        if (!etTJobExistsIntoProject(driver, projectName, tJobName)) {
            String commands = "git clone https://github.com/elastest/demo-projects; cd demo-projects/web-java-test; mvn -Dtest=MultipleWebAppTests -B -Dbrowser=firefox test;";

            createNewTJob(driver, tJobName, tJobTestResultPath, sutName,
                    tJobImage, false, commands, null, tssList, null);
        }
        // Run TJob
        runTJobFromProjectPage(driver, tJobName);

        this.checkFinishTJobExec(driver, timeout, "FAIL", false);
    }

    @Test
    @DisplayName("Create WebApp project Multi Test")
    void testCreateMultiTest(
            @DockerBrowser(type = CHROME) RemoteWebDriver rDriver)
            throws InterruptedException, MalformedURLException {
        String testName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        if (eusURL != null) {
            this.setupTest(testName);
        } else {
            driver = rDriver;
        }

        this.createProjectAndSut(driver);

        navigateToETProject(driver, projectName);

        String tJobName = "Chrome Test";
        if (!etTJobExistsIntoProject(driver, projectName, tJobName)) {
            String commands = "git clone https://github.com/elastest/demo-projects; cd demo-projects/web-java-test; mvn -Dtest=MultipleWebAppTests -B -Dbrowser=$BROWSER test;";

            List<String> multiConfig1List = new ArrayList<>(
                    Arrays.asList("chrome", "firefox"));
            Map<String, List<String>> multiConfigurations = new HashMap<>();
            multiConfigurations.put("BROWSER", multiConfig1List);

            createNewTJob(driver, tJobName, tJobTestResultPath, sutName,
                    tJobImage, false, commands, null, tssList,
                    multiConfigurations);
        }
        // Run TJob
        runTJobFromProjectPage(driver, tJobName);

        this.checkFinishTJobExec(driver, timeout, "FAIL", true);
    }

}
