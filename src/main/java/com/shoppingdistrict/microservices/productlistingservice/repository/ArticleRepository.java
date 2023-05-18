package com.shoppingdistrict.microservices.productlistingservice.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.shoppingdistrict.microservices.model.model.Articles;
import com.shoppingdistrict.microservices.model.model.Users;

public interface ArticleRepository extends JpaRepository<Articles, Integer> {
	
	List<Articles> findBySubcategory(String subcateogry);
	List<Articles> findByTitleLikeOrCategoryLikeOrSubcategoryLike(String title, String category, String subcategory);

}
