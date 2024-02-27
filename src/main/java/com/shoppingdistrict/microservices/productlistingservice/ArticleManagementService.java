package com.shoppingdistrict.microservices.productlistingservice;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.shoppingdistrict.microservices.model.model.Articles;
import com.shoppingdistrict.microservices.productlistingservice.controller.ProductlistingController;
import com.shoppingdistrict.microservices.productlistingservice.repository.ArticleRepository;

@Service
public class ArticleManagementService {
	
	private Logger logger = LoggerFactory.getLogger(ArticleManagementService.class);
	
	@Autowired
	private ArticleRepository articleRepository;
	
	public List<Articles> getArticlesIdAndTitleByTitle(String title) {
		logger.info("Entry to getArticlesIdAndTitleByTitle {}", title);
		List<Articles> articles = articleRepository.findArticlsBySimilarTitle(title);
		logger.info("Size of all articles with similar title found {}", articles.size());
		int limitSize = 5;
		int count = 0;

		List<Articles> articlesToReturn = new ArrayList<Articles>();
		for (Articles a : articles) {
			if (count < limitSize) {
				Articles art = new Articles();
				art.setId(a.getId());
				art.setCategory(a.getCategory());
				art.setSubcategory(a.getSubcategory());
				art.setTitle(a.getTitle());
				articlesToReturn.add(art);
				count++;
			} else {
				break;
			}
		
		}
		logger.info("Returning only first {} articles and exiting from getArticlesIdAndTitleByTitle ", articlesToReturn.size());
		return articlesToReturn;
	}

}
