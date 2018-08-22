package io.elastest.etm.api;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;

import io.elastest.epm.client.model.DockerServiceStatus.DockerServiceStatusEnum;
import io.elastest.etm.model.TestEngine;
import io.elastest.etm.service.TestEnginesService;
import io.swagger.annotations.ApiParam;

@Controller
public class TestEnginesApiController implements TestEnginesApi {

    @Autowired
    TestEnginesService testEngineService;

    public ResponseEntity<TestEngine> startTestEngine(
            @ApiParam(value = "Engine Name.", required = true) @PathVariable("name") String name) {
        TestEngine engine = testEngineService.createInstance(name);
        return new ResponseEntity<TestEngine>(engine, HttpStatus.OK);
    }

    public ResponseEntity<TestEngine> startTestEngineAsync(
            @ApiParam(value = "Engine Name.", required = true) @PathVariable("name") String name) {
        TestEngine engine = testEngineService.getTestEngine(name);
        if (engine.getStatus().equals(DockerServiceStatusEnum.NOT_INITIALIZED)) {
            engine.setStatus(DockerServiceStatusEnum.INITIALIZING);
            engine.setStatusMsg("INITIALIZING...");
        }
        testEngineService.createInstanceAsync(name);
        return new ResponseEntity<TestEngine>(engine, HttpStatus.OK);
    }

    public ResponseEntity<List<TestEngine>> getTestEngines() {
        List<TestEngine> testEngines = this.testEngineService.getTestEngines();
        return new ResponseEntity<List<TestEngine>>(testEngines, HttpStatus.OK);
    }

    public ResponseEntity<TestEngine> getTestEngine(
            @ApiParam(value = "Engine Name.", required = true) @PathVariable("name") String name) {
        TestEngine testEngine = null;
        try {
            testEngine = this.testEngineService.getTestEngine(name);
            return new ResponseEntity<TestEngine>(testEngine, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<TestEngine>(testEngine,
                    HttpStatus.NOT_FOUND);
        }
    }

    public ResponseEntity<TestEngine> stopTestEngine(
            @ApiParam(value = "Engine Name.", required = true) @PathVariable("name") String name) {
        TestEngine engine = testEngineService.stopInstance(name);
        return new ResponseEntity<TestEngine>(engine, HttpStatus.OK);
    }

    public ResponseEntity<String> getUrlIfIsRunning(
            @ApiParam(value = "Engine Name.", required = true) @PathVariable("name") String name) {
        String url = this.testEngineService.getUrlIfIsRunning(name);
        return new ResponseEntity<String>(url, HttpStatus.OK);
    }

    public ResponseEntity<Boolean> isRunning(
            @ApiParam(value = "Engine Name.", required = true) @PathVariable("name") String name) {
        Boolean started = this.testEngineService.isRunning(name);
        return new ResponseEntity<Boolean>(started, HttpStatus.OK);
    }

    public ResponseEntity<Boolean> isWorking(
            @ApiParam(value = "Engine Url.", required = true) @PathVariable("name") String name) {
        Boolean working = this.testEngineService.checkIfEngineUrlIsUp(name);
        return new ResponseEntity<Boolean>(working, HttpStatus.OK);
    }

}
