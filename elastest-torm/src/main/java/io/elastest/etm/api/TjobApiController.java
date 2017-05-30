package io.elastest.etm.api;

import io.elastest.etm.api.model.TJob;
import io.elastest.etm.api.model.TJob.BasicAttTJob;
import io.elastest.etm.api.model.TJobExecution;
import io.elastest.etm.api.model.TJobExecution.BasicAttTJobExec;
import io.elastest.etm.service.tjob.TJobService;
import io.elastest.etm.utils.UtilTools;
import io.swagger.annotations.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import com.fasterxml.jackson.annotation.JsonView;

import java.util.List;

import javax.validation.Valid;
@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2017-05-19T13:25:11.074+02:00")

@Controller
public class TjobApiController implements TjobApi {

	@Autowired
	private TJobService tJobService;
	
	private UtilTools utilTools = new UtilTools();

	@JsonView(BasicAttTJob.class)
    public ResponseEntity<TJob> createTJob(@ApiParam(value = "Tjob object that needs to create" ,required=true )  @Valid @RequestBody TJob body) {
    	try{
    		TJob tJob = tJobService.createTJob(body);
    		return new ResponseEntity<TJob>(tJob ,HttpStatus.OK);
    	}
    	catch (Exception e) {
    		return new ResponseEntity<TJob>(HttpStatus.METHOD_NOT_ALLOWED);
		}
    }

	@JsonView(BasicAttTJob.class)
    public ResponseEntity<Long> deleteTJob(@ApiParam(value = "ID of tJob to delete.",required=true ) @PathVariable("tJobId") Long tJobId) {
    	try{
    		tJobService.deleteTJob(tJobId);
    		return new ResponseEntity<Long>(tJobId, HttpStatus.OK);
    	}catch (Exception e) {
    		return new ResponseEntity<Long>(tJobId, HttpStatus.NOT_FOUND);
		}
    }

	@JsonView(BasicAttTJobExec.class)
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

	@JsonView(BasicAttTJobExec.class)
    public ResponseEntity<TJobExecution> execTJob(@ApiParam(value = "TJob Id.",required=true ) @PathVariable("tJobId") Long tJobId) {
    	try{
    		TJobExecution tJobExec = tJobService.executeTJob(tJobId);
	        return new ResponseEntity<TJobExecution>(tJobExec, HttpStatus.OK);
    	}
    	catch (Exception e) {
    		if(utilTools.getHttpExceptionCode(e) == 500){
    			return new ResponseEntity<TJobExecution>(HttpStatus.INTERNAL_SERVER_ERROR);
    		}
    		else{
    			return new ResponseEntity<TJobExecution>(HttpStatus.NOT_FOUND);
	        }
		}
    }

	@JsonView(BasicAttTJob.class)
    public ResponseEntity<List<TJob>> getAllTJobs() {
    	try{
    		List<TJob> tjobList = tJobService.getAllTJobs();
	        return new ResponseEntity<List<TJob>>(tjobList, HttpStatus.OK);
    	}
    	catch (Exception e) {
    		return new ResponseEntity<List<TJob>>(HttpStatus.NOT_FOUND);
		}
    }

	@JsonView(BasicAttTJob.class)
	public ResponseEntity<TJob> getTJobById(@ApiParam(value = "ID of tJob to retrieve.",required=true ) @PathVariable("tJobId") Long tJobId) {
    	try{
    		TJob tJob = tJobService.getTJobById(tJobId);
	        return new ResponseEntity<TJob>(tJob, HttpStatus.OK);
    	}
    	catch (Exception e) {
	        return new ResponseEntity<TJob>(HttpStatus.BAD_REQUEST);
		}
    }

	@JsonView(BasicAttTJobExec.class)
    public ResponseEntity<TJobExecution> getTJobsExecution(@ApiParam(value = "TJob Id.",required=true ) @PathVariable("tJobId") Long tJobId,
        @ApiParam(value = "TJob Execution Id.",required=true ) @PathVariable("tJobExecId") Long tJobExecId) {
    	try{
    		TJobExecution tJobExec = tJobService.getTJobsExecution(tJobId, tJobExecId);
	        return new ResponseEntity<TJobExecution>(tJobExec, HttpStatus.OK);
    	}
    	catch (Exception e) {
    	    return new ResponseEntity<TJobExecution>(HttpStatus.NOT_FOUND);
		}
    }

	@JsonView(BasicAttTJobExec.class)
    public ResponseEntity<List<TJobExecution>> getTJobsExecutionsByTJob(@ApiParam(value = "TJob Id.",required=true ) @PathVariable("tJobId") Long tJobId) {
        try{
        	List<TJobExecution> tjobExecList = tJobService.getTJobsExecutionsByTJob(tJobId);
	        return new ResponseEntity<List<TJobExecution>>(tjobExecList, HttpStatus.OK);
        }
        catch (Exception e) {
        	return new ResponseEntity<List<TJobExecution>>(HttpStatus.NOT_FOUND);
		}
    }

	@JsonView(BasicAttTJob.class)
    public ResponseEntity<TJob> modifyTJob(@ApiParam(value = "Tjob object that needs to modify." ,required=true )  @Valid @RequestBody TJob body) {
    	try{
    		TJob tJob = tJobService.modifyTJob(body);
    		return new ResponseEntity<TJob>(tJob, HttpStatus.OK);
    	}
    	catch (Exception e) {
    		if(utilTools.getHttpExceptionCode(e) == 405){
				return new ResponseEntity<TJob>(HttpStatus.METHOD_NOT_ALLOWED);
    		}
    		return new ResponseEntity<TJob>(HttpStatus.BAD_REQUEST);
		} 
    }

}
