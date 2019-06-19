package io.elastest.etm.prometheus.client;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import io.elastest.etm.prometheus.client.PrometheusApiResponse.PrometheusApiResponseStatus;

public class PrometheusApiClient {
    protected final Logger logger = getLogger(lookup().lookupClass());

    private static final ParameterizedTypeReference<PrometheusApiResponse<Object>> responseTypeDataObject = new ParameterizedTypeReference<PrometheusApiResponse<Object>>() {
    };

    private static final ParameterizedTypeReference<PrometheusApiResponse<PrometheusQueryData>> responseTypeDataQuery = new ParameterizedTypeReference<PrometheusApiResponse<PrometheusQueryData>>() {
    };

    private static final ParameterizedTypeReference<String> responseString = new ParameterizedTypeReference<String>() {
    };

    // Endpoint
    private static final String HEALTH_PATH = "/-/healthy";
    private static final String READY_PATH = "/-/ready";

    // Api
    private static final String API_PATH = "/api/v1/";
    private static final String LABEL_NAMES_PATH = "label/__name__/values";
    private static final String QUERY_PATH = "query?query=";
    private static final String QUERY_RANGE_PATH = "query_range?query=";

    private String server;
    private RestTemplate rest;
    private HttpHeaders headers;

    public PrometheusApiClient(String server) {
        this.server = server;
        this.rest = new RestTemplate();
        this.headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        headers.add("Accept", "*/*");
    }

    public PrometheusApiClient(String server, String user, String password) {
        this(server);
        headers.add("Content-Type", "application/json");
        headers.add("Accept", "*/*");
        String auth = Base64.encodeBase64String(
                (user + ":" + StringUtils.defaultString(password))
                        .getBytes(StandardCharsets.UTF_8));
        headers.add("Authorization", "Basic " + auth);
    }

    /* *************************** */
    /* ********* Methods ********* */
    /* *************************** */

    @SuppressWarnings("unchecked")
    public List<String> getAllLabelNames() {
        List<String> labelNames = new ArrayList<>();

        ResponseEntity<PrometheusApiResponse<Object>> response = this
                .get(API_PATH + LABEL_NAMES_PATH, responseTypeDataObject);

        logger.debug(
                "getAllLabelNames ResponseEntity<PrometheusApiResponse>: {}",
                response);

        if (response != null && response.getStatusCode() == HttpStatus.OK) {
            PrometheusApiResponse<Object> prometheusResponse = response
                    .getBody();

            if (prometheusResponse != null
                    && PrometheusApiResponseStatus.SUCCESS.toString()
                            .equals(prometheusResponse.getStatus())) {

                if (prometheusResponse.getData() != null) {
                    labelNames = (List<String>) prometheusResponse.getData();
                }
            }

        }

        return labelNames;
    }

    /* *** Query *** */

    public PrometheusApiResponse<PrometheusQueryData> executeQuery(
            String jsonOrMetricName, Double time, Double timeout) {

        String uri = QUERY_PATH + jsonOrMetricName;

        if (time != null) {
            uri += "&time=" + time;
        }

        if (timeout != null) {
            uri += "&timeout=" + timeout;
        }

        ResponseEntity<PrometheusApiResponse<PrometheusQueryData>> response = this
                .get(API_PATH + uri, responseTypeDataQuery);

        if (response != null) {
            return response.getBody();
        }

        return null;
    }

    public PrometheusApiResponse<PrometheusQueryData> executeQuery(
            String jsonOrMetricName) {
        return executeQuery(jsonOrMetricName, null, null);
    }

    /* *** Query Range *** */

    public PrometheusApiResponse<PrometheusQueryData> executeQueryRange(
            String jsonOrMetricName, Double start, Double end, Integer step,
            Double timeout) {

        String uri = QUERY_RANGE_PATH + jsonOrMetricName + "&start=" + start
                + "&end=" + end;

        if (step != null) {
            uri += "&step=" + step;
        }

        if (step != null) {
            uri += "&timeout=" + timeout;
        }

        ResponseEntity<PrometheusApiResponse<PrometheusQueryData>> response = this
                .get(API_PATH + uri, responseTypeDataQuery);

        if (response != null) {
            return response.getBody();
        }

        return null;
    }

    public PrometheusApiResponse<PrometheusQueryData> executeQueryRange(
            String jsonOrMetricName, Double start, Double end) {
        return executeQueryRange(jsonOrMetricName, start, end, null, null);
    }

    public ResponseEntity<String> getHealthy() {
        return get(HEALTH_PATH, responseString);
    }

    public boolean isHealthy() {
        ResponseEntity<String> response = getHealthy();
        return response.getStatusCode() == HttpStatus.OK
                && "Prometheus is Healthy.".equals(response.getBody().trim());
    }

    public ResponseEntity<String> getReady() {
        return get(READY_PATH, responseString);
    }

    public boolean isReady() {
        ResponseEntity<String> response = getReady();
        return response.getStatusCode() == HttpStatus.OK
                && "Prometheus is Ready.".equals(response.getBody().trim());
    }

    /* ********************************************** */
    /* **************** Rest Methods **************** */
    /* ********************************************** */

    private <T> ResponseEntity<T> get(String uri,
            ParameterizedTypeReference<T> responseType) {
        HttpEntity<String> requestEntity = new HttpEntity<String>("", headers);
        ResponseEntity<T> responseEntity = rest.exchange(server + uri,
                HttpMethod.GET, requestEntity, responseType);
        return responseEntity;
    }

    private <T> ResponseEntity<T> post(String uri, String json,
            ParameterizedTypeReference<T> responseType) {
        HttpEntity<String> requestEntity = new HttpEntity<String>(json,
                headers);
        ResponseEntity<T> responseEntity = rest.exchange(server + uri,
                HttpMethod.POST, requestEntity, responseType);
        return responseEntity;
    }

    private <T> ResponseEntity<T> put(String uri, String json,
            ParameterizedTypeReference<T> responseType) {
        HttpEntity<String> requestEntity = new HttpEntity<String>(json,
                headers);
        ResponseEntity<T> responseEntity = rest.exchange(server + uri,
                HttpMethod.PUT, requestEntity, responseType);

        return responseEntity;
    }

    private <T> ResponseEntity<T> delete(String uri,
            ParameterizedTypeReference<T> responseType) {
        HttpEntity<String> requestEntity = new HttpEntity<String>("", headers);
        ResponseEntity<T> responseEntity = rest.exchange(server + uri,
                HttpMethod.DELETE, requestEntity, responseType);
        return responseEntity;
    }
}
