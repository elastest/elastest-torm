package io.elastest.etm.api;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import com.fasterxml.jackson.annotation.JsonView;

import io.elastest.etm.model.SupportService;
import io.elastest.etm.model.SupportServiceInstance;
import io.elastest.etm.model.SupportServiceInstance.FrontView;
import io.elastest.etm.service.EsmService;
import io.elastest.etm.utils.ElastestConstants;

@Controller
public class EsmApiController implements EsmApi {
	
	private static final Logger logger = LoggerFactory.getLogger(EsmApiController.class);

	@Autowired EsmService esmService;
	
	@Value("${elastest.execution.mode}")
	public String ELASTEST_EXECUTION_MODE;	
	
	@Override
	public ResponseEntity<List<String>> getSupportServicesNames() {
		List<String> servicesList = esmService.getRegisteredServicesName();
		return new ResponseEntity<List<String>>(servicesList, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<List<SupportService>> getSupportServices() {		
		if (ELASTEST_EXECUTION_MODE.equals(ElastestConstants.MODE_NORMAL)){
			List<SupportService> servicesList = esmService.getRegisteredServices();
			return new ResponseEntity<List<SupportService>>(servicesList, HttpStatus.OK);
		}else{
			return new ResponseEntity<List<SupportService>>(new ArrayList<>(), HttpStatus.OK);
		}
	}

	@Override
	@JsonView(FrontView.class)
	public ResponseEntity<List<SupportServiceInstance>> getSupportServicesInstances() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@JsonView(FrontView.class)
	public ResponseEntity<SupportServiceInstance> provisionServiceInstance(String service_id) {		
		return new ResponseEntity<SupportServiceInstance>(esmService.provisionServiceInstance(service_id), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<String> deprovisionServiceInstance(String instance_id) {
		return new ResponseEntity<String>(esmService.deprovisionServiceInstance(instance_id), HttpStatus.OK);
	}
	
	
	
	
}
