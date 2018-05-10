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
import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated;
import static org.openqa.selenium.support.ui.ExpectedConditions.textToBePresentInElementLocated;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;

import io.elastest.etm.test.base.EtmBaseTest;
import io.github.bonigarcia.DockerBrowser;
import io.github.bonigarcia.SeleniumExtension;

/**
 * E2E ETM test.
 *
 * @author EduJG(https://github.com/EduJGURJC)
 * @since 0.1.1
 */
@Tag("e2e")
@DisplayName("ETM E2E test of Fullteaching project")
@ExtendWith(SeleniumExtension.class)
public class EtmFullteachingE2eTest extends EtmBaseTest {

    final Logger log = getLogger(lookup().lookupClass());
    final String projectName = "FullTeaching";
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
            + "   image: pablofuente/full-teaching:latest\r\n"
            + "   depends_on:\r\n" + "     - full-teaching-mysql\r\n"
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
    String tJobImage = "elastest/test-etm-alpinegitjava";
    final int timeout = 600;

    void createProjectAndSut(WebDriver driver) throws InterruptedException {
        navigateToTorm(driver);
        // Create Project
        if (!etProjectExists(driver, projectName)) {
            createNewETProject(driver, projectName);
        }
        // Create SuT
        if (!etSutExistsIntoProject(driver, projectName, sut1Name)) {
            createNewSutDeployedByElastestWithCompose(driver, sut1Name,
                    sut1Desc, sut1Compose, sut1MainService, sut1Port, null);
        }
    }

    @Test
    @DisplayName("Create and Executes Fullteaching Teacher and Student Testing")
    void testTeacherandStudentTesting(
            @DockerBrowser(type = CHROME) RemoteWebDriver driver)
            throws InterruptedException {
        String tJobName = "Teacher and Student Testing";
        String tJobTestResultPath = "/full-teaching/spring/backend/target/surefire-reports/";
        String commands = "git clone https://github.com/pabloFuente/full-teaching; cd full-teaching/spring/backend; mvn -Dtest=FullTeachingTestE2ESleep -B test;";
        this.fullTeachingBaseTest(driver, tJobName, tJobTestResultPath,
                tJobImage, commands, sut1Name, "SUCCESS");
    }

    @Test
    @DisplayName("Create and Executes Fullteaching E2E REST operations")
    void testE2eRestOperations(
            @DockerBrowser(type = CHROME) RemoteWebDriver driver)
            throws InterruptedException {
        String tJobName = "E2E REST operations";
        String tJobTestResultPath = "/full-teaching-experiment/target/surefire-reports/";
        String commands = "git clone https://github.com/elastest/full-teaching-experiment; cd full-teaching-experiment; mvn -Dtest=FullTeachingTestE2EREST -B test;";
        this.fullTeachingBaseTest(driver, tJobName, tJobTestResultPath,
                tJobImage, commands, sut1Name, "SUCCESS");
    }

    @Test
    @DisplayName("Create and Executes Fullteaching E2E Teacher + Student VIDEO-SESSION")
    void testE2eTeacherStudentVideoSession(
            @DockerBrowser(type = CHROME) RemoteWebDriver driver)
            throws InterruptedException {
        String tJobName = "E2E Teacher + Student VIDEO-SESSION";
        String tJobTestResultPath = "/full-teaching-experiment/target/surefire-reports/";
        String commands = "git clone https://github.com/elastest/full-teaching-experiment; cd full-teaching-experiment; mvn -Dtest=FullTeachingTestE2EVideoSession -B test;";

        this.fullTeachingBaseTest(driver, tJobName, tJobTestResultPath,
                tJobImage, commands, sut1Name, "SUCCESS");
    }

    @Test
    @DisplayName("Create and Executes Fullteaching E2E Teacher + Student CHAT")
    void testE2eTeacherStudentChat(
            @DockerBrowser(type = CHROME) RemoteWebDriver driver)
            throws InterruptedException {
        String tJobName = "E2E Teacher + Student CHAT";
        String tJobTestResultPath = "/full-teaching-experiment/target/surefire-reports/";
        String commands = "git clone https://github.com/elastest/full-teaching-experiment; cd full-teaching-experiment; mvn -Dtest=FullTeachingTestE2EChat -B test;";

        this.fullTeachingBaseTest(driver, tJobName, tJobTestResultPath,
                tJobImage, commands, sut1Name, "SUCCESS");
    }

    public void fullTeachingBaseTest(RemoteWebDriver driver, String tJobName,
            String tJobTestResultPath, String tJobImage, String commands,
            String sutName, String result) throws InterruptedException {
        this.driver = driver;
        this.createProjectAndSut(driver);

        if (!etTJobExistsIntoProject(driver, projectName, tJobName)) {
            List<String> tssList = new ArrayList<>();
            tssList.add("EUS");

            createNewTJob(driver, tJobName, tJobTestResultPath, sut1Name,
                    tJobImage, false, commands, null, tssList);
        } else {
            navigateToETProject(driver, projectName);
        }

        // Run TJob
        runTJobFromProjectPage(driver, tJobName);

        WebDriverWait waitLogs = new WebDriverWait(driver, timeout);
        log.info("Wait for metrics");
        waitLogs.until(presenceOfElementLocated(By.className("tick")));
        log.info("Wait for build sucess traces");
        waitLogs.until(textToBePresentInElementLocated(By.tagName("logs-view"),
                "BUILD " + result));
    }
}
