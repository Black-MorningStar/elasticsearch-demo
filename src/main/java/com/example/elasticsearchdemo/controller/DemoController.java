package com.example.elasticsearchdemo.controller;

import com.alibaba.fastjson.JSONObject;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.replication.ReplicationResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.GetSourceRequest;
import org.elasticsearch.client.core.GetSourceResponse;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.index.reindex.UpdateByQueryRequest;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author 君墨笑
 * @date 2024/8/28
 */
@RestController
@RequestMapping("/demo")
public class DemoController {

    @Autowired
    private RestHighLevelClient esClient;

    @PostMapping("/testCreateIndex")
    public void testCreateIndex() throws IOException {
        //创建索引
        CreateIndexRequest request = new CreateIndexRequest("product_three");
        //配置索引分片
        request.settings(Settings.builder()
                .put("index.number_of_shards", 3)
                .put("index.number_of_replicas", 2)
        );
        //配置mapping
        String mapping = "{\n" +
                "    \"properties\": {\n" +
                "      \"name\": {\n" +
                "        \"type\": \"text\",\n" +
                "        \"analyzer\": \"ik_max_word\",\n" +
                "        \"fields\": {\n" +
                "          \"keyword\": {\n" +
                "            \"type\": \"keyword\",\n" +
                "            \"ignore_above\": 256\n" +
                "          }\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  }";
        request.mapping(mapping, XContentType.JSON);
        CreateIndexResponse response = esClient.indices().create(request, RequestOptions.DEFAULT);
        String msg = "返回结果acknowledged: %s, shardsAcknowledged: %s";
        String result = String.format(msg, response.isAcknowledged(), response.isShardsAcknowledged());
        System.out.println(result);
    }

    @PostMapping("/indexDoc")
    public void indexDoc() throws IOException {
        IndexRequest request = new IndexRequest("product_three");
        request.id("2");
        String json = "{\n" +
                "  \"name\" : \"测试数据2\"\n" +
                "}";
        request.source(json,XContentType.JSON);
        IndexResponse response = esClient.index(request, RequestOptions.DEFAULT);
        DocWriteResponse.Result result = response.getResult();
        if (result == DocWriteResponse.Result.CREATED) {
            System.out.println("这是第一次创建");
        }
        if (result == DocWriteResponse.Result.UPDATED) {
            System.out.println("这是更新操作");
        }
        ReplicationResponse.ShardInfo shardInfo = response.getShardInfo();
        System.out.println("分片总数 :" +  shardInfo.getTotal());
        System.out.println("分片成功数量:" + shardInfo.getSuccessful());
        System.out.println("分片失败的数量:" + shardInfo.getFailed());
    }


    @PostMapping("/getDoc")
    public void getDoc() throws IOException {
        GetRequest request = new GetRequest(
                "product_three",
                "1");
        //request.fetchSourceContext(FetchSourceContext.FETCH_SOURCE);
        GetResponse response = esClient.get(request, RequestOptions.DEFAULT);
        String id = response.getId();
        String sourceAsString = response.getSourceAsString();
        boolean exists = response.isExists();
        System.out.println("返回结果ID:" + id + " 数据Source:" + sourceAsString + " 是否存在: " + exists);
    }

    @PostMapping("/getDocSource")
    public void getDocSource() throws IOException {
        GetSourceRequest request = new GetSourceRequest(
                "product_three",
                "1");
        GetSourceResponse response = esClient.getSource(request,RequestOptions.DEFAULT);
        Map<String, Object> source = response.getSource();
        System.out.println("结果: " + JSONObject.toJSONString(source));
    }

    @PostMapping("/updateDoc")
    public void updateDoc() throws IOException {
        UpdateRequest request = new UpdateRequest(
                "product_three",
                "1");
        String json = "{\n" +
                "    \"name\" : \"测试数据修改02\"\n" +
                "  }";
        request.doc(json,XContentType.JSON);
        UpdateResponse updateResponse = esClient.update(
                request, RequestOptions.DEFAULT);
        String status = updateResponse.getResult().getLowercase();
        System.out.println("状态为: " + status);
    }

