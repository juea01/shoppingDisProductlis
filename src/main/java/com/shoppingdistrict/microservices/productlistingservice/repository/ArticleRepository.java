package com.shoppingdistrict.microservices.productlistingservice.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.shoppingdistrict.microservices.model.model.Articles;


public interface ArticleRepository extends JpaRepository<Articles, Integer> {
	
	List<Articles> findByUsersId(int userId);
	List<Articles> findBySubcategory(String subcateogry);
	List<Articles> findByTitleLikeOrCategoryLikeOrSubcategoryLike(String title, String category, String subcategory);
	
	@Query("SELECT a from Articles a WHERE a.title LIKE %:title%")
	List<Articles> findArticlsBySimilarTitle(String title);
	

}
