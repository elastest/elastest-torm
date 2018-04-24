package io.elastest.etm.api;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import io.elastest.etm.service.PcapService;
import io.swagger.annotations.ApiParam;

@Controller
public class PcapApiController implements PcapApi {
    @Autowired
    PcapService pcapService;

    public ResponseEntity<Boolean> startPcap(
            @ApiParam(value = "Execution Id", required = true) @Valid @RequestBody String execId) {
        Boolean started = pcapService.startPcap(execId);
        return new ResponseEntity<Boolean>(started, HttpStatus.OK);
    }

    public void stopPcap(
            @ApiParam(value = "Execution Id.", required = true) @PathVariable("execId") String execId,
            HttpServletResponse response) {
        response.setStatus(HttpStatus.OK.value());
        try {
            pcapService.stopContainerAndSendFileTo(execId,
                    response.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ResponseEntity<String> getPcapContainerName(
            @ApiParam(value = "Execution Id.", required = true) @PathVariable("execId") String execId) {
        return new ResponseEntity<>(
                this.pcapService.getPcapContainerName(execId), HttpStatus.OK);
    }

}
