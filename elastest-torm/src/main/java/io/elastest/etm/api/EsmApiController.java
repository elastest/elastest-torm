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
import org.springframework.web.bind.annotation.PathVariable;

import com.fasterxml.jackson.annotation.JsonView;

import io.elastest.etm.model.SupportService;
import io.elastest.etm.model.SupportServiceInstance;
import io.elastest.etm.model.SupportServiceInstance.FrontView;
import io.elastest.etm.service.EsmService;
import io.elastest.etm.utils.ElastestConstants;
import io.swagger.annotations.ApiParam;

@Controller
public class EsmApiController implements EsmApi {

	private static final Logger logger = LoggerFactory.getLogger(EsmApiController.class);

	@Autowired
	EsmService esmService;

	@Value("${elastest.execution.mode}")
	public String ELASTEST_EXECUTION_MODE;

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
		return new ResponseEntity<List<SupportServiceInstance>>(esmService.getServicesInstances(), HttpStatus.OK);
	}

	@Override
	@JsonView(FrontView.class)
	public ResponseEntity<SupportServiceInstance> provisionServiceInstance(
			@ApiParam(value = "Service Id", required = true) @PathVariable("serviceId") String serviceId) {
		logger.info("Service provision:" + serviceId);
		return new ResponseEntity<SupportServiceInstance>(esmService.provisionServiceInstance(serviceId, false),
				HttpStatus.OK);
	}

	@Override
	public ResponseEntity<String> deprovisionServiceInstance(@PathVariable("instanceId") String instanceId) {
		return new ResponseEntity<String>(esmService.deprovisionServiceInstance(instanceId, false), HttpStatus.OK);
	}

	@Override
	public ResponseEntity<SupportServiceInstance> getSupportServiceInstanceById(@PathVariable("id") String id) {
		return new ResponseEntity<SupportServiceInstance>(esmService.getServiceInstanceFromMem(id), HttpStatus.OK);
	}
}
