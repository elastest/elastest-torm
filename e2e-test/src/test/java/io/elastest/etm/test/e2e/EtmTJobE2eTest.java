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
package io.elastest.etm.test.e2e;

import static io.github.bonigarcia.BrowserType.CHROME;
import static java.lang.invoke.MethodHandles.lookup;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.openqa.selenium.support.ui.ExpectedConditions.textToBePresentInElementLocated;
import static org.slf4j.LoggerFactory.getLogger;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
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
@DisplayName("E2E tests of ETM")
@ExtendWith(SeleniumExtension.class)
public class EtmTJobE2eTest extends EtmBaseTest {

	final Logger log = getLogger(lookup().lookupClass());

	@Test
	@DisplayName("Create Unit Test")
	void testCreateUnitTest(@DockerBrowser(type = CHROME) RemoteWebDriver driver) throws InterruptedException {
		this.driver = driver;

		log.info("Navigate to TORM and start new project");
		driver.manage().window().setSize(new Dimension(1024, 1024));
		driver.manage().timeouts().implicitlyWait(5, SECONDS);
		if (secureElastest) {
			driver.get(secureTorm);
		} else {
			driver.get(tormUrl);
		}
		createNewProject(driver, "Unit Tests");

		log.info("Create new TJob");
		String tJobName = "Unit Test";
		String tJobTestResultPath = "/demo-projects/unit-java-test/target/surefire-reports/";
		String sutName = null;
		String tJobImage = "elastest/test-etm-alpinegitjava";
		String commands = "git clone https://github.com/elastest/demo-projects; cd demo-projects/unit-java-test; mvn -B test;";

		createNewTJobWithCommands(driver, tJobName, tJobTestResultPath, sutName, tJobImage, commands, null, null);

		log.info("Run TJob");
		driver.findElement(By.xpath("//button[@title='Run TJob']")).click();

		log.info("Wait for build sucess traces");
		WebDriverWait waitLogs = new WebDriverWait(driver, 180);
		waitLogs.until(textToBePresentInElementLocated(By.tagName("logs-view"), "BUILD FAILURE"));
	}

}
