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
import static java.util.logging.Level.ALL;
import static org.openqa.selenium.logging.LogType.BROWSER;
import static org.openqa.selenium.remote.CapabilityType.LOGGING_PREFS;
import static org.openqa.selenium.remote.DesiredCapabilities.chrome;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;

import io.github.bonigarcia.DriverCapabilities;

/**
 * Parent for E2E EUS tests.
 *
 * @author Boni Garcia (boni.garcia@urjc.es)
 * @since 0.1.1
 */
public class EtmBaseTest {

	final Logger log = getLogger(lookup().lookupClass());

	protected String tormUrl = "http://172.17.0.1:37000/"; // local by default
	protected String secureTorm = "http://user:pass@localhost:37000/";

	protected String eUser = null;
	protected String ePassword = null;

	protected boolean secureElastest = false;

	protected WebDriver driver;

	@DriverCapabilities
	DesiredCapabilities capabilities = chrome();
	{
		LoggingPreferences logPrefs = new LoggingPreferences();
		logPrefs.enable(BROWSER, ALL);
		capabilities.setCapability(LOGGING_PREFS, logPrefs);
	}

	@BeforeEach
	void setup() {
		String etmApi = getProperty("etEmpApi");
		if (etmApi != null) {
			tormUrl = etmApi;
		}
		String elastestUser = getProperty("eUser");
		if (elastestUser != null) {
			eUser = elastestUser;

			String elastestPassword = getProperty("ePass");
			if (elastestPassword != null) {
				ePassword = elastestPassword;
				secureElastest = true;
			}

		}
		if (secureElastest) {
			String split_url[] = tormUrl.split("//");
			secureTorm = split_url[0] + "//" + eUser + ":" + ePassword + "@" + split_url[1];
		}

		log.info("Using URL {} to connect to {} TORM", tormUrl, secureElastest ? "secure" : "unsecure");
	}

	@AfterEach
	void teardown() throws IOException {
		if (driver != null) {
			log.info("Browser console at the end of the test");
			LogEntries logEntries = driver.manage().logs().get(BROWSER);
			logEntries.forEach((entry) -> log.info("[{}] {} {}", new Date(entry.getTimestamp()), entry.getLevel(),
					entry.getMessage()));
		}
	}

	protected void createNewProject(WebDriver driver, String projectName) {
		driver.findElement(By.xpath("//button[contains(string(), 'New Project')]")).click();
		driver.findElement(By.name("project.name")).sendKeys(projectName);
		driver.findElement(By.xpath("//button[contains(string(), 'SAVE')]")).click();
	}
	
	protected void removeProject(WebDriver driver, String projectName) {
		//TODO
	}

	protected void createNewTJobWithCommands(WebDriver driver, String tJobName, String testResultPath, String sutName,
			String dockerImage, String commands, Map<String, String> parameters, List<String> tss) {
		driver.findElement(By.xpath("//button[contains(string(), 'New TJob')]")).click();

		driver.findElement(By.name("tJobName")).sendKeys(tJobName);

		if (testResultPath != null) {
			driver.findElement(By.name("resultsPath")).sendKeys(testResultPath);
		}

		// Select SuT
		driver.findElement(By.xpath("//md-select/div/span[contains(string(), 'Select a SuT')]")).click();
		if (sutName != null) {
			driver.findElement(By.xpath("//md-option[contains(string(), '" + sutName + "')]")).click();
		} else {
			driver.findElement(By.xpath("//md-option[contains(string(), 'None')]")).click();

		}

		// Image and commands
		driver.findElement(By.name("tJobImageName")).sendKeys(dockerImage);
		driver.findElement(By.name("commands")).sendKeys(commands);

		// Parameters

		// TSS

		// Save
		driver.findElement(By.xpath("//button[contains(string(), 'SAVE')]")).click();
	}

	// protected void createNewTJobWithDockerImageCommands(WebDriver driver,
	// String tJobName, String testResultPath, ) {
	//
	// }

	protected void startTestSupportService(WebDriver driver, String supportServiceLabel) {
		WebElement tssNavButton = driver.findElement(By.id("nav_support_services"));
		if (!tssNavButton.isDisplayed()) {
			driver.findElement(By.id("main_menu")).click();
		}
		tssNavButton.click();

		WebDriverWait waitElement = new WebDriverWait(driver, 3);
		By supportService;
		int numRetries = 1;
		do {
			driver.findElement(By.className("mat-select-trigger")).click();
			supportService = By.xpath("//md-option[contains(string(), '" + supportServiceLabel + "')]");
			try {
				waitElement.until(visibilityOfElementLocated(supportService));
				log.info("Element {} already available", supportService);
				break;

			} catch (Exception e) {
				numRetries++;
				if (numRetries > 6) {
					log.warn("Max retries ({}) reached ... leaving", numRetries);
					break;
				}
				log.warn("Element {} not available ... retrying", supportService);
			}
		} while (true);
		driver.findElement(supportService).click();

		log.info("Create and wait instance");
		driver.findElement(By.id("create_instance")).click();
		WebDriverWait waitService = new WebDriverWait(driver, 120); // seconds
		By serviceDetailButton = By.xpath("//button[@title='View Service Detail']");
		waitService.until(visibilityOfElementLocated(serviceDetailButton));
		driver.findElement(serviceDetailButton).click();
	}

}
