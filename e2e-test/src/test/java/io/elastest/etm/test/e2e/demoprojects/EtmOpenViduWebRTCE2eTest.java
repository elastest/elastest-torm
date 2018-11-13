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

import java.util.ArrayList;
import java.util.List;

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
@DisplayName("ETM E2E test of OpenVidu WebRTC project")
@ExtendWith(SeleniumExtension.class)
public class EtmOpenViduWebRTCE2eTest extends EtmBaseTest {

    final Logger log = getLogger(lookup().lookupClass());
    final String projectName = "OpenVidu WebRTC";
    final String sutName = "OpenVidu Test App";
    final int timeout = 350;

    void createProjectAndSut(WebDriver driver) throws Exception {
        navigateToTorm(driver);
        if (!etProjectExists(driver, projectName)) {
            createNewETProject(driver, projectName);
        }
        if (!etSutExistsIntoProject(driver, projectName, sutName)) {
            // Create SuT
            String sutDesc = "OpenVidu Description";
            String sutImage = "openvidu/testapp:elastest";
            String sutPort = "4443";
            createNewSutDeployedByElastestWithImage(driver, sutName, sutDesc,
                    sutImage, sutPort, null);
        }

    }

    @Test
    @DisplayName("Create OpenVidu WebRTC project Chrome Test")
    void testCreateOpenViduWebRTC(
            @DockerBrowser(type = CHROME) RemoteWebDriver driver)
            throws Exception {
        driver = setupTestBrowser(new Object() {
        }.getClass().getEnclosingMethod().getName(), BrowserType.CHROME,
                driver);
        this.createProjectAndSut(driver);
        navigateToETProject(driver, projectName);

        String tJobName = "Videocall Test";
        if (!etTJobExistsIntoProject(driver, projectName, tJobName)) {
            String tJobTestResultPath = "/demo-projects/openvidu-test/target/surefire-reports/";
            String tJobImage = "elastest/test-etm-alpinegitjava";
            String commands = "echo \"Cloning project\"; git clone https://github.com/elastest/demo-projects; cd demo-projects/openvidu-test; echo \"Compiling project\"; mvn -DskipTests=true -B package; echo \"Executing test\"; mvn -B test;";
            List<String> tssList = new ArrayList<>();
            tssList.add("EUS");

            createNewTJob(driver, tJobName, tJobTestResultPath, sutName,
                    tJobImage, false, commands, null, tssList, null);
        }
        // Run TJob
        runTJobFromProjectPage(driver, tJobName);

        this.checkFinishTJobExec(driver, timeout, "SUCCESS", false);

    }
}