    @PostMapping("/bulkApi")
    public void bulkApi() throws IOException {
        BulkRequest request = new BulkRequest("product_three");
        request.add(new IndexRequest().id("3")
                .source(XContentType.JSON,"name", "测试名称03"));
        request.add(new IndexRequest().id("4")
                .source(XContentType.JSON,"name", "测试名称04"));
        BulkResponse bulkResponse = esClient.bulk(request, RequestOptions.DEFAULT);
        for (BulkItemResponse itemResponse : bulkResponse) {
            DocWriteResponse response = itemResponse.getResponse();
            switch (itemResponse.getOpType()) {
                case INDEX:
                    IndexResponse indexResponse = (IndexResponse) response;
                    if (indexResponse.getResult() == DocWriteResponse.Result.CREATED) {
                        System.out.println("这是一次创建");
                    }
                    break;
                default:
            }
        }
    }

    @PostMapping("/updateByQuery")
    public void updateByQuery() {
        UpdateByQueryRequest request =
                new UpdateByQueryRequest("product_three");
        request.setBatchSize(1);
        request.setQuery(new TermQueryBuilder("name","测试"));
    }


    @PostMapping("/searchApi")
    public void searchApi() throws IOException {
        SearchRequest searchRequest = new SearchRequest("product_three");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = esClient.search(searchRequest, RequestOptions.DEFAULT);
        SearchHits hits = searchResponse.getHits();
        SearchHit[] searchHits = hits.getHits();
        for (SearchHit hit : searchHits) {
            String sourceAsString = hit.getSourceAsString();
            System.out.println("查询结果为:" + sourceAsString);
        }
    }

    @PostMapping("/searchApi2")
    public void searchApi2() throws IOException {
        SearchRequest searchRequest = new SearchRequest("product_three");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = esClient.search(searchRequest, RequestOptions.DEFAULT);
        SearchHits hits = searchResponse.getHits();
        SearchHit[] searchHits = hits.getHits();
        for (SearchHit hit : searchHits) {
            String sourceAsString = hit.getSourceAsString();
            System.out.println("查询结果为:" + sourceAsString);
        }
    }

    @PostMapping("/searchApi3")
    public void searchApi3() throws IOException {
        SearchRequest searchRequest = new SearchRequest("product");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery()
                .filter(QueryBuilders.termQuery("name", "小米"))
                .must(QueryBuilders.rangeQuery("price").gte(0).lte(3000));
        searchSourceBuilder.query(boolQueryBuilder);
        searchSourceBuilder.sort("price", SortOrder.ASC);
        String[] includeFields = new String[] {"name", "price"};
        searchSourceBuilder.fetchSource(includeFields,null);
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = esClient.search(searchRequest, RequestOptions.DEFAULT);
        SearchHits hits = searchResponse.getHits();
        SearchHit[] searchHits = hits.getHits();
        for (SearchHit hit : searchHits) {
            String sourceAsString = hit.getSourceAsString();
            System.out.println("查询结果为:" + sourceAsString);
        }
    }

    @PostMapping("/searchApi4")
    public void searchApi4() throws IOException {
        SearchRequest searchRequest = new SearchRequest("product");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        TermsAggregationBuilder aggregationBuilder = AggregationBuilders.terms("lvBucket")
                .field("lv.keyword");
        searchSourceBuilder.aggregation(aggregationBuilder);
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = esClient.search(searchRequest, RequestOptions.DEFAULT);
        Aggregations aggregations = searchResponse.getAggregations();
        Terms lvBucket = aggregations.get("lvBucket");
        List<? extends Terms.Bucket> buckets = lvBucket.getBuckets();
        for (Terms.Bucket bucket : buckets) {
            String keyAsString = bucket.getKeyAsString();
            long docCount = bucket.getDocCount();
            System.out.println("key：" + keyAsString + " count: " + docCount);
        }
    }


    @PostMapping("/searchApi5")
    public void searchApi5() throws IOException {
        SearchRequest searchRequest = new SearchRequest("product");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.size(0);
        TermsAggregationBuilder aggregationBuilder = AggregationBuilders.terms("type")
                .field("type.keyword")
                        .subAggregation(AggregationBuilders.avg("price")
                                .field("price"));
        searchSourceBuilder.aggregation(aggregationBuilder);
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = esClient.search(searchRequest, RequestOptions.DEFAULT);
        Aggregations aggregations = searchResponse.getAggregations();
        Terms lvBucket = aggregations.get("type");
        List<? extends Terms.Bucket> buckets = lvBucket.getBuckets();
        for (Terms.Bucket bucket : buckets) {
            String keyAsString = bucket.getKeyAsString();
            long docCount = bucket.getDocCount();
            System.out.println("key：" + keyAsString + " count: " + docCount);
        }
    }
}
