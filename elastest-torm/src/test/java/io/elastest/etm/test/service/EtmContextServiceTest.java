package io.elastest.etm.test.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import io.elastest.etm.ElasTestTormApp;
import io.elastest.etm.service.EtmContextService;

@RunWith(JUnitPlatform.class)
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = ElasTestTormApp.class, webEnvironment = WebEnvironment.RANDOM_PORT)
public class EtmContextServiceTest {

    @Autowired
    private EtmContextService etmContextService;

    @Test
    public void getContextInfoTest() {
        assertThat(etmContextService).isNotNull();
        assertNotNull(etmContextService.getContextInfo());
    }

    @Test
    public void getHelpInfoTest() {
        assertThat(etmContextService).isNotNull();
        assertNotNull(etmContextService.getHelpInfo());
    }
}
