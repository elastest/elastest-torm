package io.elastest.etm.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpClientErrorException;

import com.fasterxml.jackson.annotation.JsonView;

import io.elastest.etm.model.ExecData;
import io.elastest.etm.model.TJob;
import io.elastest.etm.model.TJob.TJobCompleteView;
import io.elastest.etm.model.TJobExecution;
import io.elastest.etm.model.TJobExecution.TJobExecCompleteView;
import io.elastest.etm.model.TJobExecutionFile;
import io.elastest.etm.service.EsmService;
import io.elastest.etm.service.TJobService;
import io.swagger.annotations.ApiParam;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2017-05-19T13:25:11.074+02:00")

@Controller
public class TjobApiController implements TjobApi {

    private static final Logger logger = LoggerFactory
            .getLogger(TjobApiController.class);

    @Autowired
    private TJobService tJobService;

    @Autowired
    EsmService esmService;

    /* *************** */
    /* **** TJobs **** */
    /* *************** */

    @JsonView(TJobCompleteView.class)
    public ResponseEntity<TJob> createTJob(
            @ApiParam(value = "TJob object that needs to create", required = true) @Valid @RequestBody TJob body) {
        logger.info("Services:" + body.getSelectedServices());
        logger.info("Services:" + body.getName());
        TJob tJob = tJobService.createTJob(body);
        return new ResponseEntity<TJob>(tJob, HttpStatus.OK);
    }

    @JsonView(TJobCompleteView.class)
    public ResponseEntity<Long> deleteTJob(
            @ApiParam(value = "ID of TJob to delete.", required = true) @PathVariable("tJobId") Long tJobId) {

        tJobService.deleteTJob(tJobId);
        return new ResponseEntity<Long>(tJobId, HttpStatus.OK);
    }

    @JsonView(TJobCompleteView.class)
    public ResponseEntity<TJob> modifyTJob(
            @ApiParam(value = "Tjob object that needs to modify.", required = true) @Valid @RequestBody TJob body) {

        TJob tJob = tJobService.modifyTJob(body);
        return new ResponseEntity<TJob>(tJob, HttpStatus.OK);
    }

    @JsonView(TJobCompleteView.class)
    public ResponseEntity<List<TJob>> getAllTJobs() {

        List<TJob> tjobList = tJobService.getAllTJobs();
        return new ResponseEntity<List<TJob>>(tjobList, HttpStatus.OK);
    }

    @JsonView(TJobCompleteView.class)
    public ResponseEntity<TJob> getTJobById(
            @ApiParam(value = "TJob id.", required = true) @PathVariable("tJobId") Long tJobId,
            @RequestParam(value = "viewType", required = false) String viewType) {
        TJob tJob = tJobService.getTJobById(tJobId);
        return new ResponseEntity<TJob>(tJob, HttpStatus.OK);
        // return tJobService.getMappingJacksonValue(tJob, viewType);
    }

    /* ******************* */
    /* **** TJobExecs **** */
    /* ******************* */

