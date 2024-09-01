package com.example.elasticsearchdemo.controller;

import com.alibaba.fastjson.JSONObject;
import com.example.elasticsearchdemo.model.City;
import com.example.elasticsearchdemo.model.Product;
import com.example.elasticsearchdemo.model.ProductOne;
import com.example.elasticsearchdemo.model.Province;
import com.example.elasticsearchdemo.repositories.ProductRepository;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.AggregationsContainer;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.SearchPage;
import org.springframework.data.elasticsearch.core.query.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 君墨笑
 * @date 2024/8/29
 */
@RestController
@RequestMapping("/springDataClient")
public class SpringDataClientController {

    @Autowired
    private ElasticsearchOperations elasticsearchOperations;
    @Autowired
    private ProductRepository productRepository;

    @PostMapping("/getDoc")
    public void getDoc() {
        Product product = elasticsearchOperations.get("1", Product.class);
        System.out.println("结果为:" + JSONObject.toJSONString(product));
    }

    @PostMapping("/search")
    public void search(int page, int size) {
        PageRequest pageRequest = PageRequest.of(page,size,Sort.by(Sort.Order.asc("price")));
        CriteriaQuery criteriaQuery = new CriteriaQuery(new Criteria(), pageRequest);
        SearchHits<Product> search = elasticsearchOperations.search(criteriaQuery, Product.class);
        search.get().forEach(it -> {
            Product content = it.getContent();
            System.out.println("ID: "+content.getId() + " name: " + content.getName() + " price: " + content.getPrice());
        });
    }

    @PostMapping("/search2")
    public void search2() {
        //is语义会先进行分词，然后将分词条件用must组合
        //CriteriaQuery criteriaQuery = new CriteriaQuery(new Criteria("name").is("小米手机NFC"));
        //match语义
        //CriteriaQuery criteriaQuery = new CriteriaQuery(new Criteria("name").matches("耳机电视"));
        //多个term语义，用should组合
        //CriteriaQuery criteriaQuery = new CriteriaQuery(new Criteria("name").in("耳机电视"));
        //term语义
        //CriteriaQuery criteriaQuery = new CriteriaQuery(new Criteria("name").contains("nfc"));
        CriteriaQuery criteriaQuery =
                new CriteriaQuery(new Criteria("name").is("小米")
                        .and(new Criteria("price").between(10, 3000)))
                        .addSort(Sort.by(Sort.Order.desc("price")));

        SearchHits<Product> search = elasticsearchOperations.search(criteriaQuery, Product.class);
        search.get().forEach(it -> {
            Product content = it.getContent();
            System.out.println("ID: "+content.getId() + " name: " + content.getName() + " price: " + content.getPrice());
        });
    }

    @PostMapping("/search3")
    public void search3() {
        ProductOne productOne1 = bulid("3", "这是第三条测试数据之后", "湖南省", "HN",
                "长沙市", "CS",
                "开封市", "KF",
                "湘潭市", "XT");
        ProductOne productOne2 = bulid("4", "这是第四条测试数据修改之后", "河北省", "HB",
                "石家庄市", "SJZ",
                "衡水市", "HS",
                "秦皇岛市", "QHD");
        Iterable<ProductOne> save = elasticsearchOperations.save(productOne1, productOne2);
        save.forEach(it -> {
            System.out.println("结果为 : " + JSONObject.toJSONString(it));
        });
    }

    @PostMapping("/search4")
    public void search4() {
        ProductOne productOne = elasticsearchOperations.get("1", ProductOne.class);
        System.out.println("结果为: " + JSONObject.toJSONString(productOne));
    }

    @PostMapping("/search5")
    public void search5() {
        StringQuery stringQuery = new StringQuery("{\n" +
                "    \"type\": {\n" +
                "      \"terms\": {\n" +
                "        \"field\": \"type.keyword\",\n" +
                "        \"size\": 10\n" +
                "      }\n" +
                "    }\n" +
                "  }");

        SearchHits<ProductOne> search = elasticsearchOperations.search(stringQuery, ProductOne.class);
        Aggregations aggregations = (Aggregations) search.getAggregations().aggregations();
        Terms type = aggregations.get("type");
        List<? extends Terms.Bucket> buckets = type.getBuckets();
        for (Terms.Bucket bucket : buckets) {
            System.out.println("key: " + bucket.getKeyAsString() + " count: " + bucket.getDocCount());
        }
    }

    @PostMapping("/search6")
    public void search6(String name, Double price) {
        List<Product> list = productRepository.findByNameAndPrice(name, price);
        list.forEach(it -> {
            System.out.println("结果为: " + JSONObject.toJSONString(it));
        });
    }

    @PostMapping("/search7")
    public void search7(String order1, String order2) {
        NativeSearchQueryBuilder builder = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.matchQuery("name", "小米"))
                .withSorts(new FieldSortBuilder("price").order(SortOrder.DESC), new FieldSortBuilder("createtime").order(SortOrder.DESC))
                .withPageable(PageRequest.ofSize(2));
        if (StringUtils.isNotBlank(order1) && StringUtils.isNotBlank(order2)) {
            List<Object> list = new ArrayList<>();
            list.add(order1);
            list.add(order2);
            builder.withSearchAfter(list);
        }

        SearchHits<Product> search = elasticsearchOperations.search(builder.build(), Product.class);
        search.forEach(it -> {
            Product content = it.getContent();
            List<Object> sortValues = it.getSortValues();
            System.out.println("结果为: " + JSONObject.toJSONString(content));
            System.out.println("排序为: " + sortValues.get(0) + "-" + sortValues.get(1));
        });
    }

    @PostMapping("/search8")
    public void search9() {
        SearchPage<Product> page = productRepository.findByNameOrderByPriceDescCreatetimeDesc("小米", PageRequest.of(0, 2));
        page.getSearchHits().forEach(it -> {
            Product content = it.getContent();
            List<Object> sortValues = it.getSortValues();
            System.out.println("结果为: " + JSONObject.toJSONString(content));
            System.out.println("排序为: " + sortValues.get(0) + "-" + sortValues.get(1));
        });
    }

    @PostMapping("/search10")
    public void search10() {
        NativeSearchQuery query = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.matchQuery("name", "小米"))
                .withHighlightFields(new HighlightBuilder.Field("name")).build();
        SearchHits<Product> search = elasticsearchOperations.search(query, Product.class);
        search.getSearchHits().forEach(it -> {
            List<String> field = it.getHighlightField("name");
            System.out.println("高亮为: "+JSONObject.toJSONString(field));
        });
    }

    private ProductOne bulid(String id, String name,
                             String pName, String pCode,
                             String cName1, String cCode1,
                             String cName2, String cCode2,
                             String cName3, String cCode3) {
        ProductOne productOne = new ProductOne();
        productOne.setId(id);
        productOne.setName(name);
        Province province = new Province(pName,pCode);
        productOne.setProvince(province);
        City city1 = new City(cName1,cCode1);
        City city2 = new City(cName2,cCode2);
        City city3 = new City(cName3,cCode3);
        ArrayList<City> list = new ArrayList<>();
        list.add(city1);
        list.add(city2);
        list.add(city3);
        province.setCity(list);
        return productOne;
    }
}
