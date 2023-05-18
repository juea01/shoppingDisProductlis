package com.shoppingdistrict.microservices.productlistingservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import com.shoppingdistrict.microservices.model.model.Articles;
import com.shoppingdistrict.microservices.model.model.Comment;
import com.shoppingdistrict.microservices.model.model.Products;
import com.shoppingdistrict.microservices.model.model.Reply;
import com.shoppingdistrict.microservices.model.model.Users;
import com.shoppingdistrict.microservices.productlistingservice.controller.ProductlistingController;
import com.shoppingdistrict.microservices.productlistingservice.repository.ArticleRepository;
import com.shoppingdistrict.microservices.productlistingservice.repository.ProductRepository;
import com.shoppingdistrict.microservices.productlistingservice.repository.ReplyRepository;

@SpringBootTest
class ProductlistingserviceApplicationTests {
	
	@Autowired
	private ProductRepository productRepository;
	
	@Autowired
	private ReplyRepository replyRepository;
	
	@Autowired
	private ArticleRepository articleRepository;
	
	@Autowired
	private ProductlistingController controller;

//	@Test
//	void contextLoads() { 
//	}
	
	@Test
	@DirtiesContext
	void testGetArticleById() {
		Products pro = new Products();
		pro.setName("Test Shoe");
		pro.setCategory("Shoe");
		pro.setDescription("Best shoe ever"); 
		
		
		productRepository.saveAndFlush(pro);
		List<Products> product = productRepository.findByName("Test Shoe");
		assertNotNull(product.get(0)); 
	}
	
	//TODO: how to make sure that there is at least one user, article and comment in database to make test case independent?
	//Note: currently make assumption that user id 1, article id 1 and comment id 1 exist.
	//Note: DirtiesContext is not working here
	@Test
	@DirtiesContext
	void testGetRepliesByCommentId() {
		Users user = new Users();
		user.setId(1);
		user.setFirstname("Smith");
		user.setLastname("Daniel");
		user.setUsername("Smith22");
		user.setAddress("No23 Road Mt Albert");
		user.setCity("Auckland");
		user.setCountry("New Zealand");
		user.setPostalCode(2345);
		user.setEmail("aka@gmail.com");
		
		Comment comment = new Comment();
		comment.setId(1);
		
		
		Articles article =articleRepository.getById(1);
		comment.setArticle(article);
		
		Reply reply = new Reply();
		reply.setDescription("I am replying to comment");
		reply.setUser(user);
		reply.setComment(comment);
		
		
		replyRepository.saveAndFlush(reply);
		
		
		List<Reply> replies = replyRepository.findByArticleIdAndCommentId(1,1);
		assertNotNull(replies.get(0)); 
	}
	
	@Test
	@DirtiesContext
	void testAttachUserToComment() {
		Optional<Articles> article = articleRepository.findById(1);
		
		
			 Articles articles = article.get();
			 List<Comment> comments = articles.getComments();
			 
			 /**
			  * TODO: In future it would be good idea to have DTO classes rather than using Database model classes for transporting data
			  */
			 
			 controller.attachUserToComment(comments);
			 articles.setComments( controller.attachUserToComment(comments));
			 //assertNotNull(articles.getComments().get(0));
			 
	
	}

}
