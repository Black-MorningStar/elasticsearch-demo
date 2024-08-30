package com.example.elasticsearchdemo.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Getter
@Setter
public class City {

    @Field(name = "name",type = FieldType.Text)
    private String name;

    @Field(name = "code",type = FieldType.Keyword)
    private String code;

    public City() {

    }

    public City(String name, String code) {
        this.name = name;
        this.code = code;
    }
}
