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

import io.elastest.etm.model.EsmServiceModel;
import io.elastest.etm.service.EsmService;
import io.elastest.etm.utils.ElastestConstants;

@Controller
public class EsmApiController implements EsmApi {
	
	private static final Logger logger = LoggerFactory.getLogger(EsmApiController.class);

	@Autowired EsmService esmService;
	
	@Value("${elastest.execution.mode}")
	public String ELASTEST_EXECUTION_MODE;	
	
	@Override
	public ResponseEntity<List<String>> getElastestServicesNames() {
		List<String> servicesList = esmService.getRegisteredServicesName();
		return new ResponseEntity<List<String>>(servicesList, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<List<EsmServiceModel>> getElastestServices() {		
		if (ELASTEST_EXECUTION_MODE.equals(ElastestConstants.MODE_NORMAL)){
			List<EsmServiceModel> servicesList = esmService.getRegisteredServices();
			return new ResponseEntity<List<EsmServiceModel>>(servicesList, HttpStatus.OK);
		}else{
			return new ResponseEntity<List<EsmServiceModel>>(new ArrayList<>(), HttpStatus.OK);
		}
	}

	@Override
	public ResponseEntity<String> provisionServiceInstance(String service_id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ResponseEntity<String> deprovisionServiceInstance(String instance_id) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
}
