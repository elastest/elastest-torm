package io.elastest.etm.test.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.util.ReflectionTestUtils;

import io.elastest.etm.service.DockerService2;
import io.elastest.etm.service.EsmService;
import io.elastest.etm.service.EtmContextAuxService;
import io.elastest.etm.service.EtmContextService;
import io.elastest.etm.test.extensions.MockitoExtension;

@RunWith(JUnitPlatform.class)
@ExtendWith({ MockitoExtension.class })
public class EtmContextServiceTest {

    @InjectMocks
    public EtmContextService etmContextService;

    @Mock
    public EtmContextAuxService etmContextAuxService;
    @Mock
    public DockerService2 dockerService;
    @Mock
    public EsmService esmService;

    @BeforeEach
    public void setUp() {
        ReflectionTestUtils.setField(etmContextService, "etImages",
                "elastest/etm");
        ReflectionTestUtils.setField(etmContextAuxService, "etInProd", true);
    }

    @Test
    @Disabled
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
