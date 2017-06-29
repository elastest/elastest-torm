package io.elastest.etm.test.tjob;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import io.elastest.etm.api.model.TJob;
import io.elastest.etm.dao.TJobExecRepository;
import io.elastest.etm.dao.TJobRepository;
import io.elastest.etm.service.epm.EpmIntegrationService;
import io.elastest.etm.service.tjob.TJobService;
import io.elastest.etm.test.extensions.MockitoExtension;
	


@RunWith(JUnitPlatform.class)
@SpringBootTest(classes=ElastestConfigTest.class)
@ExtendWith({SpringExtension.class, MockitoExtension.class})
public class TJobServiceTest {
	
//	@Mock TJobRepository tJobRepository;
//	
//	@InjectMocks
//	private TJobService tJobService;

//	@BeforeEach
//	void setUp(@Autowired TJobService tJobService ){
//		this.tJobService = tJobService;
//	}
	
	@Test
	public void createTJobTest(@Autowired TJob tJob  
			, @Mock TJobRepository tJobRepo, @Mock EpmIntegrationService epmIntegrationService, @Mock TJobExecRepository tJobExecRepo){
		//TJob createdTJob = 
		when(tJobRepo.save(tJob)).thenReturn(tJob);
		TJobService tJobService = new TJobService(tJobRepo, tJobExecRepo, epmIntegrationService);		
		TJob tJob1 = tJobService.createTJob(tJob);
		System.out.println("ImageName:"+tJob1.getImageName());
		assertNotNull(tJob1.getId());		
        assertAll("Validating Project Properties",
                () -> assertTrue(tJob1.getName().equals("SimpleTest")),
                () -> assertTrue(tJob1.getProject().getName().equals("TestProject1"))
        );
		
	}
	
	
}
