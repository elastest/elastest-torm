package io.elastest.etm.test.utils;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class RestClient {

    private String server;
    private RestTemplate rest;
    private HttpHeaders headers;
    private HttpStatus status;

    public RestClient(String server) {
        this.server = server;
        this.rest = new RestTemplate();
        this.headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        headers.add("Accept", "*/*");
    }

    public ResponseEntity<String> get(String uri) {
        HttpEntity<String> requestEntity = new HttpEntity<String>("", headers);
        ResponseEntity<String> responseEntity = rest.exchange(server + uri,
                HttpMethod.GET, requestEntity, String.class);
        this.setStatus(responseEntity.getStatusCode());
        return responseEntity;
    }

    public ResponseEntity<String> post(String uri, String json) {
        HttpEntity<String> requestEntity = new HttpEntity<String>(json,
                headers);
        ResponseEntity<String> responseEntity = rest.exchange(server + uri,
                HttpMethod.POST, requestEntity, String.class);
        this.setStatus(responseEntity.getStatusCode());
        return responseEntity;
    }

    public ResponseEntity<String> put(String uri, String json) {
        HttpEntity<String> requestEntity = new HttpEntity<String>(json,
                headers);
        ResponseEntity<String> responseEntity = rest.exchange(server + uri,
                HttpMethod.PUT, requestEntity, String.class);
        this.setStatus(responseEntity.getStatusCode());

        return responseEntity;
    }

    public ResponseEntity<String> delete(String uri) {
        HttpEntity<String> requestEntity = new HttpEntity<String>("", headers);
        ResponseEntity<String> responseEntity = rest.exchange(server + uri,
                HttpMethod.DELETE, requestEntity, String.class);
        this.setStatus(responseEntity.getStatusCode());
        return responseEntity;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public void setStatus(HttpStatus status) {
        this.status = status;
    }
}