package io.elastest.etm.test.tjob;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import io.elastest.etm.api.model.TJob;
import io.elastest.etm.dao.TJobRepository;
import io.elastest.etm.service.tjob.TJobService;
import io.elastest.etm.test.extensions.MockitoExtension;


@RunWith(JUnitPlatform.class)
@SpringBootTest
@ExtendWith({SpringExtension.class, MockitoExtension.class})
public class TjobAPITest {
	
//	@Mock TJobRepository tJobRepository;
//	
//	@InjectMocks
//	private TJobService tJobService;

//	@BeforeEach
//	void setUp(@Autowired TJobService tJobService ){
//		this.tJobService = tJobService;
//	}
	
	@Test
	public void createTJobTest(@Autowired TJob tJob, @Autowired TJobService tJobService, @Mock TJobRepository tJobRepository){
		//TJob createdTJob = 
		when(tJobRepository.save(tJob)).thenReturn(tJob);
		tJobService.settJobRepo(tJobRepository);
		tJob = tJobService.createTJob(tJob);
		System.out.println("ImageName:"+tJob.getImageName());
		assertNotNull(tJob.getId());
	}
	
	
}
