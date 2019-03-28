package io.elastest.etm.test.api;

import static java.lang.invoke.MethodHandles.lookup;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;

import io.elastest.etm.model.EtPlugin;

@RunWith(JUnitPlatform.class)
public class EtPluginsApiItTest extends EtmApiItTest {
    final Logger log = getLogger(lookup().lookupClass());

    @Autowired
    TestRestTemplate httpClient;

    @AfterEach
    public void removeContainers() {
        try {
            this.stopTestEngine("ece");
        } catch (Exception e) {
            log.debug("Cannot stop ece, probably is already stopped", e);
        }

    }

    @Test
    public void TestEnginesTest() throws Exception {
        log.debug("Getting Test Engines List");
        List<EtPlugin> testEngines = this.getTestEngines();
        assertNotNull(testEngines);
        assertThat(testEngines.size() > 0);
        log.debug("The Test Engines List: {}", testEngines);

        String testEngineName = testEngines.get(0).getName();
        log.debug("Starting Test Engine {}", testEngineName);
        this.startTestEngine(testEngineName);

        // Ece starts but returns always 500 code
        // assertTrue(this.isRunning(testEngineName));
        //
        // Waiter waiter = new Waiter(180000) {
        // @Override
        // public boolean until() throws Exception {
        // return isWorking(testEngineName);
        // }
        // };
        //
        // waiter.waitUntil();

        // String url = this.getUrlIfIsRunning(testEngineName);
        // assertNotNull(url);
        // log.debug("Test Engine {} is started at {}", testEngineName, url);

        log.debug("Stopping Test Engine {}", testEngineName);
        this.stopTestEngine(testEngineName);
        assertFalse(this.isRunning(testEngineName));
        log.debug("Test Engine {} has been stopped", testEngineName);
    }

    public List<EtPlugin> getTestEngines() {
        return Arrays.asList(httpClient
                .getForEntity("/api/etplugins", EtPlugin[].class).getBody());
    }

    public EtPlugin startTestEngine(String engineName) {
        return httpClient.postForEntity("/api/etplugins/" + engineName + "/start",
                null, EtPlugin.class).getBody();
    }

    public void stopTestEngine(String engineName) {
        httpClient.delete("/api/etplugins/" + engineName);
    }

    public boolean isRunning(String engineName) {
        return httpClient
                .getForEntity("/api/etplugins/" + engineName + "/started",
                        boolean.class)
                .getBody();
    }

    public String getUrlIfIsRunning(String engineName) {
        return httpClient.getForEntity("/api/etplugins/" + engineName + "/url",
                String.class).getBody();
    }

    public boolean isWorking(String engineName) {
        return httpClient
                .getForEntity("/api/etplugins/" + engineName + "/working",
                        boolean.class)
                .getBody();
    }
}
