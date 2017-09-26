package io.elastest.etm.test.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import io.elastest.etm.ElasTestTormApp;
import io.elastest.etm.model.SupportService;
import io.elastest.etm.model.SupportServiceInstance;
import io.elastest.etm.service.EsmService;

@RunWith(JUnitPlatform.class)
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = ElasTestTormApp.class, webEnvironment = WebEnvironment.RANDOM_PORT)
public class EsmServiceItTest {
	
	private static final Logger logger = LoggerFactory.getLogger(EsmService.class);
	
	@Autowired
	private EsmService esmService;
	
	@Test
	@Disabled
	public void provisionServiceTest(){
		
		assertThat(esmService).isNotNull();
		assertThat(esmService.getRegisteredServices()).isNotEmpty();
		
		SupportService nginx = null;
		
		for(SupportService s : esmService.getRegisteredServices()){
			if(s.getName().equals("nginx-service")){
				nginx = s;
				break;
			}
		}
		
		assertThat(nginx).isNotNull();
		
		SupportServiceInstance instance = esmService.provisionServiceInstance(nginx.getId(), false);
		
		logger.info("Endpoints: {}",instance.getEndpointsData());
		
	}

}
