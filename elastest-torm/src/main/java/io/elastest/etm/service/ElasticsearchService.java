package io.elastest.etm.service;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class ElasticsearchService {

    public void enableFieldData(String url, String field) {
        String group = field + 's';
        url = url + "/_mapping/" + group;

        String body = "{" + "\"properties\": {" + "\"" + field + "\": {"
                + "\"type\": \"text\", \"fielddata\": true" + "}" + "}" + "}";
        try {
            putCall(url, body);
        } catch (IndexAlreadyExistException e) {
        } catch (RestClientException e) {
        }
    }

    public void putCall(String url, String body)
            throws IndexAlreadyExistException, RestClientException {
        RestTemplate restTemplate = new RestTemplate(
                clientHttpRequestFactory(5000, 5000));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<String>(body, headers);

        try {
            ResponseEntity<String> responseEntity = restTemplate.exchange(url,
                    HttpMethod.PUT, request, String.class);

            HttpStatus statusCode = responseEntity.getStatusCode();

            if (!HttpStatus.OK.equals(statusCode)) {
                throw new IndexAlreadyExistException();
            }
        } catch (RestClientException e) {
            throw e;
        }
    }

    private ClientHttpRequestFactory clientHttpRequestFactory(int readTimeout,
            int connectTimeout) {
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setReadTimeout(readTimeout);
        factory.setConnectTimeout(connectTimeout);
        return factory;
    }

    /****************/
    /** Exceptions **/
    /****************/

    public class IndexAlreadyExistException extends Exception {

        private static final long serialVersionUID = -5156214804587007246L;

        public IndexAlreadyExistException() {
        }

        public IndexAlreadyExistException(String message) {
            super(message);
        }

        public IndexAlreadyExistException(Throwable cause) {
            super(cause);
        }

        public IndexAlreadyExistException(String message, Throwable cause) {
            super(message, cause);
        }

    }
}
