package com.shoppingdistrict.microservices.productlistingservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.shoppingdistrict.microservices.model.model.Comment;

public interface CommentRepository extends JpaRepository<Comment, Integer> {

}
