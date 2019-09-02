package io.elastest.etm.api;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;

import io.elastest.epm.client.model.DockerServiceStatus.DockerServiceStatusEnum;
import io.elastest.etm.model.EtPlugin;
import io.elastest.etm.service.EtPluginsService;
import io.swagger.annotations.ApiParam;

@Controller
public class EtPluginsApiController implements EtPluginsApi {

    @Autowired
    EtPluginsService etPluginsService;

    public ResponseEntity<EtPlugin> startEtPlugin(
            @ApiParam(value = "Engine Name.", required = true) @PathVariable("name") String name) throws Exception {
        EtPlugin engine = etPluginsService.startEngineOrUniquePlugin(name);
        return new ResponseEntity<EtPlugin>(engine, HttpStatus.OK);
    }

    public ResponseEntity<EtPlugin> startEtPluginAsync(
            @ApiParam(value = "Engine Name.", required = true) @PathVariable("name") String name)
            throws Exception {
        EtPlugin engine = etPluginsService.getEtPlugin(name);
        if (engine.getStatus()
                .equals(DockerServiceStatusEnum.NOT_INITIALIZED)) {
            engine.setStatus(DockerServiceStatusEnum.INITIALIZING);
            engine.setStatusMsg("Initializing...");
        }
        etPluginsService.startEngineOrUniquePluginAsync(name);
        return new ResponseEntity<EtPlugin>(engine, HttpStatus.OK);
    }

    public ResponseEntity<List<EtPlugin>> getEtPlugins() {
        List<EtPlugin> testEngines = this.etPluginsService.getEngines();
        return new ResponseEntity<List<EtPlugin>>(testEngines, HttpStatus.OK);
    }

    public ResponseEntity<EtPlugin> getEtPlugin(
            @ApiParam(value = "Engine Name.", required = true) @PathVariable("name") String name) {
        EtPlugin testEngine = null;
        try {
            testEngine = this.etPluginsService.getEtPlugin(name);
            return new ResponseEntity<EtPlugin>(testEngine, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<EtPlugin>(testEngine,
                    HttpStatus.NOT_FOUND);
        }
    }

    public ResponseEntity<EtPlugin> stopEtPlugin(
            @ApiParam(value = "Engine Name.", required = true) @PathVariable("name") String name) {
        EtPlugin engine = etPluginsService.stopEtPlugin(name);
        return new ResponseEntity<EtPlugin>(engine, HttpStatus.OK);
    }

    public ResponseEntity<String> getUrlIfIsRunning(
            @ApiParam(value = "Engine Name.", required = true) @PathVariable("name") String name) {
        String url = this.etPluginsService.getUrlIfIsRunning(name);
        return new ResponseEntity<String>(url, HttpStatus.OK);
    }

    public ResponseEntity<Boolean> isRunning(
            @ApiParam(value = "Engine Name.", required = true) @PathVariable("name") String name) {
        Boolean started = this.etPluginsService.isRunning(name);
        return new ResponseEntity<Boolean>(started, HttpStatus.OK);
    }

    public ResponseEntity<Boolean> isWorking(
            @ApiParam(value = "Engine Url.", required = true) @PathVariable("name") String name) {
        Boolean working = this.etPluginsService.checkIfEtPluginUrlIsUp(name);
        return new ResponseEntity<Boolean>(working, HttpStatus.OK);
    }

    /* **************************************************************** */
    /* *********************** SPECIFIC METHODS *********************** */
    /* **************************************************************** */

    public ResponseEntity<List<EtPlugin>> getUniqueEtPlugins() {
        return new ResponseEntity<List<EtPlugin>>(
                this.etPluginsService.getUniqueEtPlugins(), HttpStatus.OK);
    }

    public ResponseEntity<EtPlugin> getUniqueEtPlugin(
            @ApiParam(value = "Unique EtPlugin Name.", required = true) @PathVariable("name") String name) {
        return new ResponseEntity<EtPlugin>(
                this.etPluginsService.getUniqueEtPlugin(name), HttpStatus.OK);
    }
}
