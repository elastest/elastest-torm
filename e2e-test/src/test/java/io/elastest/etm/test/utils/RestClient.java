package io.elastest.etm.test.utils;

import java.nio.charset.StandardCharsets;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class RestClient {

    private String server;
    private RestTemplate rest;
    private HttpHeaders headers;

    public RestClient(String server, String user, String password,
            boolean secureElastest) {
        this.server = server;
        this.rest = new RestTemplate();
        this.headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        headers.add("Accept", "*/*");
        if (secureElastest) {
            String auth = Base64.encodeBase64String(
                    (user + ":" + StringUtils.defaultString(password))
                            .getBytes(StandardCharsets.UTF_8));
            headers.add("Authorization", "Basic " + auth);            
        }        
    }

    public ResponseEntity<String> get(String uri) {
        HttpEntity<String> requestEntity = new HttpEntity<String>("", headers);
        ResponseEntity<String> responseEntity = rest.exchange(server + uri,
                HttpMethod.GET, requestEntity, String.class);
        return responseEntity;
    }

    public ResponseEntity<String> post(String uri, String json) {
        HttpEntity<String> requestEntity = new HttpEntity<String>(json,
                headers);
        ResponseEntity<String> responseEntity = rest.exchange(server + uri,
                HttpMethod.POST, requestEntity, String.class);
        return responseEntity;
    }

    public ResponseEntity<String> put(String uri, String json) {
        HttpEntity<String> requestEntity = new HttpEntity<String>(json,
                headers);
        ResponseEntity<String> responseEntity = rest.exchange(server + uri,
                HttpMethod.PUT, requestEntity, String.class);

        return responseEntity;
    }

    public ResponseEntity<String> delete(String uri) {
        HttpEntity<String> requestEntity = new HttpEntity<String>("", headers);
        ResponseEntity<String> responseEntity = rest.exchange(server + uri,
                HttpMethod.DELETE, requestEntity, String.class);
        return responseEntity;
    }
}