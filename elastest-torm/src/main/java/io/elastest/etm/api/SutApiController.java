package io.elastest.etm.api;

import io.elastest.etm.api.model.DeployConfig;
import io.elastest.etm.api.model.Log;
import io.elastest.etm.api.model.SutExecution;
import io.elastest.etm.api.model.SuTMonitoring;
import io.elastest.etm.api.model.SutSpecification;
import io.swagger.annotations.*;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import javax.validation.constraints.*;
import javax.validation.Valid;
@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2017-05-19T13:25:11.074+02:00")

@Controller
public class SutApiController implements SutApi {



    public ResponseEntity<Void> createSuT(@ApiParam(value = "SuT configuration" ,required=true )  @Valid @RequestBody SutSpecification body) {
        // do some magic!
        return new ResponseEntity<Void>(HttpStatus.OK);
    }

    public ResponseEntity<Void> deleteSuT(@ApiParam(value = "SuT id to delete",required=true ) @PathVariable("sutId") Long sutId) {
        // do some magic!
        return new ResponseEntity<Void>(HttpStatus.OK);
    }

    public ResponseEntity<Void> deleteSuTExec(@ApiParam(value = "SuT execution id to delete",required=true ) @PathVariable("sutExecId") Long sutExecId) {
        // do some magic!
        return new ResponseEntity<Void>(HttpStatus.OK);
    }

    public ResponseEntity<SutExecution> deploySuT(@ApiParam(value = "SuT id to deploy",required=true ) @PathVariable("sutId") Long sutId,
        @ApiParam(value = "Configuration for deploy" ,required=true )  @Valid @RequestBody DeployConfig deployConfig) {
        // do some magic!
        return new ResponseEntity<SutExecution>(HttpStatus.OK);
    }

    public ResponseEntity<SutExecution> suTExecInfo(@ApiParam(value = "SuT id to undeploy",required=true ) @PathVariable("sutId") Long sutId,
        @ApiParam(value = "SuT Execution id to deploy",required=true ) @PathVariable("sutExecId") Long sutExecId) {
        // do some magic!
        return new ResponseEntity<SutExecution>(HttpStatus.OK);
    }

    public ResponseEntity<List<Log>> suTLogs(@ApiParam(value = "SuT id to return logs",required=true ) @PathVariable("sutId") Long sutId,
        @ApiParam(value = "SuT Execution id to deploy",required=true ) @PathVariable("sutExecId") Long sutExecId) {
        // do some magic!
        return new ResponseEntity<List<Log>>(HttpStatus.OK);
    }

    public ResponseEntity<SuTMonitoring> suTMonitoring(@ApiParam(value = "SuT id to return monitoring information",required=true ) @PathVariable("sutId") Long sutId,
        @ApiParam(value = "SuT Execution id to deploy",required=true ) @PathVariable("sutExecId") Long sutExecId) {
        // do some magic!
        return new ResponseEntity<SuTMonitoring>(HttpStatus.OK);
    }

    public ResponseEntity<String> suTStatus(@ApiParam(value = "SuT id to return status",required=true ) @PathVariable("sutId") Long sutId,
        @ApiParam(value = "SuT Execution id to deploy",required=true ) @PathVariable("sutExecId") Long sutExecId) {
        // do some magic!
        return new ResponseEntity<String>(HttpStatus.OK);
    }

    public ResponseEntity<List<SutSpecification>> sutGet() {
        // do some magic!
        return new ResponseEntity<List<SutSpecification>>(HttpStatus.OK);
    }

    public ResponseEntity<Void> sutPut(@ApiParam(value = "SuT configuration" ,required=true )  @Valid @RequestBody SutSpecification body) {
        // do some magic!
        return new ResponseEntity<Void>(HttpStatus.OK);
    }

    public ResponseEntity<List<SutExecution>> getAllSutExecBySut() {
        // do some magic!
        return new ResponseEntity<List<SutExecution>>(HttpStatus.OK);
    }

    public ResponseEntity<SutSpecification> sutSutIdGet(@ApiParam(value = "SuT id to return.",required=true ) @PathVariable("sutId") Long sutId) {
        // do some magic!
        return new ResponseEntity<SutSpecification>(HttpStatus.OK);
    }

    public ResponseEntity<Void> undeploySuT(@ApiParam(value = "SuT id to undeploy",required=true ) @PathVariable("sutId") Long sutId,
        @ApiParam(value = "SuT Execution id to deploy",required=true ) @PathVariable("sutExecId") Long sutExecId) {
        // do some magic!
        return new ResponseEntity<Void>(HttpStatus.OK);
    }

}
