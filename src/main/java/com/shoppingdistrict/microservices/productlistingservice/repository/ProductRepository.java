package com.shoppingdistrict.microservices.productlistingservice.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.shoppingdistrict.microservices.model.model.Products;

public interface ProductRepository extends JpaRepository<Products, Integer> {
	
	List<Products> findByName(String name);

}
