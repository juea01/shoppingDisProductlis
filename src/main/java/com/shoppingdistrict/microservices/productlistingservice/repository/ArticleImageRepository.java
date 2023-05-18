package com.shoppingdistrict.microservices.productlistingservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.shoppingdistrict.microservices.model.model.ArticleImage;

public interface ArticleImageRepository extends JpaRepository<ArticleImage, Integer> {

}
