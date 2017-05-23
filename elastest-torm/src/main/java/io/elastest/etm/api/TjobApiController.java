package io.elastest.etm.api;

import io.elastest.etm.api.model.TJob;
import io.elastest.etm.api.model.TJobExecution;
import io.elastest.etm.model.ElasEtmTjob;
import io.elastest.etm.model.ElasEtmTjobexec;
import io.elastest.etm.tjob.service.TJobService;
import io.elastest.etm.utils.DataConverter;
import io.swagger.annotations.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.*;
import javax.validation.Valid;
@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2017-05-19T13:25:11.074+02:00")

@Controller
public class TjobApiController implements TjobApi {

	@Autowired
	private TJobService tJobService;
	
	private DataConverter dataConverter = new DataConverter();

    public ResponseEntity<TJob> createTJob(@ApiParam(value = "Tjob object that needs to create" ,required=true )  @Valid @RequestBody TJob body) {
     	ElasEtmTjob etmTjob = tJobService.createTJob(dataConverter.apiTjobToEtmTJob(body));
        return new ResponseEntity<TJob>(dataConverter.etmTjobToApiTJob(etmTjob),HttpStatus.OK);
    }

    public ResponseEntity<Long> deleteTJob(@ApiParam(value = "ID of tJob to delete.",required=true ) @PathVariable("tJobId") Long tJobId) {
        // do some magic!
        return new ResponseEntity<Long>(HttpStatus.OK);
    }

    public ResponseEntity<Long> deleteTJobExecution(@ApiParam(value = "TJob Id.",required=true ) @PathVariable("tJobId") Long tJobId,
        @ApiParam(value = "TJob Execution Id.",required=true ) @PathVariable("tJobExecId") Long tJobExecId) {
    	tJobService.deleteTJobExec(tJobExecId);
        return new ResponseEntity<Long>(HttpStatus.OK);
    }

    public ResponseEntity<TJobExecution> execTJob(@ApiParam(value = "TJob Id.",required=true ) @PathVariable("tJobId") Long tJobId) {
    	ElasEtmTjobexec etmTJobExec = tJobService.executeTJob(tJobId);
    	TJobExecution tJobExec = dataConverter.etmTjobexecToApiTJobExec(etmTJobExec);
        return new ResponseEntity<TJobExecution>(tJobExec, HttpStatus.OK);
    }

    public ResponseEntity<List<TJob>> getAllTJobs() {
        List<ElasEtmTjob> etmTjobList = tJobService.getAllTJobs();
        List<TJob> tjobList = new ArrayList<>();
        for (ElasEtmTjob elasEtmTjob : etmTjobList ) {
        	tjobList.add(dataConverter.etmTjobToApiTJob(elasEtmTjob));
        	System.out.println("asd");
		}
        return new ResponseEntity<List<TJob>>(tjobList, HttpStatus.OK);
    }

    public ResponseEntity<TJob> getTJobById(@ApiParam(value = "ID of tJob to retrieve.",required=true ) @PathVariable("tJobId") Long tJobId) {
        // do some magic!
        return new ResponseEntity<TJob>(HttpStatus.OK);
    }

    public ResponseEntity<TJobExecution> getTJobsExecution(@ApiParam(value = "TJob Id.",required=true ) @PathVariable("tJobId") Long tJobId,
        @ApiParam(value = "TJob Execution Id.",required=true ) @PathVariable("tJobExecId") Long tJobExecId) {
        // do some magic!
        return new ResponseEntity<TJobExecution>(HttpStatus.OK);
    }

    public ResponseEntity<List<TJobExecution>> getTJobsExecutionsByTJob(@ApiParam(value = "TJob Id.",required=true ) @PathVariable("tJobId") Long tJobId) {
        // do some magic!
        return new ResponseEntity<List<TJobExecution>>(HttpStatus.OK);
    }

    public ResponseEntity<TJob> modifyTJob(@ApiParam(value = "Tjob object that needs to modify." ,required=true )  @Valid @RequestBody TJob body) {
        // do some magic!
        return new ResponseEntity<TJob>(HttpStatus.OK);
    }

}
