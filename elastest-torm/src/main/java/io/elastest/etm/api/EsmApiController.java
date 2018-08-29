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

    private static final Logger logger = LoggerFactory
            .getLogger(EsmApiController.class);

    private EsmService esmService;

    @Autowired
    public EsmApiController(EsmService esmService) {
        super();
        this.esmService = esmService;
    }

    @Override
    public ResponseEntity<List<String>> getSupportServicesNames() {
        List<String> servicesList = esmService.getRegisteredServicesName();
        return new ResponseEntity<List<String>>(servicesList, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<SupportService>> getSupportServices() {
        List<SupportService> servicesList = esmService.getRegisteredServices();
        return new ResponseEntity<List<SupportService>>(servicesList,
                HttpStatus.OK);
    }

    @Override
    @JsonView(FrontView.class)
    public ResponseEntity<List<SupportServiceInstance>> getSupportServicesInstances() {
        return new ResponseEntity<List<SupportServiceInstance>>(
                esmService.getServicesInstancesAsList(), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<String> provisionServiceInstance(
            @ApiParam(value = "Service Id", required = true) @PathVariable("serviceId") String serviceId) {
        logger.info("Service provision:" + serviceId);
        String instanceId = UtilTools.generateUniqueId();
        esmService.provisionServiceInstanceAsync(serviceId, instanceId);
        return new ResponseEntity<String>(instanceId, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<String> deprovisionServiceInstance(
            @PathVariable("id") String id) {
        return new ResponseEntity<String>(
                esmService.deprovisionServiceInstance(id, null), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<String> deprovisionTJobExecServiceInstance(
            @PathVariable("id") String id,
            @PathVariable("tJobExecId") Long tJobExecId) {
        return new ResponseEntity<String>(
                esmService.deprovisionTJobExecServiceInstance(id, tJobExecId),
                HttpStatus.OK);
    }

    @Override
    public ResponseEntity<String> deprovisionExternalTJobExecServiceInstance(
            @PathVariable("id") String id,
            @PathVariable("externalTJobExecId") Long externalTJobExecId) {
        return new ResponseEntity<String>(
                esmService.deprovisionExternalTJobExecServiceInstance(id,
                        externalTJobExecId),
                HttpStatus.OK);
    }

    @Override
    public ResponseEntity<SupportServiceInstance> getSupportServiceInstanceById(
            @PathVariable("id") String id) {
        return new ResponseEntity<SupportServiceInstance>(
                esmService.getServiceInstanceFromMem(id), HttpStatus.OK);
    }

    public ResponseEntity<SupportServiceInstance> getTJobExecSupportServiceInstanceById(
            @PathVariable("id") String id) {
        return new ResponseEntity<SupportServiceInstance>(
                esmService.getTJobExecServiceInstance(id), HttpStatus.OK);
    }

    public ResponseEntity<SupportServiceInstance> getExternalTJobExecSupportServiceInstanceById(
            @PathVariable("id") String id) {
        return new ResponseEntity<SupportServiceInstance>(
                esmService.getExternalTJobExecServiceInstance(id),
                HttpStatus.OK);
    }

    @Override
    @JsonView(FrontView.class)
    public ResponseEntity<List<SupportServiceInstance>> getTSSInstByTJobExecId(
            @PathVariable("id") Long id) {
        return new ResponseEntity<List<SupportServiceInstance>>(
                esmService.getTSSInstByTJobExecId(id), HttpStatus.OK);
    }
}