    @JsonView(TJobExecCompleteView.class)
    public ResponseEntity<TJobExecution> execTJob(
            @ApiParam(value = "TJob Id.", required = true) @PathVariable("tJobId") Long tJobId,
            @ApiParam(value = "Execution Parameters", required = false) @Valid @RequestBody ExecData parameters) {
        try {
            TJobExecution tJobExec = tJobService.executeTJob(tJobId,
                    parameters.gettJobParams(), parameters.getSutParams(),
                    parameters.getMultiConfigurations());
            return new ResponseEntity<TJobExecution>(tJobExec, HttpStatus.OK);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().equals(HttpStatus.ACCEPTED)) {
                // "Sut instrumented by Elastest is still activating beats";
                return new ResponseEntity<TJobExecution>(e.getStatusCode());
            } else {
                return new ResponseEntity<TJobExecution>(e.getStatusCode());
            }
        }
    }

    @JsonView(TJobExecCompleteView.class)
    public ResponseEntity<Long> deleteTJobExecution(
            @ApiParam(value = "TJob Id.", required = true) @PathVariable("tJobId") Long tJobId,
            @ApiParam(value = "TJob Execution Id.", required = true) @PathVariable("tJobExecId") Long tJobExecId) {

        tJobService.deleteTJobExec(tJobExecId);
        return new ResponseEntity<Long>(tJobExecId, HttpStatus.OK);
    }

    @JsonView(TJobExecCompleteView.class)
    public ResponseEntity<TJobExecution> getTJobExecution(
            @ApiParam(value = "TJob Id.", required = true) @PathVariable("tJobId") Long tJobId,
            @ApiParam(value = "TJob Execution Id.", required = true) @PathVariable("tJobExecId") Long tJobExecId) {

        TJobExecution tJobExec = tJobService.getTJobsExecution(tJobId,
                tJobExecId);
        return new ResponseEntity<TJobExecution>(tJobExec, HttpStatus.OK);
    }

    @JsonView(TJobExecCompleteView.class)
    public ResponseEntity<List<TJobExecution>> getTJobExecutionsByTJob(
            @ApiParam(value = "TJob Id.", required = true) @PathVariable("tJobId") Long tJobId) {

        List<TJobExecution> tjobExecList = tJobService
                .getTJobExecutionsByTJobId(tJobId, false);
        return new ResponseEntity<List<TJobExecution>>(tjobExecList,
                HttpStatus.OK);
    }

    @JsonView(TJobExecCompleteView.class)
    public ResponseEntity<List<TJobExecution>> getTJobExecutionsByTJobWithoutChilds(
            @ApiParam(value = "TJob Id.", required = true) @PathVariable("tJobId") Long tJobId) {

        List<TJobExecution> tjobExecList = tJobService
                .getTJobExecutionsByTJobId(tJobId, true);
        return new ResponseEntity<List<TJobExecution>>(tjobExecList,
                HttpStatus.OK);
    }

    @JsonView(TJobExecCompleteView.class)
    public ResponseEntity<List<TJobExecution>> getLastNTJobExecutions(
            @ApiParam(value = "TJob Id.", required = true) @PathVariable("tJobId") Long tJobId,
            @ApiParam(value = "Number of TJobExecs to get.", required = true) @PathVariable("number") Long number) {
        List<TJobExecution> tjobExecList = tJobService.getLastNTJobExecs(tJobId,
                number);
        return new ResponseEntity<List<TJobExecution>>(tjobExecList,
                HttpStatus.OK);
    }

    /* ****************************** */
    /* *** All execs of all TJobs *** */
    /* ****************************** */

    @Override
    @JsonView(TJobExecCompleteView.class)
    public ResponseEntity<List<TJobExecution>> getAllTJobExecutions() {
        List<TJobExecution> tjobExecList = tJobService.getAllTJobExecs();
        return new ResponseEntity<List<TJobExecution>>(tjobExecList,
                HttpStatus.OK);
    }

    /* *** By ID *** */
    @Override
    @JsonView(TJobExecCompleteView.class)
    public ResponseEntity<List<TJobExecution>> getTJobsExecsRange(
            @RequestParam(value = "from", required = true) int from,
            @RequestParam(value = "to", required = true) int to,
            @RequestParam(value = "withoutChilds", required = true) Boolean withoutChilds) {
        withoutChilds = withoutChilds != null ? withoutChilds : false;

        List<TJobExecution> tjobExecList = tJobService.getTJobsExecsRange(from,
                to, Direction.DESC, withoutChilds);
        return new ResponseEntity<List<TJobExecution>>(tjobExecList,
                HttpStatus.OK);
    }

    @Override
    @JsonView(TJobExecCompleteView.class)
    public ResponseEntity<List<TJobExecution>> getLastNTJobsExecutions(
            @ApiParam(value = "Number of TJobExecs to get.", required = true) @PathVariable("number") Long number,
            @RequestParam(value = "withoutChilds", required = true) Boolean withoutChilds) {
        withoutChilds = withoutChilds != null ? withoutChilds : false;

        List<TJobExecution> tjobExecList = tJobService
                .getLastNTJobsExecs(number, withoutChilds);
        return new ResponseEntity<List<TJobExecution>>(tjobExecList,
                HttpStatus.OK);
    }

    /* *** By results *** */

    @Override
    @JsonView(TJobExecCompleteView.class)
    public ResponseEntity<List<TJobExecution>> getAllRunningTJobsExecutions(
            @RequestParam(value = "withoutChilds", required = true) Boolean withoutChilds) {
        withoutChilds = withoutChilds != null ? withoutChilds : false;

        List<TJobExecution> tjobExecList = tJobService
                .getAllRunningTJobsExecs(withoutChilds);
        return new ResponseEntity<List<TJobExecution>>(tjobExecList,
                HttpStatus.OK);
    }

    @Override
    @JsonView(TJobExecCompleteView.class)
    public ResponseEntity<List<TJobExecution>> getRunningTJobsExecutionsByRange(
            @RequestParam(value = "from", required = true) int from,
            @RequestParam(value = "to", required = true) int to,
            @RequestParam(value = "withoutChilds", required = true) Boolean withoutChilds) {
        withoutChilds = withoutChilds != null ? withoutChilds : false;

        List<TJobExecution> tjobExecList = tJobService
                .getRunningTJobExecsByRange(from, to, Direction.DESC,
                        withoutChilds);
        return new ResponseEntity<List<TJobExecution>>(tjobExecList,
                HttpStatus.OK);
    }

    @Override
    @JsonView(TJobExecCompleteView.class)
    public ResponseEntity<List<TJobExecution>> getLastNRunningTJobsExecutions(
            @ApiParam(value = "Number of TJobExecs to get.", required = true) @PathVariable("number") Long number,
            @RequestParam(value = "withoutChilds", required = true) Boolean withoutChilds) {
        withoutChilds = withoutChilds != null ? withoutChilds : false;

        List<TJobExecution> tjobExecList = tJobService
                .getLastNRunningTJobExecs(number, withoutChilds);
        return new ResponseEntity<List<TJobExecution>>(tjobExecList,
                HttpStatus.OK);
    }

    @Override
    @JsonView(TJobExecCompleteView.class)
    public ResponseEntity<List<TJobExecution>> getAllFinishedOrNotExecutedTJobsExecutions(
            @RequestParam(value = "withoutChilds", required = true) Boolean withoutChilds) {
        withoutChilds = withoutChilds != null ? withoutChilds : false;

        List<TJobExecution> tjobExecList = tJobService
                .getAllFinishedOrNotExecutedTJobExecs(withoutChilds);
        return new ResponseEntity<List<TJobExecution>>(tjobExecList,
                HttpStatus.OK);
    }

    @Override
    @JsonView(TJobExecCompleteView.class)
    public ResponseEntity<List<TJobExecution>> getFinishedOrNotExecutedTJobsExecutionsByRange(
            @RequestParam(value = "from", required = true) int from,
            @RequestParam(value = "to", required = true) int to,
            @RequestParam(value = "withoutChilds", required = true) Boolean withoutChilds) {
        withoutChilds = withoutChilds != null ? withoutChilds : false;
        List<TJobExecution> tjobExecList = tJobService
                .getFinishedOrNotExecutedTJobsExecsByRange(from, to,
                        Direction.DESC, withoutChilds);
        return new ResponseEntity<List<TJobExecution>>(tjobExecList,
                HttpStatus.OK);
    }

    @Override
    @JsonView(TJobExecCompleteView.class)
    public ResponseEntity<List<TJobExecution>> getLastNFinishedOrNotExecutedTJobsExecutions(
            @ApiParam(value = "Number of TJobExecs to get.", required = true) @PathVariable("number") Long number,
            @RequestParam(value = "withoutChilds", required = true) Boolean withoutChilds) {
        withoutChilds = withoutChilds != null ? withoutChilds : false;

        List<TJobExecution> tjobExecList = tJobService
                .getLastNFinishedOrNotExecutedTJobsExecs(number, withoutChilds);
        return new ResponseEntity<List<TJobExecution>>(tjobExecList,
                HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<TJobExecutionFile>> getTJobExecutionFiles(
            @ApiParam(value = "TJobExec Id.", required = true) @PathVariable("tJobExecId") Long tJobExecId,
            @ApiParam(value = "TJob Id.", required = true) @PathVariable("tJobId") Long tJobId) {
        ResponseEntity<List<TJobExecutionFile>> response;

        try {
            response = new ResponseEntity<List<TJobExecutionFile>>(
                    tJobService.getTJobExecutionFilesUrls(tJobId, tJobExecId),
                    HttpStatus.OK);
        } catch (Exception e) {
            response = new ResponseEntity<List<TJobExecutionFile>>(
                    new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return response;
    }

    @Override
    public ResponseEntity<Map<String, Object>> getTJobExecResultStatus(
            @ApiParam(value = "TJob Id.", required = true) @PathVariable("tJobId") Long tJobId,
            @ApiParam(value = "TJob Execution Id.", required = true) @PathVariable("tJobExecId") Long tJobExecId) {
        TJobExecution tJobExec = tJobService.getTJobsExecution(tJobId,
                tJobExecId);
        Map<String, Object> response = new HashMap<>();
        response.put("result", tJobExec.getResult());
        response.put("msg", tJobExec.getResultMsg());

        return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);

    }

    @Override
    @JsonView(TJobExecCompleteView.class)
    public ResponseEntity<TJobExecution> stopTJobExecution(
            @ApiParam(value = "Id of a TJob.", required = true) @PathVariable("tJobId") Long tJobId,
            @ApiParam(value = "TJob Execution Id associatd for a given TJob Id.", required = true) @PathVariable("tJobExecId") Long tJobExecId) {
        TJobExecution tJobExec = tJobService.stopTJobExec(tJobExecId);
        return new ResponseEntity<TJobExecution>(tJobExec, HttpStatus.OK);
    }

    @Override
    @JsonView(TJobExecCompleteView.class)
    public ResponseEntity<TJobExecution> getChildTJobExecParent(
            @ApiParam(value = "TJobExec Id.", required = true) @PathVariable("tJobExecId") Long tJobExecId) {
        TJobExecution tJobExec = tJobService.getChildTJobExecParent(tJobExecId);
        return new ResponseEntity<TJobExecution>(tJobExec, HttpStatus.OK);
    }

    @Override
    @JsonView(TJobExecCompleteView.class)
    public ResponseEntity<List<TJobExecution>> getParentTJobExecChilds(
            @ApiParam(value = "TJobExec Id.", required = true) @PathVariable("tJobExecId") Long tJobExecId) {
        List<TJobExecution> tJobExec = tJobService
                .getParentTJobExecChilds(tJobExecId);
        return new ResponseEntity<List<TJobExecution>>(tJobExec, HttpStatus.OK);
    }

}
