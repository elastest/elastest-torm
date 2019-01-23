package io.elastest.etm.api;

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import com.fasterxml.jackson.annotation.JsonView;

import io.elastest.etm.model.SutExecution;
import io.elastest.etm.model.SutExecution.SutExecView;
import io.elastest.etm.model.SutSpecification;
import io.elastest.etm.model.SutSpecification.SutView;
import io.elastest.etm.service.SutService;
import io.swagger.annotations.ApiParam;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2017-05-19T13:25:11.074+02:00")

@Controller
public class SutApiController implements SutApi {

    @Autowired
    SutService sutService;

    @JsonView(SutView.class)
    public ResponseEntity<SutSpecification> createSuT(
            @ApiParam(value = "SuT configuration", required = true) @Valid @RequestBody SutSpecification body) {
        SutSpecification sut = sutService.createSutSpecification(body);
        return new ResponseEntity<SutSpecification>(sut, HttpStatus.OK);
    }

    @JsonView(SutView.class)
    public ResponseEntity<Long> deleteSuT(
            @ApiParam(value = "SuT id to delete", required = true) @PathVariable("sutId") Long sutId) {
        sutService.deleteSut(sutId);
        return new ResponseEntity<Long>(sutId, HttpStatus.OK);

    }

    @JsonView(SutView.class)
    public ResponseEntity<SutSpecification> duplicateSuT(
            @ApiParam(value = "SuT id to duplicate", required = true) @PathVariable("sutId") Long sutId) {
        SutSpecification sut = sutService.duplicateSut(sutId);
        return new ResponseEntity<SutSpecification>(sut, HttpStatus.OK);
    }

    @JsonView(SutView.class)
    public ResponseEntity<List<SutSpecification>> getSutList() {
        List<SutSpecification> sutList = sutService.getAllSutSpecification();
        return new ResponseEntity<List<SutSpecification>>(sutList,
                HttpStatus.OK);

    }

    @JsonView(SutView.class)
    public ResponseEntity<SutSpecification> modifySut(
            @ApiParam(value = "SuT configuration", required = true) @Valid @RequestBody SutSpecification body) {
        SutSpecification sut = sutService.modifySut(body);
        return new ResponseEntity<SutSpecification>(sut, HttpStatus.OK);

    }

    @JsonView(SutView.class)
    public ResponseEntity<SutSpecification> getSutById(
            @ApiParam(value = "SuT id to return.", required = true) @PathVariable("sutId") Long sutId) {
        SutSpecification sut = sutService.getSutSpecById(sutId);
        return new ResponseEntity<SutSpecification>(sut, HttpStatus.OK);

    }

    public ResponseEntity<Void> undeploySuT(
            @ApiParam(value = "SuT id to undeploy", required = true) @PathVariable("sutId") Long sutId,
            @ApiParam(value = "SuT Execution id to deploy", required = true) @PathVariable("sutExecId") Long sutExecId) {
        sutService.undeploySut(sutId, sutExecId);
        return new ResponseEntity<Void>(HttpStatus.OK);

    }

    @JsonView(SutExecView.class)
    public ResponseEntity<SutExecution> getSutExec(
            @ApiParam(value = "SuT id to get info", required = true) @PathVariable("sutId") Long sutId,
            @ApiParam(value = "SuT Execution id to get info", required = true) @PathVariable("sutExecId") Long sutExecId) {
        SutExecution sutExec = sutService.getSutExecutionById(sutExecId);
        return new ResponseEntity<SutExecution>(sutExec, HttpStatus.OK);

    }

    @JsonView(SutExecView.class)
    public ResponseEntity<List<SutExecution>> getAllSutExecBySut(
            @ApiParam(value = "Sut id", required = true) @PathVariable("sutId") Long sutId) {
        List<SutExecution> sutExecList = sutService.getAllSutExecBySutId(sutId);
        return new ResponseEntity<List<SutExecution>>(sutExecList,
                HttpStatus.OK);

    }

    @JsonView(SutExecView.class)
    public ResponseEntity<Long> deleteSuTExec(
            @ApiParam(value = "SuT execution id to delete", required = true) @PathVariable("sutExecId") Long sutExecId) {
        sutService.deleteSutExec(sutExecId);
        return new ResponseEntity<Long>(sutExecId, HttpStatus.OK);

    }
}
