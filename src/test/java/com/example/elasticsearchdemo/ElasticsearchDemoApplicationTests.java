package com.example.elasticsearchdemo;


import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

@SpringBootTest
class ElasticsearchDemoApplicationTests {

    @Autowired
    private RestHighLevelClient esClient;

    @Test
    void contextLoads() {
        System.out.println("测试");
    }

    //创建索引
    @Test
    public void testCreateIndex() throws IOException {
        //创建索引
        CreateIndexRequest request = new CreateIndexRequest("product_three");
        //配置索引分片
        request.settings(Settings.builder()
                .put("index.number_of_shards", 3)
                .put("index.number_of_replicas", 2)
        );
        //配置mapping
        String mapping = "\"properties\": {\n" +
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
                "    }";
        request.mapping(mapping, XContentType.JSON);
        CreateIndexResponse response = esClient.indices().create(request, RequestOptions.DEFAULT);
        String msg = "返回结果acknowledged: %s, shardsAcknowledged: %s";
        String result = String.format(msg, response.isAcknowledged(), response.isShardsAcknowledged());
        System.out.println(result);
    }
}
