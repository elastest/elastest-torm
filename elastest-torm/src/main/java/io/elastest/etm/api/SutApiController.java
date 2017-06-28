package io.elastest.etm.api;

import io.elastest.etm.api.model.DeployConfig;
import io.elastest.etm.api.model.Log;
import io.elastest.etm.api.model.SutExecution;
import io.elastest.etm.api.model.SuTMonitoring;
import io.elastest.etm.api.model.SutSpecification;
import io.elastest.etm.api.model.SutExecution.SutExecView;
import io.elastest.etm.api.model.SutSpecification.SutView;
import io.elastest.etm.service.sut.SutService;
import io.elastest.etm.utils.UtilTools;
import io.swagger.annotations.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import com.fasterxml.jackson.annotation.JsonView;

import java.util.List;

import javax.validation.Valid;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2017-05-19T13:25:11.074+02:00")

@Controller
public class SutApiController implements SutApi {

	@Autowired
	SutService sutService;

	private UtilTools utilTools = new UtilTools();

	@CrossOrigin(origins = { "http://localhost:4200" })
	@JsonView(SutView.class)
	public ResponseEntity<SutSpecification> createSuT(
			@ApiParam(value = "SuT configuration", required = true) @Valid @RequestBody SutSpecification body) {
		try {
			SutSpecification sut = sutService.createSutSpecification(body);
			return new ResponseEntity<SutSpecification>(sut, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<SutSpecification>(HttpStatus.valueOf(utilTools.getHttpExceptionCode(e)));
		}
	}

	@CrossOrigin(origins = { "http://localhost:4200" })
	@JsonView(SutView.class)
	public ResponseEntity<Long> deleteSuT(
			@ApiParam(value = "SuT id to delete", required = true) @PathVariable("sutId") Long sutId) {
		try {
			sutService.deleteSut(sutId);
			return new ResponseEntity<Long>(sutId, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<Long>(sutId, HttpStatus.valueOf(utilTools.getHttpExceptionCode(e)));
		}
	}

	@CrossOrigin(origins = { "http://localhost:4200" })
	@JsonView(SutView.class)
	public ResponseEntity<List<SutSpecification>> getSutList() {
		try {
			List<SutSpecification> sutList = sutService.getAllSutSpecification();
			return new ResponseEntity<List<SutSpecification>>(sutList, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<List<SutSpecification>>(HttpStatus.valueOf(utilTools.getHttpExceptionCode(e)));
		}
	}

	@CrossOrigin(origins = { "http://localhost:4200" })
	@JsonView(SutView.class)
	public ResponseEntity<SutSpecification> modifySut(
			@ApiParam(value = "SuT configuration", required = true) @Valid @RequestBody SutSpecification body) {
		try {
			SutSpecification sut = sutService.modifySut(body);
			return new ResponseEntity<SutSpecification>(sut, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<SutSpecification>(HttpStatus.valueOf(utilTools.getHttpExceptionCode(e)));
		}
	}

	@CrossOrigin(origins = { "http://localhost:4200" })
	@JsonView(SutExecView.class)
	public ResponseEntity<SutExecution> deploySuT(
			@ApiParam(value = "SuT id to deploy", required = true) @PathVariable("sutId") Long sutId,
			@ApiParam(value = "Configuration for deploy", required = true) @Valid @RequestBody DeployConfig deployConfig) {
		try {
			SutExecution sutExec = sutService.createSutExecutionById(sutId);
			return new ResponseEntity<SutExecution>(sutExec, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<SutExecution>(HttpStatus.valueOf(utilTools.getHttpExceptionCode(e)));
		}
	}

	@CrossOrigin(origins = { "http://localhost:4200" })
	@JsonView(SutView.class)
	public ResponseEntity<SutSpecification> getSutById(
			@ApiParam(value = "SuT id to return.", required = true) @PathVariable("sutId") Long sutId) {
		try {
			SutSpecification sut = sutService.getSutSpecById(sutId);
			return new ResponseEntity<SutSpecification>(sut, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<SutSpecification>(HttpStatus.valueOf(utilTools.getHttpExceptionCode(e)));
		}
	}

	public ResponseEntity<Void> undeploySuT(
			@ApiParam(value = "SuT id to undeploy", required = true) @PathVariable("sutId") Long sutId,
			@ApiParam(value = "SuT Execution id to deploy", required = true) @PathVariable("sutExecId") Long sutExecId) {
		try {
			sutService.undeploySut(sutId, sutExecId);
			return new ResponseEntity<Void>(HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<Void>(HttpStatus.valueOf(utilTools.getHttpExceptionCode(e)));
		}
	}

	@CrossOrigin(origins = { "http://localhost:4200" })
	@JsonView(SutExecView.class)
	public ResponseEntity<SutExecution> getSutExec(
			@ApiParam(value = "SuT id to get info", required = true) @PathVariable("sutId") Long sutId,
			@ApiParam(value = "SuT Execution id to get info", required = true) @PathVariable("sutExecId") Long sutExecId) {
		try {
			SutExecution sutExec = sutService.getSutExecutionById(sutExecId);
			return new ResponseEntity<SutExecution>(sutExec, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<SutExecution>(HttpStatus.valueOf(utilTools.getHttpExceptionCode(e)));
		}
	}

	@CrossOrigin(origins = { "http://localhost:4200" })
	@JsonView(SutExecView.class)
	public ResponseEntity<List<SutExecution>> getAllSutExecBySut(
			@ApiParam(value = "Sut id", required = true) @PathVariable("sutId") Long sutId) {
		try {
			List<SutExecution> sutExecList = sutService.getAllSutExecBySutId(sutId);
			return new ResponseEntity<List<SutExecution>>(sutExecList, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<List<SutExecution>>(HttpStatus.NOT_FOUND);
		}
	}

	@CrossOrigin(origins = { "http://localhost:4200" })
	@JsonView(SutExecView.class)
	public ResponseEntity<Long> deleteSuTExec(
			@ApiParam(value = "SuT execution id to delete", required = true) @PathVariable("sutExecId") Long sutExecId) {
		try {
			sutService.deleteSutExec(sutExecId);
			return new ResponseEntity<Long>(sutExecId, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<Long>(sutExecId, HttpStatus.valueOf(utilTools.getHttpExceptionCode(e)));
		}
	}

	@CrossOrigin(origins = { "http://localhost:4200" })
	public ResponseEntity<List<Log>> suTLogs(
			@ApiParam(value = "SuT id to return logs", required = true) @PathVariable("sutId") Long sutId,
			@ApiParam(value = "SuT Execution id to deploy", required = true) @PathVariable("sutExecId") Long sutExecId) {
		// do some magic!
		return new ResponseEntity<List<Log>>(HttpStatus.OK);
	}

	@CrossOrigin(origins = { "http://localhost:4200" })
	public ResponseEntity<SuTMonitoring> suTMonitoring(
			@ApiParam(value = "SuT id to return monitoring information", required = true) @PathVariable("sutId") Long sutId,
			@ApiParam(value = "SuT Execution id to deploy", required = true) @PathVariable("sutExecId") Long sutExecId) {
		// do some magic!
		return new ResponseEntity<SuTMonitoring>(HttpStatus.OK);
	}

	@CrossOrigin(origins = { "http://localhost:4200" })
	public ResponseEntity<String> suTStatus(
			@ApiParam(value = "SuT id to return status", required = true) @PathVariable("sutId") Long sutId,
			@ApiParam(value = "SuT Execution id to deploy", required = true) @PathVariable("sutExecId") Long sutExecId) {
		// do some magic!
		return new ResponseEntity<String>(HttpStatus.OK);
	}
}
