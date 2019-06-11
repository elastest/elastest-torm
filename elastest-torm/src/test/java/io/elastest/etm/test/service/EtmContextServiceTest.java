package io.elastest.etm.test.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.util.ReflectionTestUtils;

import com.spotify.docker.client.messages.ImageInfo;

import io.elastest.epm.client.service.DockerService;
import io.elastest.etm.model.ContextInfo;
import io.elastest.etm.model.SupportServiceInstance;
import io.elastest.etm.platform.service.DockerServiceImpl;
import io.elastest.etm.service.TSSService;
import io.elastest.etm.service.EtmContextAuxService;
import io.elastest.etm.service.EtmContextService;
import io.elastest.etm.test.extensions.MockitoExtension;
import io.elastest.etm.utils.UtilsService;

@Tag("Unit test")
@DisplayName("EtmContextService Unit tests")
@ExtendWith({ MockitoExtension.class })
public class EtmContextServiceTest {

    @InjectMocks
    public EtmContextService etmContextService;

    @Mock
    public EtmContextAuxService etmContextAuxService;
    @Mock
    public DockerServiceImpl dockerServiceImpl;
    @Mock
    public TSSService esmService;
    @Mock
    public UtilsService utilService;

    @BeforeEach
    public void setUp() {
        ReflectionTestUtils.setField(etmContextService, "etImages",
                "elastest/etm");
        ReflectionTestUtils.setField(etmContextAuxService, "etInProd", true);
    }

    @Test
    public void getContextInfoTest() {
        ContextInfo contextInfo = new ContextInfo();
        contextInfo.setEusSSInstance(new SupportServiceInstance());
        when(etmContextAuxService.getContextInfo()).thenReturn(contextInfo);
        when(utilService.isElastestMini()).thenReturn(true);
        etmContextService.createContextInfo();
        assertThat(etmContextService).isNotNull();
        assertNotNull(etmContextService.getContextInfo());
    }

    @Test
    public void getHelpInfoTest() throws Exception {
        ImageInfo imageInfo = mock(ImageInfo.class);
        DockerService ds = spy(DockerService.class);
        doReturn(imageInfo).when(ds).getImageInfoByName(any(String.class));
        assertThat(etmContextService).isNotNull();
        assertNotNull(etmContextService.getHelpInfo());
    }

}
