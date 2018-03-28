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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.http.HttpHost;
import org.elasticsearch.ElasticsearchStatusException;
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
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
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

    /* ************* */
    /* *** Index *** */
    /* ************* */

    public void createMonitoringIndex(String[] indicesList) {
        boolean hasFailures = false;
        logger.info("Creating ES indices...");
        for (String index : indicesList) {
            logger.info("Creating index: {}", index);
            String type = "_doc";

            Map<String, String> mappings = new HashMap<>();
            mappings.put(type,
                    "{ \"" + type + "\": { \"properties\": {"
                            + "\"component\": { \"type\": \"text\", \"fielddata\": true, \"fields\": { \"keyword\": { \"type\": \"keyword\" } } },"
                            + "\"stream\": { \"type\": \"text\", \"fielddata\": true, \"fields\": { \"keyword\": { \"type\": \"keyword\" } } },"
                            + "\"level\": { \"type\": \"text\", \"fielddata\": true, \"fields\": { \"keyword\": { \"type\": \"keyword\" } } },"
                            + "\"et_type\": { \"type\": \"text\", \"fielddata\": true, \"fields\": { \"keyword\": { \"type\": \"keyword\" } } }"
                            + "} }" + "}");
            try {
                this.createIndexSync(index, mappings, null, null);
                logger.info("Index {} created", index);
            } catch (ElasticsearchStatusException e) {
                if (e.getMessage()
                        .contains("resource_already_exists_exception")) {
                    logger.info("ES Index {} already exist!", index);
                }
            } catch (Exception e) {
                hasFailures = true;
                logger.error("Error creating index {}", index, e);
            }
        }
        if (hasFailures) {
            logger.info("Create ES indices finished with some errors");
        } else {
            logger.info("Create ES indices finished!");
        }
    }

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
            throws Exception {
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
        sourceBuilder.sort(new FieldSortBuilder("_id").order(SortOrder.ASC));

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

    public Date findFirstMsgAndGetTimestamp(String index, String msg,
            String component) throws IOException, ParseException {
        SearchResponse response = this.findMessageSync(index, msg, component);
        SearchHits hits = response.getHits();
        if (hits != null && hits.getTotalHits() > 0) {
            SearchHit firstResult = hits.getAt(0);
            String timestamp = firstResult.getSourceAsMap().get("@timestamp")
                    .toString();

            DateFormat df = new SimpleDateFormat(
                    "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            Date date = df.parse(timestamp);

            return date;
        }

        return null;
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
