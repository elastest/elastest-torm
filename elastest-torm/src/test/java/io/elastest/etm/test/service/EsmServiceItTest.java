package io.elastest.etm.test.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import io.elastest.etm.ElasTestTormApp;
import io.elastest.etm.model.SupportService;
import io.elastest.etm.service.EsmService;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = ElasTestTormApp.class, webEnvironment = WebEnvironment.RANDOM_PORT)
@TestInstance(Lifecycle.PER_CLASS)
public class EsmServiceItTest {
	
	private static final Logger logger = LoggerFactory.getLogger(EsmService.class);
	
	@Autowired
	private EsmService esmService;
	
	@AfterAll
	public void cleanEnvironment() {
	    esmService.deprovisionServicesInstances();
	}
	
	@Test
	@Disabled
	public void provisionServiceTest(){
		
		assertThat(esmService.getRegisteredServices()).isNotEmpty();
		SupportService eus = null;
		
		for(SupportService s : esmService.getRegisteredServices()){
			if(s.getName().equals("EUS")){
				eus = s;
				break;
			}
		}
		
		String tssInstanceId = esmService.provisionServiceInstanceSync(eus.getId());
		assertThat(tssInstanceId != null && !tssInstanceId.isEmpty());
	}

}
