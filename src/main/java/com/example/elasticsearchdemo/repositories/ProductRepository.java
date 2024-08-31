package com.example.elasticsearchdemo.repositories;

import com.example.elasticsearchdemo.model.Product;
import org.springframework.data.repository.Repository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ProductRepository extends Repository<Product, String> {

    List<Product> findByNameAndPrice(String name, Double price);
}
