package io.elastest.etm.test.e2e.plugin;

import static java.lang.invoke.MethodHandles.lookup;
import static org.openqa.selenium.support.ui.ExpectedConditions.textToBePresentInElementLocated;
import static org.slf4j.LoggerFactory.getLogger;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
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
@Tag("ETinET")
@DisplayName("EJ_E2E_Test_With-plugin-installation")
@TestInstance(Lifecycle.PER_CLASS)
@ExtendWith(SeleniumExtension.class)
public class EJWhitInstallEtInEtTest extends EtmPluginBaseTest {
    final Logger log = getLogger(lookup().lookupClass());

    final String unitTestScript = "node{\n" + "    elastest(tss: ['EUS']) {\n"
            + "        stage ('Executing Test') {\n"
            + "            echo 'Print env variables'\n"
            + "            sh 'env'\n" + "            mvnHome = tool 'M3.3.9'\n"
            + "            echo 'Cloning repository'\n"
            + "            git 'https://github.com/elastest/demo-projects'\n"
            + "            echo 'Run test'\n"
            + "            sh \"cd ./unit/junit5-unit-test;'${mvnHome}/bin/mvn' -DforkCount=0 test\"\n";// +

    final String jobName = "PJob_1";

    @Test
    @DisplayName("Pipeline plugin")
    void testPipelineJob(ChromeDriver localDriver, TestInfo testInfo) throws Exception {
        setupTestBrowser(testInfo.getTestMethod().get().getName(),
                BrowserType.CHROME,
                localDriver);
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
        WebElement logsView = driver.findElement(By.xpath("//logs-view"));
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
