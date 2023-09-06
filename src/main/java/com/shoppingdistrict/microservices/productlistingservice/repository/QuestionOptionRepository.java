package com.shoppingdistrict.microservices.productlistingservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.shoppingdistrict.microservices.model.model.QuestionOption;


public interface QuestionOptionRepository extends JpaRepository<QuestionOption, Integer>{

}
