package com.shoppingdistrict.microservices.productlistingservice;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;

import com.shoppingdistrict.microservices.model.model.Articles;
import com.shoppingdistrict.microservices.model.model.Comment;
import com.shoppingdistrict.microservices.model.model.Users;
import com.shoppingdistrict.microservices.productlistingservice.controller.ProductlistingController;
import com.shoppingdistrict.microservices.productlistingservice.repository.ArticleRepository;

@Service
@Transactional
public class ArticleManagementService {

	private Logger logger = LoggerFactory.getLogger(ArticleManagementService.class);

	@Autowired
	private ArticleRepository articleRepository;

	public List<Articles> getArticlesIdAndTitleByTitle(String title) {
		logger.info("Entry to getArticlesIdAndTitleByTitle {}", title);
		List<Articles> articles = articleRepository.findArticlsBySimilarTitle(title, true);
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
		logger.info("Returning only first {} articles and exiting from getArticlesIdAndTitleByTitle ",
				articlesToReturn.size());
		return articlesToReturn;
	}

	@Transactional
	public ResponseEntity<Articles> createArticle(Articles article) {
		logger.info("Entry to createArticle");
		article.setPublishDate(new Timestamp(System.currentTimeMillis()));
		article.setLastEditDate(new Timestamp(System.currentTimeMillis()));

		Articles savedArticle = articleRepository.saveAndFlush(article);

		if (article.getPreviousArticle() != null) {
			logger.info("Article has link to previous article and id {}", article.getPreviousArticle().getId());
			logger.info("Updating previous article next article link with this article id {}", savedArticle.getId());
			Optional<Articles> previousArticle = articleRepository.findById(article.getPreviousArticle().getId());
			Articles nextArticle = new Articles();
			nextArticle.setId(savedArticle.getId());
			articleRepository.saveAndFlush(previousArticle.get());
		}

//		URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(savedProduct.getId())
//				.toUri();

		logger.info("Returning newly created article id {} {} and exiting from createArticle", savedArticle.getId(),
				savedArticle);

		return new ResponseEntity<>(savedArticle, HttpStatus.CREATED);
	}
	
	
	public Articles retriveArticleById(Integer id) {
		logger.info("Entry to retriveArticleById");

		// URI uri =
		// ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(savedProduct.getId())
		// .toUri();

		Optional<Articles> article = articleRepository.findById(id);

		if (article.isEmpty()) {
			logger.info("Article with given id {} not found", id);
			return null;
		} else {

			Articles articles = article.get();

			/**
			 * TODO: In future it would be good idea to have DTO classes rather than using
			 * Database model classes for transporting data
			 */
			attachUserToArticle(articles);

			List<Comment> comments = articles.getComments();
			articles.setComments(attachUserToComment(comments));
			
			attachPreviousAndNextArticle(articles);

			logger.info("Returning article {} and exiting from retriveArticleById", id);
			return articles;
		}

	}

	@Transactional
	public ResponseEntity<Articles> updateArticle(Articles article, Integer id) {
		logger.info("Entry to updateArticle");
		Optional<Articles> existingArticles = articleRepository.findById(id);

		existingArticles.get().setTitle(article.getTitle());
		existingArticles.get().setCategory(article.getCategory());
		existingArticles.get().setSubcategory(article.getSubcategory());
		existingArticles.get().setIntroduction(article.getIntroduction());
		existingArticles.get().setFirstParagraph(article.getFirstParagraph());
		existingArticles.get().setSecondParagraph(article.getSecondParagraph());
		existingArticles.get().setConclusion(article.getConclusion());
		existingArticles.get().setPublish(article.isPublish());
		existingArticles.get().setLastEditDate(new Timestamp(System.currentTimeMillis()));
		existingArticles.get().setPreviousArticle(article.getPreviousArticle());

		Articles updatedArticle = articleRepository.saveAndFlush(existingArticles.get());

		if (article.getPreviousArticle() != null) {
			logger.info("Article has link to previous article and id {}", article.getPreviousArticle().getId());
			logger.info("Updating previous article next article link with this article id {}", updatedArticle.getId());
			Optional<Articles> previousArticle = articleRepository.findById(article.getPreviousArticle().getId());
			Articles nextArticle = new Articles();
			nextArticle.setId(updatedArticle.getId());
			previousArticle.get().setNextArticle(nextArticle);
			articleRepository.saveAndFlush(previousArticle.get());
		}

//				URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
//						.buildAndExpand(updatedProduct.getId()).toUri();
		/**
		 * TODO: In future it would be good idea to have DTO classes rather than using
		 * Database model classes for transporting data
		 */
		Articles articlesToReturn = new Articles();
		articlesToReturn.setId(updatedArticle.getId());
		articlesToReturn.setCategory(updatedArticle.getCategory());
		articlesToReturn.setSubcategory(updatedArticle.getSubcategory());
		articlesToReturn.setTitle(updatedArticle.getTitle());
		articlesToReturn.setIntroduction(updatedArticle.getIntroduction());
		articlesToReturn.setImages(updatedArticle.getImages());

		logger.info("Returning newly updated article id {} and exiting from updateArticle", updatedArticle.getId(),
				articlesToReturn);

		return new ResponseEntity<>(articlesToReturn, HttpStatus.CREATED);
	}
	
	/** Helper methods **/
	public void attachUserToArticle(Articles article) {
		logger.info("Entry to attachUserToArticle");
		Users user = new Users();
		Users artUser = article.getUser();
		if (artUser != null) {
			user.setId(artUser.getId());
			user.setUsername(article.getUser().getUsername());
			article.setUser(user);
		} else {
			logger.info("Can't retrieve associated user for this article {}", article.getId());
			article.setUser(null);
		}

		logger.info("Exiting from attachUserToArticle");

	}
	
	public List<Comment> attachUserToComment(List<Comment> comments) {
		logger.info("Attaching users to comments in attachUserToComment");
		List<Comment> commentsWithUser = new ArrayList<>();
		for (Comment comment : comments) {
			Users user = new Users();
			logger.info("user Id:", comment.getUser().getId());
			user.setId(comment.getUser().getId());
			// user.setEmail(comment.getUser().getEmail());
			user.setUsername(comment.getUser().getUsername());
			comment.setUser(user);
			comment.setReply(null);
			commentsWithUser.add(comment);
		}
		logger.info("Returning comments with users and exiting from attachUserToComment");
		return commentsWithUser;

	}
	

	/**
	 * Create previous or next article with only id.
	 * @param articles which existing previous and/or next article are going to be replaced with created articles.
	 */
	public void attachPreviousAndNextArticle(Articles articles) {
		logger.info("Entry to attachPreviousAndNextArticle");
		
		if(articles.getPreviousArticle() != null) {
			logger.info("Article {} has previous article with id {}, only previous article id value would be returned back along with article", articles.getId(), 
					articles.getPreviousArticle().getId());
			Articles previousArticle = new Articles();
			previousArticle.setId(articles.getPreviousArticle().getId());
			articles.setPreviousArticle(previousArticle);
		}
		
		if(articles.getNextArticle() != null) {
			logger.info("Article {} has next article with id {}, only next article id value would be returned back along with article", articles.getId(), 
					articles.getNextArticle().getId());
			Articles nextArticle = new Articles();
			nextArticle.setId(articles.getNextArticle().getId());
			articles.setNextArticle(nextArticle);
		}
		
		logger.info("Existing from attachPreviousAndNextArticle");
	}

}
