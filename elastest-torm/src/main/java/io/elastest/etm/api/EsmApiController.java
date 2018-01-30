package io.elastest.etm.api;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;

import com.fasterxml.jackson.annotation.JsonView;

import io.elastest.etm.model.SupportService;
import io.elastest.etm.model.SupportServiceInstance;
import io.elastest.etm.model.SupportServiceInstance.FrontView;
import io.elastest.etm.service.EsmService;
import io.elastest.etm.utils.UtilTools;
import io.swagger.annotations.ApiParam;

@Controller
public class EsmApiController implements EsmApi {

	private static final Logger logger = LoggerFactory.getLogger(EsmApiController.class);

	@Autowired
	EsmService esmService;
	
	@Autowired
	UtilTools utilTools;

	@Override
	public ResponseEntity<List<String>> getSupportServicesNames() {
		List<String> servicesList = esmService.getRegisteredServicesName();
		return new ResponseEntity<List<String>>(servicesList, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<List<SupportService>> getSupportServices() {
		List<SupportService> servicesList = esmService.getRegisteredServices();
		return new ResponseEntity<List<SupportService>>(servicesList, HttpStatus.OK);
	}

	@Override
	@JsonView(FrontView.class)
	public ResponseEntity<List<SupportServiceInstance>> getSupportServicesInstances() {
		return new ResponseEntity<List<SupportServiceInstance>>(esmService.getServicesInstancesAsList(), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<String> provisionServiceInstance(
			@ApiParam(value = "Service Id", required = true) @PathVariable("serviceId") String serviceId) {
		logger.info("Service provision:" + serviceId);
		String instanceId = utilTools.generateUniqueId();
		esmService.provisionServiceInstanceAsync(serviceId, null, instanceId);
		return new ResponseEntity<String>(instanceId, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<String> deprovisionServiceInstance(@PathVariable("id") String id) {
		return new ResponseEntity<String>(esmService.deprovisionServiceInstance(id, false), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<SupportServiceInstance> getSupportServiceInstanceById(@PathVariable("id") String id) {
		return new ResponseEntity<SupportServiceInstance>(esmService.getServiceInstanceFromMem(id), HttpStatus.OK);
	}

	@Override
	@JsonView(FrontView.class)
	public ResponseEntity<List<SupportServiceInstance>> getTSSInstByTJobExecId(@PathVariable("id") Long id) {
		return new ResponseEntity<List<SupportServiceInstance>>(esmService.getTSSInstByTJobExecId(id), HttpStatus.OK);
	}	
}
