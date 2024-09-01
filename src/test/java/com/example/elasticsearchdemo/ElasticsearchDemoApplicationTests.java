package com.example.elasticsearchdemo;


import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.common.settings.Settings;

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

    }
}
