package com.shoppingdistrict.microservices.productlistingservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.shoppingdistrict.microservices.model.model.CompletedQuestion;

public interface CompletedQuestionRepository extends JpaRepository<CompletedQuestion, Integer> {

}
