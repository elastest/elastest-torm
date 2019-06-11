package io.elastest.etm.utils;

import java.nio.charset.StandardCharsets;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class FixedUrlRestClient {

    private String url;
    private RestTemplate rest;
    private HttpHeaders headers;

    public FixedUrlRestClient(String url) {
        this.url = url;
        this.rest = new RestTemplate();
        this.headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        headers.add("Accept", "*/*");
    }

    public FixedUrlRestClient(String url, String user, String password) {
        this(url);
        headers.add("Content-Type", "application/json");
        headers.add("Accept", "*/*");
        String auth = Base64.encodeBase64String(
                (user + ":" + StringUtils.defaultString(password))
                        .getBytes(StandardCharsets.UTF_8));
        headers.add("Authorization", "Basic " + auth);
    }

    public ResponseEntity<String> get() {
        HttpEntity<String> requestEntity = new HttpEntity<String>("", headers);
        ResponseEntity<String> responseEntity = rest.exchange(url,
                HttpMethod.GET, requestEntity, String.class);
        return responseEntity;
    }

    public ResponseEntity<String> post(String json) {
        HttpEntity<String> requestEntity = new HttpEntity<String>(json,
                headers);
        ResponseEntity<String> responseEntity = rest.exchange(url,
                HttpMethod.POST, requestEntity, String.class);
        return responseEntity;
    }

    public ResponseEntity<String> put(String json) {
        HttpEntity<String> requestEntity = new HttpEntity<String>(json,
                headers);
        ResponseEntity<String> responseEntity = rest.exchange(url,
                HttpMethod.PUT, requestEntity, String.class);

        return responseEntity;
    }

    public ResponseEntity<String> delete() {
        HttpEntity<String> requestEntity = new HttpEntity<String>("", headers);
        ResponseEntity<String> responseEntity = rest.exchange(url,
                HttpMethod.DELETE, requestEntity, String.class);
        return responseEntity;
    }
}