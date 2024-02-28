package com.shoppingdistrict.microservices.productlistingservice.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.shoppingdistrict.microservices.model.model.Articles;
import com.shoppingdistrict.microservices.model.model.Subject;

public interface SubjectRepository extends JpaRepository<Subject, Integer> {

	List<Subject> findByIsPublishAndLevel(boolean isPublish, int level);
	List<Subject> findByUserId(int userId);
	List<Subject> findByIsPublishAndLevelAndCategoryLikeAndSubCategoryLike(boolean isPublish, int level, String category, String subCategory);
	List<Subject> findByIsPublishAndLevelAndSubCategory(boolean isPublish, int level, String subCategory);
	
	

}
