package io.elastest.etm.api;

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import io.elastest.etm.service.TestEnginesService;
import io.swagger.annotations.ApiParam;

@Controller
public class TestEnginesApiController implements TestEnginesApi {

	@Autowired
	TestEnginesService testEngineService;

	public ResponseEntity<String> createTestEngine(
			@ApiParam(value = "Data to create the new Test Engine", required = true) @Valid @RequestBody String engineName) {
		String url = testEngineService.createInstance(engineName);
		return new ResponseEntity<String>(url, HttpStatus.OK);
	}

	public ResponseEntity<String> stopTestEngine(
			@ApiParam(value = "Engine Name.", required = true) @PathVariable("name") String name) {
		testEngineService.stopInstance(name);
		return new ResponseEntity<String>(name, HttpStatus.OK);
	}

	public ResponseEntity<Boolean> isRunning(
			@ApiParam(value = "Engine Name.", required = true) @PathVariable("name") String name) {
		Boolean started = this.testEngineService.isStarted(name);
		return new ResponseEntity<Boolean>(started, HttpStatus.OK);
	}

	public ResponseEntity<List<String>> getTestEngines() {
		List<String> testEngines = this.testEngineService.getTestEngines();
		return new ResponseEntity<List<String>>(testEngines, HttpStatus.OK);
	}
	
}
