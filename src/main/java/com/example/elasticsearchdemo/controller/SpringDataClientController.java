package com.example.elasticsearchdemo.controller;

import com.alibaba.fastjson.JSONObject;
import com.example.elasticsearchdemo.model.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author 君墨笑
 * @date 2024/8/29
 */
@RestController
@RequestMapping("/springDataClient")
public class SpringDataClientController {

    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    @PostMapping("/getDoc")
    public void getDoc() {
        Product product = elasticsearchOperations.get("1", Product.class);
        System.out.println("结果为:" + JSONObject.toJSONString(product));
    }

    @PostMapping("/search")
    public void search() {
        PageRequest pageRequest = PageRequest.of(1,2,Sort.by(Sort.Order.asc("price")));
        Query query = Query.findAll().setPageable(pageRequest);
        SearchHits<Product> search = elasticsearchOperations.search(query, Product.class);
        search.get().forEach(it -> {
            Product content = it.getContent();
            System.out.println("结果为:" + JSONObject.toJSONString(content));
        });
    }
}
