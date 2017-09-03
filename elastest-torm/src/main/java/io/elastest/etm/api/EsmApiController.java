package io.elastest.etm.api;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import com.fasterxml.jackson.annotation.JsonView;

import io.elastest.etm.model.TJob.BasicAttTJob;
import io.elastest.etm.service.EsmService;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2017-05-19T13:25:11.074+02:00")

@Controller
public class EsmApiController implements EsmApi {
	
	private static final Logger logger = LoggerFactory.getLogger(EsmApiController.class);

	@Autowired EsmService esmService;

	@JsonView(BasicAttTJob.class)
	@Override
	public ResponseEntity<List<String>> getElastestServices() {
		List<String> servicesList = esmService.getRegisteredServices();
		return new ResponseEntity<List<String>>(servicesList, HttpStatus.OK);
	}	
}
