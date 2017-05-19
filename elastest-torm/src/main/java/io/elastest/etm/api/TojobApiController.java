package io.elastest.etm.api;

import io.elastest.etm.api.model.TOJob;
import io.elastest.etm.api.model.TOJobExecution;
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
public class TojobApiController implements TojobApi {



    public ResponseEntity<TOJob> createTOJob(@ApiParam(value = "TOJjob's desctiption." ,required=true )  @Valid @RequestBody TOJob body) {
        // do some magic!
        return new ResponseEntity<TOJob>(HttpStatus.OK);
    }

    public ResponseEntity<Long> deleteTOJob(@ApiParam(value = "ID of TOJob to delete.",required=true ) @PathVariable("tOJobId") Long tOJobId) {
        // do some magic!
        return new ResponseEntity<Long>(HttpStatus.OK);
    }

    public ResponseEntity<Long> deleteTOJobExec(@ApiParam(value = "ID of TOJob to return.",required=true ) @PathVariable("tOJobId") Long tOJobId,
        @ApiParam(value = "ID of TOJob Execution to return.",required=true ) @PathVariable("tOJobExecId") Long tOJobExecId) {
        // do some magic!
        return new ResponseEntity<Long>(HttpStatus.OK);
    }

    public ResponseEntity<Long> execTOJob(@ApiParam(value = "ID of TOJob to execute.",required=true ) @PathVariable("tOJobId") Long tOJobId) {
        // do some magic!
        return new ResponseEntity<Long>(HttpStatus.OK);
    }

    public ResponseEntity<List<TOJobExecution>> getAllTOJobExecution(@ApiParam(value = "ID of TOJob execution to return.",required=true ) @PathVariable("tOJobId") Long tOJobId) {
        // do some magic!
        return new ResponseEntity<List<TOJobExecution>>(HttpStatus.OK);
    }

    public ResponseEntity<TOJob> getTOJobById(@ApiParam(value = "ID of TOJob to return.",required=true ) @PathVariable("tOJobId") Long tOJobId) {
        // do some magic!
        return new ResponseEntity<TOJob>(HttpStatus.OK);
    }

    public ResponseEntity<TOJobExecution> getTOJobExecById(@ApiParam(value = "ID of TOJob to return.",required=true ) @PathVariable("tOJobId") Long tOJobId,
        @ApiParam(value = "ID of TOJob Execution to return.",required=true ) @PathVariable("tOJobExecId") Long tOJobExecId) {
        // do some magic!
        return new ResponseEntity<TOJobExecution>(HttpStatus.OK);
    }

    public ResponseEntity<List<TOJob>> getTOJobs() {
        // do some magic!
        return new ResponseEntity<List<TOJob>>(HttpStatus.OK);
    }

    public ResponseEntity<TOJob> modifyTOJob(@ApiParam(value = "Id of TJob that needs to be update.",required=true ) @PathVariable("tOJobId") Long tOJobId,
        @ApiParam(value = "TOJob object that needs to be updated." ,required=true )  @Valid @RequestBody TOJob body) {
        // do some magic!
        return new ResponseEntity<TOJob>(HttpStatus.OK);
    }

}
