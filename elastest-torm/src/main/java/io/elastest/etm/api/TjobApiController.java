package io.elastest.etm.api;

import io.elastest.etm.api.model.TJob;
import io.elastest.etm.api.model.TJobExecution;
import io.elastest.etm.model.ElasEtmTjob;
import io.elastest.etm.model.ElasEtmTjobexec;
import io.elastest.etm.tjob.service.TJobService;
import io.elastest.etm.utils.DataConverter;
import io.swagger.annotations.*;

import org.apache.http.HttpException;
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
import javax.xml.ws.http.HTTPException;
import javax.validation.Valid;
@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2017-05-19T13:25:11.074+02:00")

@Controller
public class TjobApiController implements TjobApi {

	@Autowired
	private TJobService tJobService;
	
	private DataConverter dataConverter = new DataConverter();

    public ResponseEntity<TJob> createTJob(@ApiParam(value = "Tjob object that needs to create" ,required=true )  @Valid @RequestBody TJob body) {
    	try{
    		ElasEtmTjob etmTjob = tJobService.createTJob(dataConverter.apiTjobToEtmTJob(body));
    		return new ResponseEntity<TJob>(dataConverter.etmTjobToApiTJob(etmTjob),HttpStatus.OK);
    	}
    	catch (Exception e) {
    		return new ResponseEntity<TJob>(HttpStatus.METHOD_NOT_ALLOWED);
		}
    }

    public ResponseEntity<Long> deleteTJob(@ApiParam(value = "ID of tJob to delete.",required=true ) @PathVariable("tJobId") Long tJobId) {
    	try{
    		tJobService.deleteTJob(tJobId);
    		return new ResponseEntity<Long>(tJobId, HttpStatus.OK);
    	}catch (Exception e) {
    		return new ResponseEntity<Long>(tJobId, HttpStatus.NOT_FOUND);
		}
    }

    public ResponseEntity<Long> deleteTJobExecution(@ApiParam(value = "TJob Id.",required=true ) @PathVariable("tJobId") Long tJobId,
        @ApiParam(value = "TJob Execution Id.",required=true ) @PathVariable("tJobExecId") Long tJobExecId) {
    	try{
    		tJobService.deleteTJobExec(tJobExecId);
    		return new ResponseEntity<Long>(tJobExecId, HttpStatus.OK);
    	}
    	catch (Exception e) {
    		return new ResponseEntity<Long>(tJobExecId, HttpStatus.NOT_FOUND);
		}
    }

    public ResponseEntity<TJobExecution> execTJob(@ApiParam(value = "TJob Id.",required=true ) @PathVariable("tJobId") Long tJobId) {
    	try{
	    	ElasEtmTjobexec etmTJobExec = tJobService.executeTJob(tJobId);
	    	TJobExecution tJobExec = dataConverter.etmTjobexecToApiTJobExec(etmTJobExec);
	        return new ResponseEntity<TJobExecution>(tJobExec, HttpStatus.OK);
    	}
    	catch (Exception e) {
	        return new ResponseEntity<TJobExecution>(HttpStatus.NOT_FOUND);
		}
    }

    public ResponseEntity<List<TJob>> getAllTJobs() {
    	try{
	        List<ElasEtmTjob> etmTjobList = tJobService.getAllTJobs();
	        List<TJob> tjobList = new ArrayList<>();
	        for (ElasEtmTjob elasEtmTjob : etmTjobList ) {
	        	tjobList.add(dataConverter.etmTjobToApiTJob(elasEtmTjob));
			}
	        return new ResponseEntity<List<TJob>>(tjobList, HttpStatus.OK);
    	}
    	catch (Exception e) {
    		return new ResponseEntity<List<TJob>>(HttpStatus.NOT_FOUND);
		}
    }

    public ResponseEntity<TJob> getTJobById(@ApiParam(value = "ID of tJob to retrieve.",required=true ) @PathVariable("tJobId") Long tJobId) {
    	try{
	        ElasEtmTjob etmTJob = tJobService.getTJobById(tJobId);
	        TJob tJob = dataConverter.etmTjobToApiTJob(etmTJob);
	        return new ResponseEntity<TJob>(tJob, HttpStatus.OK);
    	}
    	catch (Exception e) {
	        return new ResponseEntity<TJob>(HttpStatus.BAD_REQUEST);
		}
    }

    public ResponseEntity<TJobExecution> getTJobsExecution(@ApiParam(value = "TJob Id.",required=true ) @PathVariable("tJobId") Long tJobId,
        @ApiParam(value = "TJob Execution Id.",required=true ) @PathVariable("tJobExecId") Long tJobExecId) {
    	try{
	        ElasEtmTjobexec etmTjobExec = tJobService.getTJobsExecution(tJobId, tJobExecId);
	        TJobExecution tJobExec = dataConverter.etmTjobexecToApiTJobExec(etmTjobExec);
	        return new ResponseEntity<TJobExecution>(tJobExec, HttpStatus.OK);
    	}
    	catch (Exception e) {
    	    return new ResponseEntity<TJobExecution>(HttpStatus.NOT_FOUND);
		}
    }

    public ResponseEntity<List<TJobExecution>> getTJobsExecutionsByTJob(@ApiParam(value = "TJob Id.",required=true ) @PathVariable("tJobId") Long tJobId) {
        try{
	    	List<ElasEtmTjobexec> etmTjobExecList = tJobService.getTJobsExecutionsByTJob(tJobId);
	        List<TJobExecution> tjobExecList = new ArrayList<>();
	        for (ElasEtmTjobexec elasEtmTjobExec : etmTjobExecList ) {
	        	tjobExecList.add(dataConverter.etmTjobexecToApiTJobExec(elasEtmTjobExec));
			}
	        return new ResponseEntity<List<TJobExecution>>(tjobExecList, HttpStatus.OK);
        }
        catch (Exception e) {
        	return new ResponseEntity<List<TJobExecution>>(HttpStatus.NOT_FOUND);
		}
    }

    public ResponseEntity<TJob> modifyTJob(@ApiParam(value = "Tjob object that needs to modify." ,required=true )  @Valid @RequestBody TJob body) {
    	try{
    		ElasEtmTjob etmTJob = tJobService.modifyTJob(dataConverter.apiTjobToEtmTJob(body));
    		return new ResponseEntity<TJob>(dataConverter.etmTjobToApiTJob(etmTJob), HttpStatus.OK);
    	}
    	catch (Exception e) {
    		if(dataConverter.getHttpExceptionCode(e) == 405){
				return new ResponseEntity<TJob>(HttpStatus.METHOD_NOT_ALLOWED);
    		}
    		return new ResponseEntity<TJob>(HttpStatus.BAD_REQUEST);
		} 
    }

}
