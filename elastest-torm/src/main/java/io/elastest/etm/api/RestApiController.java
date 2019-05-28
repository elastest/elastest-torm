package io.elastest.etm.api;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import io.elastest.etm.utils.RestClient;
import io.swagger.annotations.ApiParam;

@Controller
public class RestApiController implements RestApi {
    private static final Logger logger = LoggerFactory
            .getLogger(RestApiController.class);

    @Override
    public ResponseEntity<String> provisionServiceInstance(
            @ApiParam(value = "method", required = true) @PathVariable(value = "method", required = false) String method,
            @ApiParam(value = "Body in Json String format", required = false) @Valid @RequestBody String jsonBody,
            HttpServletRequest request) {
        String requestURL = request.getRequestURL().toString();
        String[] splittedRequestURL = requestURL.split(method + "/");
        String url = splittedRequestURL[1];

        logger.debug("url {}", url);

        RestClient restClient = new RestClient(url);

        if ("post".equals(method)) {
            return restClient.post(jsonBody);
        } else if ("get".equals(method)) {
            return restClient.get();
        } else if ("delete".equals(method)) {
            return restClient.delete();
        } else if ("put".equals(method)) {
            return restClient.put(jsonBody);
        } else {
            return new ResponseEntity<String>(
                    "Method " + method + " not allowed",
                    HttpStatus.METHOD_NOT_ALLOWED);
        }

    }

}
