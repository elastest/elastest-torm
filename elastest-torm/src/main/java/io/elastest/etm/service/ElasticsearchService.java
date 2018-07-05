package io.elastest.etm.service;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.validation.Valid;

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
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms.Bucket;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;

import io.elastest.etm.model.AggregationTree;
import io.elastest.etm.model.LogAnalyzerQuery;
import io.elastest.etm.model.MonitoringQuery;
import io.elastest.etm.utils.UtilTools;
import io.elastest.etm.utils.UtilsService;

//@Service
public class ElasticsearchService implements MonitoringServiceInterface {
    final Logger logger = getLogger(lookup().lookupClass());

    private final UtilsService utilsService;

    @Value("${et.edm.elasticsearch.api}")
    private String esApiUrl;

    private String host;
    private int port;

    RestHighLevelClient esClient;

    public ElasticsearchService(UtilsService utilsService) {
        this.utilsService = utilsService;
    }

    @PostConstruct
    private void init() {
        URL url;
        try {
            url = new URL(this.esApiUrl);
            this.host = url.getHost();
            this.port = url.getPort();

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

    /* ********************* */
    /* *** Search Config *** */
    /* ********************* */

    public SearchSourceBuilder getDefaultSearchSourceBuilderByGivenBoolQueryBuilder(
            BoolQueryBuilder boolQueryBuilder) {
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(boolQueryBuilder);
        sourceBuilder.size(10000);
        sourceBuilder
                .sort(new FieldSortBuilder("@timestamp").order(SortOrder.ASC));
        sourceBuilder.sort(new FieldSortBuilder("_id").order(SortOrder.ASC));

        return sourceBuilder;

    }

    public SearchSourceBuilder getDefaultInverseSearchSourceBuilderByGivenBoolQueryBuilder(
            BoolQueryBuilder boolQueryBuilder) {
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(boolQueryBuilder);
        sourceBuilder.size(10000);
        sourceBuilder
                .sort(new FieldSortBuilder("@timestamp").order(SortOrder.DESC));
        sourceBuilder.sort(new FieldSortBuilder("_id").order(SortOrder.DESC));

        return sourceBuilder;
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

    public List<SearchHit> searchAllHits(SearchRequest request) {
        List<SearchHit> hits = new ArrayList<>();
        SearchResponseHitsIterator hitIterator = new SearchResponseHitsIterator(
                request);

        while (hitIterator.hasNext()) {
            List<SearchHit> currentHits = Arrays.asList(hitIterator.next());
            hits.addAll(currentHits);
        }
        return hits;
    }

    public List<Map<String, Object>> searchAllByRequest(SearchRequest request) {
        List<Map<String, Object>> mapList = new ArrayList<>();
        SearchHitIterator hitIterator = new SearchHitIterator(request);

        while (hitIterator.hasNext()) {
            SearchHit currentHit = hitIterator.next();
            if (currentHit.getSortValues() != null
                    && currentHit.getSortValues().length > 0) {
                currentHit.getSourceAsMap().put("sort",
                        currentHit.getSortValues());
            }
            mapList.add(currentHit.getSourceAsMap());
        }
        return mapList;
    }

    public List<Map<String, Object>> searchRequestAndGetSourceMapList(
            SearchRequest request) throws IOException {
        List<Map<String, Object>> mapList = new ArrayList<>();
        SearchResponse response = this.esClient.search(request);

        if (response.getHits() != null
                && response.getHits().getHits() != null) {
            for (SearchHit hit : response.getHits().getHits()) {
                if (hit.getSortValues() != null
                        && hit.getSortValues().length > 0) {
                    hit.getSourceAsMap().put("sort", hit.getSortValues());
                }
                mapList.add(hit.getSourceAsMap());
            }
        }
        return mapList;
    }

    public List<SearchHit> searchByTimestamp(String[] indices,
            BoolQueryBuilder boolQueryBuilder, String timestamp) {
        boolQueryBuilder = (BoolQueryBuilder) UtilTools
                .cloneObject(boolQueryBuilder);

        TermQueryBuilder timestampTerm = QueryBuilders.termQuery("@timestamp",
                timestamp);
        boolQueryBuilder.must().add(timestampTerm);

        SearchSourceBuilder sourceBuilder = getDefaultSearchSourceBuilderByGivenBoolQueryBuilder(
                boolQueryBuilder);

        SearchRequest searchRequest = new SearchRequest(indices);
        searchRequest.source(sourceBuilder);
        searchRequest.indicesOptions(
                IndicesOptions.fromOptions(true, false, false, false));

        return searchAllHits(searchRequest);
    }

    public List<SearchHit> getPreviousFromTimestamp(String[] indices,
            BoolQueryBuilder boolQueryBuilder, String timestamp) {
        // search all hits to find hit with given timestamp
        List<SearchHit> hits = this.searchByTimestamp(indices, boolQueryBuilder,
                timestamp);

        if (hits.size() > 0) {
            // Inverse Search Source builder
            SearchSourceBuilder inverseSourceBuilder = getDefaultInverseSearchSourceBuilderByGivenBoolQueryBuilder(
                    boolQueryBuilder);

            inverseSourceBuilder
                    .searchAfter(hits.get(hits.size() - 1).getSortValues());

            SearchRequest searchRequest = new SearchRequest(indices);
            searchRequest.source(inverseSourceBuilder);
            searchRequest.indicesOptions(
                    IndicesOptions.fromOptions(true, false, false, false));

            List<SearchHit> previousHits = this.searchAllHits(searchRequest);
            // Sort ASC
            Collections.reverse(previousHits);
            return previousHits;

        } else {
            return new ArrayList<>();
        }
    }

    public List<SearchHit> getLast(String[] indices,
            BoolQueryBuilder boolQueryBuilder, int size) throws IOException {
        // Inverse Search Source builder
        SearchSourceBuilder inverseSourceBuilder = getDefaultInverseSearchSourceBuilderByGivenBoolQueryBuilder(
                boolQueryBuilder);

        inverseSourceBuilder.size(size);

        SearchRequest searchRequest = new SearchRequest(indices);
        searchRequest.source(inverseSourceBuilder);
        searchRequest.indicesOptions(
                IndicesOptions.fromOptions(true, false, false, false));

        SearchResponse response = this.esClient.search(searchRequest);
        List<SearchHit> lastHits = new ArrayList<>();
        if (response.getHits() != null
                && response.getHits().getHits() != null) {
            List<SearchHit> currentHits = Arrays
                    .asList(response.getHits().getHits());
            lastHits.addAll(currentHits);
            // Sort ASC
            Collections.reverse(lastHits);
        }
        return lastHits;
    }

    public BoolQueryBuilder getBoolQueryBuilderByMonitoringQuery(
            MonitoringQuery monitoringQuery) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        for (String selectedTerm : monitoringQuery.getSelectedTerms()) {
            TermQueryBuilder term = monitoringQuery
                    .getAttributeTermByGivenName(selectedTerm);
            if (term != null) {
                boolQueryBuilder.must(term);
            }
        }

        return boolQueryBuilder;
    }

    public List<Map<String, Object>> searchAllByTerms(
            MonitoringQuery monitoringQuery) throws IOException {
        BoolQueryBuilder boolQueryBuilder = getBoolQueryBuilderByMonitoringQuery(
                monitoringQuery);

        SearchSourceBuilder sourceBuilder = getDefaultSearchSourceBuilderByGivenBoolQueryBuilder(
                boolQueryBuilder);
        SearchRequest searchRequest = new SearchRequest(
                monitoringQuery.getIndicesAsArray());
        searchRequest.source(sourceBuilder);
        searchRequest.indicesOptions(
                IndicesOptions.fromOptions(true, false, false, false));

        return this.searchAllByRequest(searchRequest);
    }

    /* *** Aggregations *** */

    public List<Map<String, Object>> searchAllAggregationsByRequest(
            SearchRequest request) {
        List<Map<String, Object>> mapList = new ArrayList<>();
        SearchHitIterator hitIterator = new SearchHitIterator(request);

        while (hitIterator.hasNext()) {
            SearchHit currentHit = hitIterator.next();
            if (currentHit.getSortValues() != null
                    && currentHit.getSortValues().length > 0) {
                currentHit.getSourceAsMap().put("sort",
                        currentHit.getSortValues());
            }
            mapList.add(currentHit.getSourceAsMap());
        }
        return mapList;
    }

    public AggregationBuilder createNestedAggs(List<String> orderedFieldList) {
        AggregationBuilder aggregationBuilder = null;
        if (orderedFieldList.size() > 0) {
            String firstField = orderedFieldList.get(0);
            aggregationBuilder = AggregationBuilders.terms(firstField + "s")
                    .size(10000).field(firstField);
            if (orderedFieldList.size() > 1) {
                aggregationBuilder.subAggregation(this.createNestedAggs(
                        orderedFieldList.subList(1, orderedFieldList.size())));
            }
        }

        return aggregationBuilder;
    }

    public List<AggregationTree> getAggTreeList(Aggregations aggs,
            List<String> fields) {
        List<AggregationTree> aggTreeList = new ArrayList<>();

        if (fields.size() > 0) {
            String field = fields.get(0) + 's';
            Terms terms = aggs.get(field);
            if (terms != null && terms.getBuckets() != null) {
                Collection<? extends Bucket> buckets = terms.getBuckets();
                for (Bucket bucket : buckets) {
                    AggregationTree aggObj = new AggregationTree();
                    aggObj.setName(bucket.getKeyAsString());
                    aggObj.setChildren(
                            this.getAggTreeList(bucket.getAggregations(),
                                    fields.subList(1, fields.size())));
                    aggTreeList.add(aggObj);
                }
            }

        }
        return aggTreeList;
    }

    public Aggregations getAggregationsTree(MonitoringQuery monitoringQuery,
            BoolQueryBuilder boolQueryBuilder) throws IOException {
        AggregationBuilder aggregationBuilder = createNestedAggs(
                monitoringQuery.getSelectedTerms());

        SearchSourceBuilder sourceBuilder = getDefaultSearchSourceBuilderByGivenBoolQueryBuilder(
                boolQueryBuilder);
        sourceBuilder.aggregation(aggregationBuilder);

        SearchRequest searchRequest = new SearchRequest(
                monitoringQuery.getIndicesAsArray());
        searchRequest.source(sourceBuilder);
        searchRequest.indicesOptions(
                IndicesOptions.fromOptions(true, false, false, false));

        return esClient.search(searchRequest).getAggregations();

    }

    public List<AggregationTree> getMonitoringTree(
            MonitoringQuery monitoringQuery, boolean isMetric)
            throws IOException {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        TermQueryBuilder streamTypeTerm = QueryBuilders.termQuery("stream_type",
                "log");

        if (isMetric) {
            boolQueryBuilder.mustNot().add(streamTypeTerm);

            TermQueryBuilder etTypeContainerTerm = QueryBuilders
                    .termQuery("et_type", "container");
            boolQueryBuilder.mustNot().add(etTypeContainerTerm);
        } else {
            boolQueryBuilder.must().add(streamTypeTerm);
        }
        Aggregations aggs = this.getAggregationsTree(monitoringQuery,
                boolQueryBuilder);
        List<AggregationTree> aggregationTreeList = getAggTreeList(aggs,
                monitoringQuery.getSelectedTerms());
        return aggregationTreeList;
    }

    /* ****************************************** */
    /* ****************** Logs ****************** */
    /* ****************************************** */

    public BoolQueryBuilder getLogBoolQueryBuilder(String component,
            String stream, boolean underShould) {
        BoolQueryBuilder componentStreamBoolBuilder = QueryBuilders.boolQuery();
        TermQueryBuilder componentTerm = QueryBuilders.termQuery("component",
                component);
        TermQueryBuilder streamTerm = QueryBuilders.termQuery("stream", stream);

        componentStreamBoolBuilder.must(componentTerm);
        componentStreamBoolBuilder.must(streamTerm);

        if (underShould) {

            TermQueryBuilder streamTypeTerm = QueryBuilders
                    .termQuery("stream_type", "log");

            BoolQueryBuilder shouldBoolBuilder = QueryBuilders.boolQuery();
            shouldBoolBuilder.should(componentStreamBoolBuilder);

            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
            boolQueryBuilder.must(streamTypeTerm);
            boolQueryBuilder.must(shouldBoolBuilder);

            return boolQueryBuilder;
        } else {
            return componentStreamBoolBuilder;
        }
    }

    public List<Map<String, Object>> searchAllLogs(
            MonitoringQuery monitoringQuery) throws IOException {
        BoolQueryBuilder boolQueryBuilder = getLogBoolQueryBuilder(
                monitoringQuery.getComponent(), monitoringQuery.getStream(),
                false);

        SearchSourceBuilder sourceBuilder = getDefaultSearchSourceBuilderByGivenBoolQueryBuilder(
                boolQueryBuilder);
        SearchRequest searchRequest = new SearchRequest(
                monitoringQuery.getIndicesAsArray());
        searchRequest.source(sourceBuilder);
        searchRequest.indicesOptions(
                IndicesOptions.fromOptions(true, false, false, false));

        return this.searchAllByRequest(searchRequest);
    }

    public List<Map<String, Object>> getPreviousLogsFromTimestamp(
            MonitoringQuery monitoringQuery) {

        BoolQueryBuilder boolQueryBuilder = getLogBoolQueryBuilder(
                monitoringQuery.getComponent(), monitoringQuery.getStream(),
                false);

        List<SearchHit> hits = this.getPreviousFromTimestamp(
                monitoringQuery.getIndicesAsArray(), boolQueryBuilder,
                monitoringQuery.getTimestamp());

        return getTracesFromHitList(hits);
    }

    public List<Map<String, Object>> getLastLogs(
            MonitoringQuery monitoringQuery, int size) throws IOException {
        BoolQueryBuilder boolQueryBuilder = getLogBoolQueryBuilder(
                monitoringQuery.getComponent(), monitoringQuery.getStream(),
                false);

        List<SearchHit> hits = this.getLast(monitoringQuery.getIndicesAsArray(),
                boolQueryBuilder, size);

        return getTracesFromHitList(hits);
    }

    @Override
    public List<AggregationTree> searchLogsTree(
            @Valid MonitoringQuery monitoringQuery) throws Exception {
        return this.getMonitoringTree(monitoringQuery, false);
    }

    @Override
    public List<AggregationTree> searchLogsLevelsTree(
            @Valid MonitoringQuery monitoringQuery) throws Exception {
        return this.getMonitoringTree(monitoringQuery, false);
    }

    /* *** Messages *** */

    public SearchRequest getFindMessageSearchRequest(String index, String msg,
            String component, String stream) {

        MatchPhrasePrefixQueryBuilder messageMatchTerm = QueryBuilders
                .matchPhrasePrefixQuery("message", msg);

        BoolQueryBuilder boolQueryBuilder = getLogBoolQueryBuilder(component,
                stream, true);
        boolQueryBuilder.must(messageMatchTerm);

        SearchSourceBuilder sourceBuilder = getDefaultSearchSourceBuilderByGivenBoolQueryBuilder(
                boolQueryBuilder);

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
            Date date = utilsService.getIso8601UTCDateFromStr(timestamp);

            return date;
        }

        return null;
    }

    @Override
    public Date findFirstStartTestMsgAndGetTimestamp(String index,
            String testName, String component) throws Exception {
        return this.findFirstMsgAndGetTimestamp(index,
                utilsService.getETTestStartPrefix() + testName, component);
    }

    @Override
    public Date findFirstFinishTestMsgAndGetTimestamp(String index,
            String testName, String component) throws Exception {
        return this.findFirstMsgAndGetTimestamp(index,
                utilsService.getETTestFinishPrefix() + testName, component);
    }

    /* ***************************************** */
    /* **************** Metrics **************** */
    /* ***************************************** */

    public BoolQueryBuilder getMetricBoolQueryBuilder(
            MonitoringQuery monitoringQuery, boolean underShould) {
        BoolQueryBuilder componentEtTypeBoolBuilder = QueryBuilders.boolQuery();
        if (monitoringQuery.getComponent() != null) {
            TermQueryBuilder componentTerm = QueryBuilders
                    .termQuery("component", monitoringQuery.getComponent());
            componentEtTypeBoolBuilder.must(componentTerm);
        }
        TermQueryBuilder etTypeTerm = QueryBuilders.termQuery("et_type",
                monitoringQuery.getEtType());
        componentEtTypeBoolBuilder.must(etTypeTerm);

        if (underShould) {
            TermQueryBuilder streamTypeTerm = QueryBuilders
                    .termQuery("stream_type", "log"); // TODO fix

            BoolQueryBuilder shouldBoolBuilder = QueryBuilders.boolQuery();
            shouldBoolBuilder.should(componentEtTypeBoolBuilder);

            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
            boolQueryBuilder.must(streamTypeTerm);
            boolQueryBuilder.must(shouldBoolBuilder);

            return boolQueryBuilder;
        } else {
            return componentEtTypeBoolBuilder;
        }
    }

    public List<Map<String, Object>> searchAllMetrics(
            MonitoringQuery monitoringQuery) throws IOException {
        BoolQueryBuilder boolQueryBuilder = getMetricBoolQueryBuilder(
                monitoringQuery, false);

        SearchSourceBuilder sourceBuilder = getDefaultSearchSourceBuilderByGivenBoolQueryBuilder(
                boolQueryBuilder);
        SearchRequest searchRequest = new SearchRequest(
                monitoringQuery.getIndicesAsArray());
        searchRequest.source(sourceBuilder);
        searchRequest.indicesOptions(
                IndicesOptions.fromOptions(true, false, false, false));

        return this.searchAllByRequest(searchRequest);
    }

    public List<Map<String, Object>> getLastMetrics(
            MonitoringQuery monitoringQuery, int size) throws IOException {
        BoolQueryBuilder boolQueryBuilder = getMetricBoolQueryBuilder(
                monitoringQuery, false);

        List<SearchHit> hits = this.getLast(monitoringQuery.getIndicesAsArray(),
                boolQueryBuilder, size);

        return getTracesFromHitList(hits);
    }

    public List<Map<String, Object>> getPreviousMetricsFromTimestamp(
            MonitoringQuery monitoringQuery) {

        BoolQueryBuilder boolQueryBuilder = getMetricBoolQueryBuilder(
                monitoringQuery, false);

        List<SearchHit> hits = this.getPreviousFromTimestamp(
                monitoringQuery.getIndicesAsArray(), boolQueryBuilder,
                monitoringQuery.getTimestamp());

        return getTracesFromHitList(hits);
    }

    @Override
    public List<AggregationTree> searchMetricsTree(
            @Valid MonitoringQuery monitoringQuery) throws Exception {
        return this.getMonitoringTree(monitoringQuery, true);
    }

    /* ******************* */
    /* *** LogAnalyzer *** */
    /* ******************* */

    public BoolQueryBuilder getLogAnalyzerQueryBuilder(
            LogAnalyzerQuery logAnalyzerQuery) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        BoolQueryBuilder componentsStreamsBoolBuilder = QueryBuilders
                .boolQuery();

        // Components/streams
        for (AggregationTree componentStream : logAnalyzerQuery
                .getComponentsStreams()) {
            for (AggregationTree stream : componentStream.getChildren()) {
                BoolQueryBuilder componentStreamBoolBuilder = QueryBuilders
                        .boolQuery();
                TermQueryBuilder componentTerm = QueryBuilders
                        .termQuery("component", componentStream.getName());
                TermQueryBuilder streamTerm = QueryBuilders.termQuery("stream",
                        stream.getName());

                componentStreamBoolBuilder.must().add(componentTerm);
                componentStreamBoolBuilder.must().add(streamTerm);
                componentsStreamsBoolBuilder.should()
                        .add(componentStreamBoolBuilder);
            }
        }

        // Levels
        for (String level : logAnalyzerQuery.getLevels()) {
            TermQueryBuilder levelTerm = QueryBuilders.termQuery("level",
                    level);
            boolQueryBuilder.must(levelTerm);
        }

        // Range Time
        RangeQueryBuilder timeRange = new RangeQueryBuilder("@timestamp");
        if (logAnalyzerQuery.getRangeLT() != null) {
            timeRange.lt(logAnalyzerQuery.getRangeLT());
        }
        if (logAnalyzerQuery.getRangeLTE() != null) {
            timeRange.lte(logAnalyzerQuery.getRangeLTE());
        }
        if (logAnalyzerQuery.getRangeGT() != null) {
            timeRange.gt(logAnalyzerQuery.getRangeGT());
        }
        if (logAnalyzerQuery.getRangeGTE() != null) {
            timeRange.gte(logAnalyzerQuery.getRangeGTE());
        }

        // Match Message
        if (logAnalyzerQuery.getMatchMessage() != null
                && !logAnalyzerQuery.getMatchMessage().equals("")) {
            MatchPhrasePrefixQueryBuilder matchPhrasePrefix = new MatchPhrasePrefixQueryBuilder(
                    "message", "*" + logAnalyzerQuery.getMatchMessage() + "*");
            boolQueryBuilder.must(matchPhrasePrefix);
        }

        // Stream Type
        TermQueryBuilder streamTypeTerm = QueryBuilders.termQuery("stream_type",
                "log");
        boolQueryBuilder.must(streamTypeTerm);

        boolQueryBuilder.must(timeRange);
        boolQueryBuilder.must(componentsStreamsBoolBuilder);

        return boolQueryBuilder;

    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> searchLogAnalyzerQuery(
            LogAnalyzerQuery logAnalyzerQuery) throws IOException {
        BoolQueryBuilder boolQueryBuilder = getLogAnalyzerQueryBuilder(
                logAnalyzerQuery);

        SearchSourceBuilder sourceBuilder = getDefaultSearchSourceBuilderByGivenBoolQueryBuilder(
                boolQueryBuilder);

        // Size
        sourceBuilder.size(logAnalyzerQuery.getSize());

        // Search After
        if (logAnalyzerQuery.getSearchAfterTrace() != null
                && logAnalyzerQuery.getSearchAfterTrace().get("sort") != null) {

            ArrayList<Object> sort = (ArrayList<Object>) logAnalyzerQuery
                    .getSearchAfterTrace().get("sort");

            sourceBuilder.searchAfter(sort.toArray());
        }

        SearchRequest searchRequest = new SearchRequest(
                logAnalyzerQuery.getIndicesAsArray());
        searchRequest.source(sourceBuilder);
        searchRequest.indicesOptions(
                IndicesOptions.fromOptions(true, false, false, false));

        // return this.searchAllByRequest(searchRequest); TODO get all option
        return this.searchRequestAndGetSourceMapList(searchRequest);
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

    public List<Map<String, Object>> getTracesFromHitList(
            List<SearchHit> hits) {
        List<Map<String, Object>> mapList = new ArrayList<>();
        for (SearchHit currentHit : hits) {
            if (currentHit.getSortValues() != null
                    && currentHit.getSortValues().length > 0) {
                currentHit.getSourceAsMap().put("sort",
                        currentHit.getSortValues());
            }
            mapList.add(currentHit.getSourceAsMap());
        }
        return mapList;
    }

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

        public SearchResponseHitsIterator(SearchRequest initialRequest) {
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
