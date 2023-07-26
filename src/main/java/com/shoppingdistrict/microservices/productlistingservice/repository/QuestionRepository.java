package com.shoppingdistrict.microservices.productlistingservice.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.shoppingdistrict.microservices.model.model.Question;

public interface QuestionRepository extends JpaRepository<Question, Integer>{
	
	List<Question> findBySubjectId(int subjectId);

}
