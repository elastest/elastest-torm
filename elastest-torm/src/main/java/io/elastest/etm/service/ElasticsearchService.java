package io.elastest.etm.service;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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
import org.elasticsearch.action.support.IndicesOptions;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.elastest.etm.model.LogTrace;
import io.elastest.etm.model.MonitoringQuery;

@Service
public class ElasticsearchService {
    final Logger logger = getLogger(lookup().lookupClass());

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

    public void createMonitoringIndex(String[] indicesList) {
        boolean hasFailures = false;
        logger.info("Creating ES indices...");
        for (String index : indicesList) {
            logger.info("Creating index: {}", index);
            String type = "_doc";

            Map<String, String> mappings = new HashMap<>();
            mappings.put(type, "{ \"" + type + "\": { \"properties\": {"
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

    public List<SearchHit> searchAll(SearchRequest searchRequest)
            throws IOException {
        List<SearchHit> hits = new ArrayList<>();

        SearchResponseHitsIterator hitsIterator = new SearchResponseHitsIterator(
                searchRequest);

        while (hitsIterator.hasNext()) {
            List<SearchHit> currentHits = Arrays.asList(hitsIterator.next());
            hits.addAll(currentHits);
        }

        return hits;
    }

    public SearchResponse searchBySearchSourceBuilder(
            SearchSourceBuilder searchSourceBuilder, String[] indices)
            throws IOException {

        SearchRequest searchRequest = new SearchRequest(indices);
        searchRequest.source(searchSourceBuilder);
        searchRequest.indicesOptions(
                IndicesOptions.fromOptions(true, false, false, false));
        searchRequest.indicesOptions(
                IndicesOptions.fromOptions(true, false, false, false));
        return this.esClient.search(searchRequest);
    }

    /* ************** */
    /* **** Logs **** */
    /* ************** */

    public List<LogTrace> searchAllLogs(SearchRequest request) {
        List<LogTrace> logTraces = new ArrayList<>();
        SearchHitIterator hitIterator = new SearchHitIterator(request);

        while (hitIterator.hasNext()) {
            SearchHit currentHit = hitIterator.next();
            try {
                String message = currentHit.getSourceAsMap().get("message")
                        .toString();
                String timestamp = currentHit.getSourceAsMap().get("@timestamp")
                        .toString();
                if (message != null) {
                    LogTrace trace = new LogTrace();
                    trace.setMessage(message);
                    trace.setTimestamp(timestamp);
                    logTraces.add(trace);
                }
            } catch (Exception e) {
            }
        }
        return logTraces;
    }

    public List<LogTrace> searchLog(MonitoringQuery body) throws IOException {
        BoolQueryBuilder boolQueryBuilder = getLogBoolQueryBuilder(
                body.getComponent(), body.getStream());

        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(boolQueryBuilder);
        sourceBuilder.size(10000);
        sourceBuilder
                .sort(new FieldSortBuilder("@timestamp").order(SortOrder.ASC));
        sourceBuilder.sort(new FieldSortBuilder("_id").order(SortOrder.ASC));

        SearchRequest searchRequest = new SearchRequest(body.getIndices()
                .toArray(new String[body.getIndices().size()]));
        searchRequest.source(sourceBuilder);
        searchRequest.indicesOptions(
                IndicesOptions.fromOptions(true, false, false, false));

        return this.searchAllLogs(searchRequest);
    }

    public BoolQueryBuilder getLogBoolQueryBuilder(String component,
            String stream) {
        BoolQueryBuilder componentStreamBoolBuilder = QueryBuilders.boolQuery();
        TermQueryBuilder componentTerm = QueryBuilders.termQuery("component",
                component);
        TermQueryBuilder streamTerm = QueryBuilders.termQuery("stream", stream);

        componentStreamBoolBuilder.must(componentTerm);
        componentStreamBoolBuilder.must(streamTerm);

        TermQueryBuilder streamTypeTerm = QueryBuilders.termQuery("stream_type",
                "log");

        BoolQueryBuilder shouldBoolBuilder = QueryBuilders.boolQuery();
        shouldBoolBuilder.should(componentStreamBoolBuilder);

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must(streamTypeTerm);
        boolQueryBuilder.must(shouldBoolBuilder);

        return boolQueryBuilder;
    }

    public SearchRequest getFindMessageSearchRequest(String index, String msg,
            String component, String stream) {

        MatchPhrasePrefixQueryBuilder messageMatchTerm = QueryBuilders
                .matchPhrasePrefixQuery("message", msg);

        BoolQueryBuilder boolQueryBuilder = getLogBoolQueryBuilder(component,
                stream);
        boolQueryBuilder.must(messageMatchTerm);

        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(boolQueryBuilder);
        sourceBuilder.size(10000);
        sourceBuilder
                .sort(new FieldSortBuilder("@timestamp").order(SortOrder.ASC));
        sourceBuilder.sort(new FieldSortBuilder("_id").order(SortOrder.ASC));

        SearchRequest searchRequest = new SearchRequest(index);
        searchRequest.source(sourceBuilder);
        searchRequest.indicesOptions(
                IndicesOptions.fromOptions(true, false, false, false));
        return searchRequest;
    }

    public SearchResponse findMessageSync(String index, String msg,
            String component) throws IOException {
        SearchRequest searchRequest = this.getFindMessageSearchRequest(index,
                msg, component, "default_log");
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
    /* ************** */
    /* * Exceptions * */
    /* ************** */

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

    /* ************** */
    /* *** Others *** */
    /* ************** */

    public class SearchHitIterator implements Iterator<SearchHit> {

        private final SearchRequest initialRequest;

        private SearchHit[] currentPageResults;
        private int currentResultIndex;
        private SearchResponse currentPageResponse;

        public SearchHitIterator(SearchRequest initialRequest) {
            this.initialRequest = initialRequest;
            this.currentResultIndex = -1;
        }

        @Override
        public boolean hasNext() {
            if (currentPageResults == null
                    || currentResultIndex + 1 >= currentPageResults.length) {

                // If is not first search
                if (currentPageResponse != null) {
                    SearchSourceBuilder source = initialRequest.source();
                    if (currentPageResponse.getHits() != null
                            && currentPageResponse.getHits().getHits() != null
                            && currentPageResponse.getHits()
                                    .getHits().length > 0) {

                        SearchHit hit = currentPageResponse.getHits()
                                .getHits()[currentPageResponse.getHits()
                                        .getHits().length - 1];

                        source.searchAfter(hit.getSortValues());
                        initialRequest.source(source);
                    }
                }

                try {
                    currentPageResponse = esClient.search(initialRequest);
                } catch (IOException e) {
                    return false;
                }

                currentPageResults = currentPageResponse.getHits().getHits();

                if (currentPageResults.length < 1)
                    return false;

                currentResultIndex = -1;
            }

            return true;
        }

        @Override
        public SearchHit next() {
            if (!hasNext())
                return null;

            currentResultIndex++;
            return currentPageResults[currentResultIndex];
        }

    }

    public class SearchResponseHitsIterator implements Iterator<SearchHit[]> {
        private final SearchRequest initialRequest;
        private SearchResponse currentPageResponse;

        public SearchResponseHitsIterator(SearchRequest initialRequest)
                throws IOException {
            this.initialRequest = initialRequest;
        }

        @Override
        public boolean hasNext() {
            // If is not first search
            if (currentPageResponse != null) {
                SearchSourceBuilder source = initialRequest.source();
                if (currentPageResponse.getHits() != null
                        && currentPageResponse.getHits().getHits() != null
                        && currentPageResponse.getHits().getHits().length > 0) {

                    SearchHit hit = currentPageResponse.getHits()
                            .getHits()[currentPageResponse.getHits()
                                    .getHits().length - 1];

                    source.searchAfter(hit.getSortValues());
                    initialRequest.source(source);
                }
            }
            SearchResponse response = null;
            try {
                response = esClient.search(initialRequest);
            } catch (IOException e) {
                return false;
            }

            if (response == null || response.getHits() == null
                    || response.getHits().getHits() == null
                    || response.getHits().getHits().length == 0) {
                return false;
            }

            return true;
        }

        @Override
        public SearchHit[] next() {
            if (!hasNext()) {
                return new SearchHit[0];
            }

            try {
                currentPageResponse = esClient.search(initialRequest);
            } catch (IOException e) {
                return new SearchHit[0];
            }

            if (currentPageResponse.getHits() != null
                    && currentPageResponse.getHits().getHits() != null) {

                return currentPageResponse.getHits().getHits();
            } else {
                return new SearchHit[0];
            }
        }

    }
}
