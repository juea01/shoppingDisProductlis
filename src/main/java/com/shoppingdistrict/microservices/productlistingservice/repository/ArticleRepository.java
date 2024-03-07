package com.shoppingdistrict.microservices.productlistingservice.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.shoppingdistrict.microservices.model.model.Articles;


public interface ArticleRepository extends JpaRepository<Articles, Integer> {
	
	List<Articles> findByUsersId(int userId);
	List<Articles> findByIsPublishAndSubcategory(boolean isPublish, String subcateogry);
	
	@Query("SELECT a from Articles a WHERE a.title LIKE %:searchWord% OR a.category LIKE %:searchWord% OR a.subcategory LIKE %:searchWord% AND a.isPublish = :isPublish")
	List<Articles> searchBySimilarTitleOrCategoryOrSubcategoryAndIsPublish(String searchWord, boolean isPublish);
	
	@Query("SELECT a from Articles a WHERE a.title LIKE %:title% AND a.isPublish = :isPublish")
	List<Articles> findArticlsBySimilarTitle(String title, boolean isPublish);
	
	List<Articles> findByIsPublishOrderByPublishDateDesc(boolean isPublish);
	
	

}
