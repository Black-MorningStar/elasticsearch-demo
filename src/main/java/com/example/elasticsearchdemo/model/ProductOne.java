package com.example.elasticsearchdemo.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Getter
@Setter
@Document(indexName = "product_one",createIndex = false)
public class ProductOne {

    @Id
    private String id;

    @Field(name = "name",type = FieldType.Text)
    private String name;

    @Field(name = "province",type = FieldType.Nested)
    private Province province;
}
