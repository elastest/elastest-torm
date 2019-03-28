package io.elastest.etm.test.service;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import io.elastest.etm.service.LogstashService;
import io.elastest.etm.test.IntegrationBaseTest;

@RunWith(JUnitPlatform.class)
public class LogstashServiceItTest extends IntegrationBaseTest {
    final Logger log = getLogger(lookup().lookupClass());

    @Autowired
    public LogstashService logstashService;

    @Test
    public void sendTestLogTracesTest() {
        String exec = "3718";
        String testName = "SampleTestName";
        log.debug("Sending start test log trace to {} for test '{}'", testName,
                exec);
        this.logstashService.sendStartTestLogtrace(exec, testName);
        log.debug("Sending finish test log trace to {} for test '{}'", testName,
                exec);
        this.logstashService.sendFinishTestLogtrace(exec, testName);
    }

}
