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
@DisplayName("ETM E2E test of Webapp project")
@ExtendWith(SeleniumExtension.class)
public class EtmWebappE2eTest extends EtmBaseTest {

	final Logger log = getLogger(lookup().lookupClass());
	final String projectName = "Webapp";
	final String sutName = "Webapp";
	final int timeout = 600;

	void createProjectAndSut(WebDriver driver) throws InterruptedException {
		if (!projectExists(driver, projectName)) {
			navigateToTorm(driver);

			createNewProject(driver, projectName);

			// Create SuT
			String sutDesc = "Webapp Description";
			String sutImage = "elastest/demo-web-java-test-sut";
			String sutPort = "8080";
			createNewSutDeployedByElastestWithImage(driver, sutName, sutDesc, sutImage, sutPort, null);
		}
	}

	@Test
	@DisplayName("Create WebApp project Chrome Test")
	void testCreateChromeTest(@DockerBrowser(type = CHROME) RemoteWebDriver driver) throws InterruptedException {
		this.driver = driver;

		this.createProjectAndSut(driver);

		navigateToProject(driver, projectName);

		String tJobName = "Chrome Test";
		String tJobTestResultPath = "/demo-projects/web-java-test/target/surefire-reports/";
		String tJobImage = "elastest/test-etm-alpinegitjava";
		String commands = "git clone https://github.com/elastest/demo-projects; cd demo-projects/web-java-test; mvn -Dtest=MultipleWebAppTests -B -Dbrowser=chrome test;";
		List<String> tssList = new ArrayList<>();
		tssList.add("EUS");

		createNewTJob(driver, tJobName, tJobTestResultPath, sutName, tJobImage, false, commands, null, tssList);

		// Run TJob
		runTJobFromProjectPage(driver, tJobName);

		log.info("Wait for build sucess traces");
		WebDriverWait waitLogs = new WebDriverWait(driver, timeout);
		waitLogs.until(textToBePresentInElementLocated(By.tagName("logs-view"), "BUILD FAILURE"));
	}

	@Test
	@DisplayName("Create WebApp project Chrome Test")
	void testCreateFirefoxTest(@DockerBrowser(type = CHROME) RemoteWebDriver driver) throws InterruptedException {
		this.driver = driver;

		this.createProjectAndSut(driver);

		navigateToProject(driver, projectName);

		String tJobName = "Firefox Test";
		String tJobTestResultPath = "/demo-projects/web-java-test/target/surefire-reports/";
		String tJobImage = "elastest/test-etm-alpinegitjava";
		String commands = "git clone https://github.com/elastest/demo-projects; cd demo-projects/web-java-test; mvn -Dtest=MultipleWebAppTests -B -Dbrowser=firefox test;";
		List<String> tssList = new ArrayList<>();
		tssList.add("EUS");

		createNewTJob(driver, tJobName, tJobTestResultPath, sutName, tJobImage, false, commands, null, tssList);

		// Run TJob
		runTJobFromProjectPage(driver, tJobName);

		log.info("Wait for build sucess traces");
		WebDriverWait waitLogs = new WebDriverWait(driver, timeout);
		waitLogs.until(textToBePresentInElementLocated(By.tagName("logs-view"), "BUILD FAILURE"));
	}

}
