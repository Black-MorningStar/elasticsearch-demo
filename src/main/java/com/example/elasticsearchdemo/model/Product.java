package com.example.elasticsearchdemo.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.Date;
import java.util.List;

/**
 * 映射ES文档的Model
 *
 * @author 君墨笑
 * @date 2024/8/29
 */
@Getter
@Setter
@Document(indexName = "product",createIndex = false)
public class Product {
    @Id
    private String id;

    @Field(name = "name",type = FieldType.Text)
    private String name;

    @Field(name = "desc",type = FieldType.Text)
    private String desc;

    @Field(name = "price",type = FieldType.Double)
    private Double price;

    @Field(name = "lv",type = FieldType.Text)
    private String lv;

    @Field(name = "type",type = FieldType.Text)
    private String type;

    @Field(name = "createtime",type = FieldType.Date,format = DateFormat.basic_date_time_no_millis)
    private String createtime;

    @Field(name = "tags",type = FieldType.Text)
    private List<String> tags;
}
