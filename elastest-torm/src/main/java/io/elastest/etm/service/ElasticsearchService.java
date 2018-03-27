package io.elastest.etm.service;

import org.springframework.beans.factory.annotation.Value;
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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.elastest.etm.utils.ParserService;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.http.HttpHost;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.indices.alias.Alias;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchPhrasePrefixQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ElasticsearchService {
    private static final Logger logger = LoggerFactory
            .getLogger(ElasticsearchService.class);

    @Value("${et.edm.elasticsearch.api}")
    private String esApiUrl;

    private String host;
    private int port;

    RestHighLevelClient esClient;

    public ElasticsearchService() {
    }

    @PostConstruct
    private void init() {
        URL url;
        try {
            url = new URL(this.esApiUrl);
            this.host = url.getHost();
            this.port = url.getPort();
            logger.debug("Elasticsearch API host: {} / port: {}", this.host,
                    this.port);
            this.esClient = new RestHighLevelClient(RestClient
                    .builder(new HttpHost(this.host, this.port, "http")));

        } catch (MalformedURLException e) {
            logger.error("Cannot get Elasticsearch url by given: {}",
                    this.esApiUrl);
        }

    }

    /* ************* */
    /* *** Index *** */
    /* ************* */

    public CreateIndexRequest createIndexRequest(String index,
            Map<String, String> mappings, String alias, String timeout) {
        CreateIndexRequest request = new CreateIndexRequest(index);

        if (mappings != null && !mappings.isEmpty()) {
            for (Map.Entry<String, String> entry : mappings.entrySet()) {
                request.mapping(entry.getKey(), entry.getValue(),
                        XContentType.JSON);
            }
        }

        if (alias != null) {
            request.alias(new Alias(alias));
        }

        if (timeout != null && !timeout.isEmpty()) {
            request.timeout(timeout);
        }

        return request;
    }

    public CreateIndexResponse createIndexSync(String index,
            Map<String, String> mappings, String alias, String timeout)
            throws IOException {
        CreateIndexRequest request = this.createIndexRequest(index, mappings,
                alias, timeout);

        return this.esClient.indices().create(request);
    }

    public void createIndexAsync(ActionListener<CreateIndexResponse> listener,
            String index, Map<String, String> mappings, String alias,
            String timeout) throws IOException {
        CreateIndexRequest request = this.createIndexRequest(index, mappings,
                alias, timeout);

        this.esClient.indices().createAsync(request, listener);
    }

    /* ************** */
    /* *** Search *** */
    /* ************** */

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

    public void enablePropertiesGroupFieldData(String url, String type,
            String[] fields) {
        url = url + "/_mapping/" + type;

        String properties = "";
        int counter = 0;
        for (String field : fields) {
            properties += "\"" + field + "\": {"
                    + "\"type\": \"text\", \"fielddata\": true" + "}";
            if (counter < fields.length - 1) {
                properties += ",";
            }
            counter++;
        }

        String body = "{" + "\"properties\": {" + properties + "}" + "}";
        try {
            putCall(url, body);
        } catch (IndexAlreadyExistException e) {
        } catch (RestClientException e) {
        }
    }

    public ResponseEntity<String> putCall(String url, String body)
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
            return responseEntity;
        } catch (RestClientException e) {
            throw e;
        }
    }

    public ResponseEntity<String> postCall(String url, String body)
            throws IndexAlreadyExistException, RestClientException {
        RestTemplate restTemplate = new RestTemplate(
                clientHttpRequestFactory(5000, 5000));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<String>(body, headers);

        try {
            ResponseEntity<String> responseEntity = restTemplate.exchange(url,
                    HttpMethod.POST, request, String.class);

            return responseEntity;
        } catch (RestClientException e) {
            throw e;
        }
    }

    public ClientHttpRequestFactory clientHttpRequestFactory(int readTimeout,
            int connectTimeout) {
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setReadTimeout(readTimeout);
        factory.setConnectTimeout(connectTimeout);
        return factory;
    }

    public ObjectNode findMessage(String url, String msg, String component)
            throws RestClientException, IndexAlreadyExistException,
            IOException {
        String body = "{\"size\":10000,\"query\":{\"bool\":{\"must\":[{\"term\":{\"stream_type\":\"log\"}},{\"bool\":{\"should\":[{\"bool\":{\"must\":[{\"term\":{\"component\":\""
                + component
                + "\"}},{\"term\":{\"stream\":\"default_log\"}}]}}]}},{\"match\":{\"message\":{\"query\":\"*"
                + msg
                + "*\",\"type\":\"phrase_prefix\"}}}]}},\"sort\":[{\"@timestamp\":\"asc\"},{\"_uid\":\"asc\"}]}";

        return this.responseAsObjectNode(this.postCall(url, body).getBody());
    }

    public SearchRequest getFindMessageSearchRequest(String index, String msg,
            String component) {
        BoolQueryBuilder componentStreamBoolBuilder = QueryBuilders.boolQuery();
        TermQueryBuilder componentTerm = QueryBuilders.termQuery("component",
                component);
        TermQueryBuilder streamTerm = QueryBuilders.termQuery("stream",
                "default_log");

        componentStreamBoolBuilder.must(componentTerm);
        componentStreamBoolBuilder.must(streamTerm);

        TermQueryBuilder streamTypeTerm = QueryBuilders.termQuery("stream_type",
                "log");
        MatchPhrasePrefixQueryBuilder messageMatchTerm = QueryBuilders
                .matchPhrasePrefixQuery("message", msg);

        BoolQueryBuilder shouldBoolBuilder = QueryBuilders.boolQuery();
        shouldBoolBuilder.should(componentStreamBoolBuilder);

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must(streamTypeTerm);
        boolQueryBuilder.must(messageMatchTerm);
        boolQueryBuilder.must(shouldBoolBuilder);

        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(boolQueryBuilder);
        sourceBuilder.size(10000);
        sourceBuilder
                .sort(new FieldSortBuilder("@timestamp").order(SortOrder.ASC));
        sourceBuilder.sort(new FieldSortBuilder("_uid").order(SortOrder.ASC));

        SearchRequest searchRequest = new SearchRequest(index);
        searchRequest.source(sourceBuilder);

        return searchRequest;
    }

    public SearchResponse findMessageSync(String index, String msg,
            String component) throws IOException {
        SearchRequest searchRequest = this.getFindMessageSearchRequest(index,
                msg, component);
        return this.esClient.search(searchRequest);
    }

    public Date findFirstMsgAndGetTimestamp(String url, String msg,
            String component) {
        try {
            ObjectNode responseMsg = this.findMessage(url, msg, component);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode hits = responseMsg.get("hits");
            if (hits != null) {
                JsonNode hitsHits = hits.get("hits");
                if (hitsHits != null && hitsHits.isArray()) {
                    List<JsonNode> hitsList = mapper.convertValue(hitsHits,
                            ArrayList.class);
                    if (hitsList.size() > 0) {
                        JsonNode firstResult = hitsList.get(0);
                        if (firstResult != null) {
                            JsonNode source = firstResult.get("_source");
                            if (source != null) {
                                String timestamp = source.get("@timestamp")
                                        .toString();

                                DateFormat df = new SimpleDateFormat(
                                        "yyyy-MM-dd'T'HH:mm:ss.SSSZ");
                                Date date = df.parse(timestamp);

                                return date;
                            }
                        }
                    }
                }
            }
        } catch (RestClientException e) {

        } catch (IndexAlreadyExistException e) {

        } catch (IOException e) {
        } catch (ParseException e) {

        }
        return null;
    }

    public ObjectNode responseAsObjectNode(String response) throws IOException {
        return ParserService.fromStringToJson(response);
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
