package com.example.elasticsearchdemo.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.List;

@Getter
@Setter
public class Province {

    @Field(name = "name",type = FieldType.Text)
    private String name;

    @Field(name = "code",type = FieldType.Keyword)
    private String code;

    @Field(name = "city",type = FieldType.Nested)
    private List<City> city;

    public Province(){}

    public Province(String name, String code){
        this.name = name;
        this.code = code;
    }
}
