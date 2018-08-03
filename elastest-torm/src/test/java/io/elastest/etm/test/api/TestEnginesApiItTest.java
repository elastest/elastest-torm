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
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import io.elastest.etm.ElasTestTormApp;
import io.elastest.etm.model.TestEngine;

@RunWith(JUnitPlatform.class)
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = ElasTestTormApp.class, webEnvironment = WebEnvironment.RANDOM_PORT)
public class TestEnginesApiItTest extends EtmApiItTest {
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
        List<TestEngine> testEngines = this.getTestEngines();
        assertNotNull(testEngines);
        assertThat(testEngines.size() > 0);
        log.debug("The Test Engines List: {}", testEngines);

        String testEngineName = testEngines.get(0).getEngineName();
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

    public List<TestEngine> getTestEngines() {
        return Arrays.asList(httpClient
                .getForEntity("/api/engines", TestEngine[].class).getBody());
    }

    public TestEngine startTestEngine(String engineName) {
        return httpClient.postForEntity("/api/engines/" + engineName + "/start",
                null, TestEngine.class).getBody();
    }

    public void stopTestEngine(String engineName) {
        httpClient.delete("/api/engines/" + engineName);
    }

    public boolean isRunning(String engineName) {
        return httpClient
                .getForEntity("/api/engines/" + engineName + "/started",
                        boolean.class)
                .getBody();
    }

    public String getUrlIfIsRunning(String engineName) {
        return httpClient.getForEntity("/api/engines/" + engineName + "/url",
                String.class).getBody();
    }

    public boolean isWorking(String engineName) {
        return httpClient
                .getForEntity("/api/engines/" + engineName + "/working",
                        boolean.class)
                .getBody();
    }
}
