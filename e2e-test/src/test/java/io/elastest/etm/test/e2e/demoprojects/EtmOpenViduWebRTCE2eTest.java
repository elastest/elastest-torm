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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    final String projectName = "E2E_test_OpenVidu_WebRTC";
    final String sutName = "OpenVidu Test App";
    final int timeout = 350;

    private static final Map<String, List<String>> tssMap;
    static {
        tssMap = new HashMap<String, List<String>>();
        tssMap.put("EUS", Arrays.asList("webRtcStats"));
    }

    void createProjectAndSut(WebDriver driver) throws Exception {
        navigateToTorm(driver);
        if (!etProjectExists(driver, projectName)) {
            createNewETProject(driver, projectName);
        }
        if (!etSutExistsIntoProject(driver, projectName, sutName)) {
            // Create SuT
            String sutDesc = "OpenVidu Description";
            String sutImage = "elastest/test-etm-alpinedockernode";
            String sutPort = "5000";
            String sutCommands = "echo \"### Create Dockerfile ###\"\n"
                    + "mkdir dockerimage;\n" + "cd dockerimage;\n"
                    + "echo \"FROM openvidu/openvidu-server-kms:2.6.0\" >> Dockerfile\n"
                    + "echo \"RUN apt-get update\" >> Dockerfile\n"
                    + "echo \"RUN apt-get install -y git\" >> Dockerfile\n"
                    + "echo \"RUN apt-get install -y nodejs npm\" >> Dockerfile\n"
                    + "echo \"RUN apt-get install -y curl\" >> Dockerfile\n"
                    + "echo \"RUN curl -sL https://deb.nodesource.com/setup_8.x | bash - \\\\\" >> Dockerfile\n"
                    + "echo \"    && apt-get install -y nodejs\" >> Dockerfile\n"
                    + "echo \"RUN npm install -g @angular/cli@7.1.3\" >> Dockerfile\n"
                    + "echo \"RUN npm install -g http-server\" >> Dockerfile\n"
                    + "echo \"EXPOSE 4443\" >> Dockerfile\n"
                    + "echo \"EXPOSE 5000\" >> Dockerfile\n"
                    + "echo -n \"CMD \" >> Dockerfile\n"
                    + "echo -n \"echo 'run supervisord';\" >> Dockerfile\n"
                    + "echo -n \"/usr/bin/supervisord & \" >> Dockerfile\n"
                    + "echo -n \"echo '##### BUILD OPENVIDU #####';\" >> Dockerfile\n"
                    + "echo -n \"git clone https://github.com/OpenVidu/openvidu.git; \" >> Dockerfile\n"
                    + "echo -n \"cd openvidu/openvidu-browser;\" >> Dockerfile\n"
                    + "echo -n \"npm install; \" >> Dockerfile\n"
                    + "echo -n \"npm run build; \" >> Dockerfile\n"
                    + "echo -n \"npm link; \" >> Dockerfile\n"
                    + "echo -n \"cd ..; \" >> Dockerfile\n"
                    + "echo -n \"cd openvidu-testapp; \" >> Dockerfile\n"
                    + "echo -n \"echo 'run npm install';\" >> Dockerfile\n"
                    + "echo -n \"npm install; \" >> Dockerfile\n"
                    + "echo -n \"npm link openvidu-browser; \" >> Dockerfile\n"
                    + "echo -n \"ng build --output-path ./dist;\" >> Dockerfile;\n"
                    + "echo -n \"cd dist;\" >> Dockerfile;\n"
                    + "echo -n \"openssl req -newkey rsa:2048 -new -nodes -x509 -days 3650 -subj '/CN=www.mydom.com/O=My Company LTD./C=US' -keyout key.pem -out cert.pem;\" >> Dockerfile;\n"
                    + "echo -n \" echo '##### RUN OPENVIDU #####';\" >> Dockerfile\n"
                    + "echo -n \"http-server -S -p 5000;\" >> Dockerfile;\n"
                    + "cat Dockerfile;\n" + "echo \"\";\n"
                    + "echo “### BUILD AND RUN ###”\n"
                    + "docker build -t openvidu/elastest .\n" + "echo \"\"\n"
                    + "echo \"Running image\"\n"
                    + "docker run --name $ET_SUT_CONTAINER_NAME --network $ET_NETWORK -e \"OPENVIDU_PUBLICURL=docker\" openvidu/elastest\n";

            createNewSutDeployedByElastestWithCommands(driver, sutCommands,
                    SutCommandsOptionEnum.IN_NEW_CONTAINER, sutName, sutDesc,
                    sutImage, sutPort, null, false);
        }

    }

    @Test
    @DisplayName("Create OpenVidu WebRTC project Chrome Test")
    void testCreateOpenViduWebRTC(
            @DockerBrowser(type = CHROME) RemoteWebDriver localDriver,
            TestInfo testInfo) throws Exception {
        setupTestBrowser(testInfo, BrowserType.CHROME, localDriver);
        this.createProjectAndSut(driver);
        navigateToETProject(driver, projectName);

        String tJobName = "Videocall Test";
        if (!etTJobExistsIntoProject(driver, projectName, tJobName)) {
            String tJobTestResultPath = "/demo-projects/openvidu-test/target/surefire-reports/";
            String tJobImage = "elastest/test-etm-alpinegitjava";
            String commands = "echo \"Cloning project\"; git clone https://github.com/elastest/demo-projects; cd demo-projects/openvidu-test; echo \"Compiling project\"; mvn -DskipTests=true -B package; echo \"Executing test\"; mvn -B test;";

            createNewTJob(driver, tJobName, tJobTestResultPath, sutName,
                    tJobImage, false, commands, null, tssMap, null);
        }
        // Run TJob
        runTJobFromProjectPage(driver, tJobName);

        this.checkFinishTJobExec(driver, timeout, "SUCCESS", false);

    }
}
