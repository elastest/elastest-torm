package io.elastest.etm.test.api;

import static java.lang.invoke.MethodHandles.lookup;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;

import org.junit.jupiter.api.Disabled;
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
import io.elastest.etm.test.util.Waiter;
import io.elastest.etm.test.util.Waiter.TimedOut;

@RunWith(JUnitPlatform.class)
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = ElasTestTormApp.class, webEnvironment = WebEnvironment.RANDOM_PORT)
public class TestEnginesApiItTest {
    final Logger log = getLogger(lookup().lookupClass());

    @Autowired
    TestRestTemplate httpClient;

    @Test
    @Disabled
    public void TestEnginesTest() throws TimedOut, Exception {
        List<String> testEngines = this.getTestEngines();
        assertNotNull(testEngines);
        assertThat(testEngines.size() > 0);

        String testEngineName = testEngines.get(0);
        this.startTestEngine(testEngineName);
        assertTrue(this.isRunning(testEngineName));

        Waiter waiter = new Waiter(180000) {
            @Override
            public boolean until() throws Exception {
                return isWorking(testEngineName);
            }
        };

        waiter.waitUntil();

        String url = this.getUrlIfIsRunning(testEngineName);
        assertNotNull(url);

        this.stopTestEngine(testEngineName);
        assertFalse(this.isRunning(testEngineName));
    }

    @SuppressWarnings("unchecked")
    public List<String> getTestEngines() {
        return httpClient.getForEntity("/api/engines", List.class).getBody();
    }

    public String startTestEngine(String engineName) {
        return httpClient
                .postForEntity("/api/engines", engineName, String.class)
                .getBody();
    }

    public void stopTestEngine(String engineName) {
        httpClient.delete("/api/engines/" + engineName);
    }

    public boolean isRunning(String engineName) {
        return httpClient.getForEntity("/api/engines/" + engineName + "/started",
                boolean.class).getBody();
    }

    public String getUrlIfIsRunning(String engineName) {
        return httpClient
                .getForEntity("/api/engines/" + engineName + "/url", String.class)
                .getBody();
    }

    public boolean isWorking(String engineName) {
        return httpClient.getForEntity("/api/engines/" + engineName + "/working",
                boolean.class).getBody();
    }
}
