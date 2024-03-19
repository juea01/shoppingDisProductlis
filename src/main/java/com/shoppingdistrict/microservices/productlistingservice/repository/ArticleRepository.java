package com.shoppingdistrict.microservices.productlistingservice.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.shoppingdistrict.microservices.model.model.Articles;


public interface ArticleRepository extends JpaRepository<Articles, Integer> {
	
	List<Articles> findByUsersId(int userId);
	List<Articles> findByIsPublishAndSubcategory(boolean isPublish, String subcateogry);
	List<Articles> findByIsPublishAndCategory(boolean isPublish, String cateogry);
	
	@Query("SELECT a from Articles a WHERE a.title LIKE %:searchWord% OR a.category LIKE %:searchWord% OR a.subcategory LIKE %:searchWord% AND a.isPublish = :isPublish")
	List<Articles> searchBySimilarTitleOrCategoryOrSubcategoryAndIsPublish(String searchWord, boolean isPublish);
	
	@Query("SELECT a from Articles a WHERE a.title LIKE %:title% AND a.isPublish = :isPublish")
	List<Articles> findArticlsBySimilarTitle(String title, boolean isPublish);
	
	List<Articles> findByIsPublishOrderByPublishDateDesc(boolean isPublish);
	
	@Query("SELECT a FROM Articles a WHERE a.isPublish = ?1 AND a.category NOT LIKE ?2 ORDER BY a.publishDate DESC")
	List<Articles> findByIsPublishAndCategoryNotLikeOrderByPublishDateDesc(boolean isPublish, String category);
	
	@Query("SELECT a FROM Articles a WHERE a.isPublish = ?1 AND a.category LIKE ?2 ORDER BY a.publishDate DESC")
	List<Articles> findByIsPublishAndCategoryLikeOrderByPublishDateDesc(boolean isPublish, String category);
	
	

}
