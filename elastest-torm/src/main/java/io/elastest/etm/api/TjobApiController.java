package io.elastest.etm.api;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import com.fasterxml.jackson.annotation.JsonView;

import io.elastest.etm.model.Parameter;
import io.elastest.etm.model.TJob;
import io.elastest.etm.model.TJob.BasicAttTJob;
import io.elastest.etm.model.TJobExecution;
import io.elastest.etm.model.TJobExecution.BasicAttTJobExec;
import io.elastest.etm.model.TJobExecutionFile;
import io.elastest.etm.service.EsmService;
import io.elastest.etm.service.TJobService;
import io.swagger.annotations.ApiParam;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2017-05-19T13:25:11.074+02:00")

@Controller
public class TjobApiController implements TjobApi {
	
	private static final Logger logger = LoggerFactory.getLogger(TjobApiController.class);

	@Autowired
	private TJobService tJobService;
	
	@Autowired EsmService esmService;

	@JsonView(BasicAttTJob.class)
	public ResponseEntity<TJob> createTJob(
			@ApiParam(value = "TJob object that needs to create", required = true) @Valid @RequestBody TJob body) {
		logger.info("Services:" + body.getSelectedServices());
		logger.info("Services:" + body.getName());
		TJob tJob = tJobService.createTJob(body);
		return new ResponseEntity<TJob>(tJob, HttpStatus.OK);
	}

	@JsonView(BasicAttTJob.class)
	public ResponseEntity<Long> deleteTJob(
			@ApiParam(value = "ID of TJob to delete.", required = true) @PathVariable("tJobId") Long tJobId) {

		tJobService.deleteTJob(tJobId);
		return new ResponseEntity<Long>(tJobId, HttpStatus.OK);
	}

	@JsonView(BasicAttTJobExec.class)
	public ResponseEntity<Long> deleteTJobExecution(
			@ApiParam(value = "TJob Id.", required = true) @PathVariable("tJobId") Long tJobId,
			@ApiParam(value = "TJob Execution Id.", required = true) @PathVariable("tJobExecId") Long tJobExecId) {

		tJobService.deleteTJobExec(tJobExecId);
		return new ResponseEntity<Long>(tJobExecId, HttpStatus.OK);
	}

	@JsonView(BasicAttTJobExec.class)
	public ResponseEntity<TJobExecution> execTJob(
			@ApiParam(value = "TJob Id.", required = true) @PathVariable("tJobId") Long tJobId,
			@ApiParam(value = "Parameters", required = true) @Valid @RequestBody List<Parameter> parameters) {

		TJobExecution tJobExec = tJobService.executeTJob(tJobId, parameters);
		return new ResponseEntity<TJobExecution>(tJobExec, HttpStatus.OK);
	}

	@JsonView(BasicAttTJob.class)
	public ResponseEntity<List<TJob>> getAllTJobs() {

		List<TJob> tjobList = tJobService.getAllTJobs();
		return new ResponseEntity<List<TJob>>(tjobList, HttpStatus.OK);
	}

	@JsonView(BasicAttTJob.class)
	public ResponseEntity<TJob> getTJobById(
			@ApiParam(value = "ID of tJob to retrieve.", required = true) @PathVariable("tJobId") Long tJobId) {
		
		TJob tJob = tJobService.getTJobById(tJobId);		
		return new ResponseEntity<TJob>(tJob, HttpStatus.OK);
	}

	@JsonView(BasicAttTJobExec.class)
	public ResponseEntity<TJobExecution> getTJobsExecution(
			@ApiParam(value = "TJob Id.", required = true) @PathVariable("tJobId") Long tJobId,
			@ApiParam(value = "TJob Execution Id.", required = true) @PathVariable("tJobExecId") Long tJobExecId) {

		TJobExecution tJobExec = tJobService.getTJobsExecution(tJobId, tJobExecId);
		return new ResponseEntity<TJobExecution>(tJobExec, HttpStatus.OK);
	}

	@JsonView(BasicAttTJobExec.class)
	public ResponseEntity<List<TJobExecution>> getTJobsExecutionsByTJob(
			@ApiParam(value = "TJob Id.", required = true) @PathVariable("tJobId") Long tJobId) {

		List<TJobExecution> tjobExecList = tJobService.getTJobsExecutionsByTJobId(tJobId);
		return new ResponseEntity<List<TJobExecution>>(tjobExecList, HttpStatus.OK);
	}

	@JsonView(BasicAttTJob.class)
	public ResponseEntity<TJob> modifyTJob(
			@ApiParam(value = "Tjob object that needs to modify.", required = true) @Valid @RequestBody TJob body) {

		TJob tJob = tJobService.modifyTJob(body);
		return new ResponseEntity<TJob>(tJob, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<List<TJobExecutionFile>> getTJobExecutionFiles(@ApiParam(value = "TJobExec Id.", required = true) @PathVariable("tJobExecId")Long tJobExecId,
			@ApiParam(value = "TJob Id.", required = true) @PathVariable("tJobId") Long tJobId) {
		ResponseEntity<List<TJobExecutionFile>> response;
		
		try{
			response = new ResponseEntity<List<TJobExecutionFile>>(tJobService.getTJobExecutionFilesUrls(tJobId, tJobExecId), HttpStatus.OK);
		}catch(Exception e){
			response = new ResponseEntity<List<TJobExecutionFile>>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		return response;
	}

}
