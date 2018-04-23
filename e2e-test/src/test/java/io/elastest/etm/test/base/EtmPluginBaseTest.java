package io.elastest.etm.test.base;

import static java.lang.System.getProperty;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class EtmPluginBaseTest extends EtmBaseTest {
    
    protected String jenkinsPluginManagerAd = "/pluginManager/advanced";
    protected String jenkinsCIUrl = "http://172.17.0.1:8080";
    // protected String pluginPath =
    // "/home/frdiaz/git/elastest/elastest-jenkins/target/elastest.hpi";
    protected String pluginOriginPath = "/home/ubuntu/workspace/elastest-torm/e2e-test-with-plugin/elastest-plugin/target/elastest.hpi";
    protected String pluginTargetPath = "/home/ubuntu/workspace/elastest-torm/e2e-test-with-plugin/target/surefire-reports/io.elastest.etm.test.e2e.plugin.ElasTestPluginE2ETest/elastest.hpi";
//    protected String pluginOriginPath = "/home/frdiaz/git/elastest/elastest-jenkins/target/elastest.hpi";
//    protected String pluginTargetPath = "/home/frdiaz/git/elastest/elastest-torm/e2e-test/target/surefire-reports/io.elastest.etm.test.e2e.plugin.ElasTestPluginE2ETest/elastest.hpi";
    protected String pluginPath = "/opt/selenoid/video";
    // protected String pluginPath = "e2e-test-with-plugin/target/elastest.hpi";
    protected String pluginSettings = "/configureTools/";
    
    @BeforeEach
    void pluginSetup() {        
        String jenkinsCIUrl = getProperty("ciUrl");
        if (jenkinsCIUrl != null) {
            this.jenkinsCIUrl = jenkinsCIUrl;
        }

        String pluginPath = getProperty("ePluginPath");
        if (pluginPath != null) {
            this.pluginPath = pluginPath;
        }

        jenkinsPluginManagerAd = this.jenkinsCIUrl + jenkinsPluginManagerAd;
        pluginSettings = this.jenkinsCIUrl + pluginSettings;
    }
    
    protected void installElasTestPlugin(WebDriver webDriver) throws IOException {
        WebDriverWait waitService = new WebDriverWait(driver, 60);

        // Copy hpi file to the folder accessible by the Browser
        Path sourcePathFile = Paths.get(pluginOriginPath);
        Path targetPathFile = Paths.get(pluginTargetPath);        
        Files.copy(sourcePathFile, targetPathFile);

        // Install plugin
        log.info("Installing plugin");
        By inputFileName = By.name("name");
        webDriver.findElement(inputFileName).sendKeys(pluginPath);
        By uploadButton = By.xpath("//button[contains(string(), 'Upload')]");
        webDriver.findElement(uploadButton).click();

        // Check the plugin installation is ok
        log.info("Checking installation status");
        By installationStatus = By
                .xpath("//table/tbody/tr/td[contains(string(), 'Success')]");
        waitService.until(visibilityOfElementLocated(installationStatus));
        log.info("Plugin installation finished");

        log.info("Navigate to main page");
        By homeLink = By.linkText("Jenkins");
        webDriver.findElement(homeLink).click();
    }

    protected void pluginConfiguration(WebDriver driver) {
        WebDriverWait waitService = new WebDriverWait(driver, 10);

        // Fill configuration
        log.info("Filling the configuration");
        driver.findElement(By.name("_.elasTestUrl")).clear();
        driver.findElement(By.name("_.elasTestUrl"))
                .sendKeys(secureElastest ? secureTorm : tormUrl);
        if (eUser != null && ePassword != null) {
            driver.findElement(By.name("_.username")).sendKeys(eUser);
            driver.findElement(By.name("_.password")).sendKeys(ePassword);
        }

        // Test connection
        driver.findElement(
                By.xpath("//button[contains(string(), 'Test Connection')]"))
                .click();
        By testConnectionResult = By
                .xpath("//div[contains(string(), 'Success')]");
        waitService.until(visibilityOfElementLocated(testConnectionResult));
        log.info("Successfull conection");

        driver.findElement(By.xpath("//button[contains(string(), 'Save')]"))
                .click();
    }

    protected void createFreestyleJob(WebDriver driver, String jobName) {
        WebDriverWait waitService = new WebDriverWait(driver, 60);

        log.info("Creating a Freestyle Job");
        driver.findElement(By.id("name")).sendKeys(jobName);

        log.info("Select the Job's type");
        driver.findElement(
                By.xpath("//li[contains(string(), 'Freestyle project')]"))
                .click();
        driver.findElement(By.id("ok-button")).click();

        driver.findElement(By.xpath(
                "//input[@type='radio'][following-sibling::text()[position()=1][contains(string(), 'Git')]]"))
                .click();
        driver.findElement(By.xpath(
                "//*[@id=\"main-panel\"]/div/div/div/form/table/tbody/tr[133]/td[3]/div/div[1]/table/tbody/tr[1]/td[3]/input"))
                .sendKeys("https://github.com/elastest/demo-projects.git");

        WebElement myelement = driver.findElement(By.xpath(
                "//div/span/span/button[contains(string(), 'Add build step')]"));
        JavascriptExecutor jse2 = (JavascriptExecutor) driver;
        jse2.executeScript("arguments[0].scrollIntoView()", myelement);

        By byAddStepButton = By.xpath(
                "//div/span/span/button[contains(string(), 'Add build step')]");
        waitService.until(
                ExpectedConditions.elementToBeClickable(byAddStepButton));
        driver.findElement(byAddStepButton).click();

        By byItemShell = By
                .xpath("//ul/li/a[contains(string(), 'Execute shell')]");
        waitService.until(visibilityOfElementLocated(byItemShell));
        driver.findElement(byItemShell).click();

        // By byTextArea = By.xpath("//textarea[@name='command']");
        // waitService.until(visibilityOfElementLocated(byTextArea));
        // driver.findElements(By.xpath("//textarea[@name='command']")).get(0).sendKeys("cd
        // unit-java-test; mvn test");
        driver.findElement(By.xpath("//button[contains(string(), 'Save')]"))
                .click();
        driver.findElement(By.linkText("Configure")).click();

        // WebElement myelement2 =
        // driver.findElement(byDom("document.getElementsByName(\"command\")[0]"));
        // JavascriptExecutor jse22 = (JavascriptExecutor)driver;
        // jse2.executeScript("arguments[0].scrollIntoView()", myelement2);

        // By byTextArea = By.xpath("//textarea[contains(@name, 'command')]");
        // By byTextArea = By.xpath("//*[contains(@id,
        // 'yui-gen')]/table/tbody/tr[3]/td[3]/textarea[contains(@name,
        // 'command')]");
        // waitService.until(visibilityOfElementLocated(byDom("document.getElementsByName(\"command\")[0]")));
        // driver.findElement(byTextArea).sendKeys("cd unit-java-test; mvn
        // test");
        driver.findElement(By.xpath(
                "//*[contains(@id, 'yui-gen')]/table/tbody/tr[3]/td[3]/textarea"))
                .sendKeys("cd unit-java-test; mvn test");
        driver.findElement(By.xpath("//button[contains(string(), 'Save')]"))
                .click();

    }

    protected void createPipelineJob(WebDriver driver, String jobName,
            String script) {
        log.info("Creating a Freestyle Job");
        driver.findElement(By.id("name")).sendKeys(jobName);

        log.info("Select the Job's type");
        driver.findElement(By.xpath("//li[contains(string(), 'Pipeline')]"))
                .click();
        driver.findElement(By.id("ok-button")).click();

        driver.findElement(By.xpath("//*[@id=\"workflow-editor-1\"]/textarea"))
                .sendKeys(script);

        driver.findElement(By.xpath("//button[contains(string(), 'Save')]"))
                .click();
    }

}
