package io.elastest.etm.test.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import io.elastest.etm.model.SupportService;
import io.elastest.etm.service.TSSService;
import io.elastest.etm.test.IntegrationBaseTest;

@TestInstance(Lifecycle.PER_CLASS)
public class EsmServiceItTest extends IntegrationBaseTest {
	
	private static final Logger logger = LoggerFactory.getLogger(TSSService.class);
	
	@Autowired
	private TSSService esmService;
	
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
