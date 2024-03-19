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
import com.shoppingdistrict.microservices.productlistingservice.MyConstant;

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

	public Articles retrieveArticleById(Integer id) {
		logger.info("Entry to retrieveArticleById");

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

			logger.info("Returning article {} and exiting from retrieveArticleById", id);
			return articles;
		}

	}

	public List<Articles> retrieveRelatedArticlesById(Integer id) {
		logger.info("Entry to retrieveRelatedArticlesById {}", id);

		Optional<Articles> article = articleRepository.findById(id);

		if (article.isEmpty()) {
			logger.info("Article with given id {} not found", id);
			return null;
		} else {

			Articles articles = article.get();
			List<Articles> articlesToReturn = new ArrayList<Articles>();

			addPreviousArticle(articlesToReturn, articles.getPreviousArticle());
			Articles art = new Articles();
			art.setId(articles.getId());
			art.setCategory(articles.getCategory());
			art.setSubcategory(articles.getSubcategory());
			art.setTitle(articles.getTitle());
			art.setPublish(articles.isPublish());
			art.setPremium(articles.isPremium());
			articlesToReturn.add(art);
			addNextArticle(articlesToReturn, articles.getNextArticle());

			logger.info(
					"Returning {} related articles for given article id {} and exiting from retrieveRelatedArticlesById",
					articlesToReturn.size(), id);
			return articlesToReturn;
		}

	}

	public List<Articles> searchAllArticles(String searchCategory) {
		//TODO: THis is JUST FYI // URI uri =
		// ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(savedProduct.getId())
		// .toUri();
		logger.info("Entry to searchAllArticles with given search word {}", searchCategory);
		List<Articles> articles = articleRepository.searchBySimilarTitleOrCategoryOrSubcategoryAndIsPublish(searchCategory,
				true);

		if (articles.isEmpty()) {
			logger.info("Article with given search value {} not found", searchCategory);
			return null;
		} else {

			/**
			 * TODO: In future it would be good idea to have DTO classes rather than using
			 * Database model classes for transporting data
			 */

			List<Articles> articlesToReturn = new ArrayList<Articles>();

			for (Articles a : articles) {
				Articles art = new Articles();
				art.setId(a.getId());
				art.setCategory(a.getCategory());
				art.setSubcategory(a.getSubcategory());
				art.setTitle(a.getTitle());
				art.setPremium(a.isPremium());
				art.setPublish(a.isPublish());
				art.setIntroduction(a.getIntroduction());
				art.setImages(a.getImages());

				art.setUser(a.getUser());
				attachUserToArticle(art);
				articlesToReturn.add(art);
			}
			logger.info("Returning  {} articles and exiting from searchAllArticles", articlesToReturn.size());
			return articlesToReturn;
		}
	}

	public List<Articles> retrieveAllArticles(boolean excludeCaseStudyArticles, boolean excludeLearningArticles) {
		logger.info("Entry to retrieveAllArticles, exclude Case Study {} and exclude Learning {} ", excludeCaseStudyArticles, excludeLearningArticles);

		// List<Articles> articles =
		// articleRepository.findAll(Sort.by(Sort.Direction.DESC,"publishDate"));
		List<Articles> articles = null;
		
		if (excludeCaseStudyArticles) {
			articles = articleRepository.findByIsPublishAndCategoryNotLikeOrderByPublishDateDesc(true, MyConstant.Category.CaseStudy.toString() );
		} else if (excludeLearningArticles && !excludeCaseStudyArticles) {
			articles = articleRepository.findByIsPublishAndCategoryLikeOrderByPublishDateDesc(true, MyConstant.Category.CaseStudy.toString() );
		} else {
			articles = articleRepository.findByIsPublishOrderByPublishDateDesc(true);
		}
		
		logger.info("Size of all articles", articles.size());

		List<Articles> articlesToReturn = new ArrayList<Articles>();
		for (Articles a : articles) {
			Articles art = new Articles();
			art.setId(a.getId());
			art.setCategory(a.getCategory());
			art.setSubcategory(a.getSubcategory());
			art.setTitle(a.getTitle());
			art.setPremium(a.isPremium());
			art.setPublish(a.isPublish());
			art.setIntroduction(a.getIntroduction());
			art.setImages(a.getImages());

			art.setUser(a.getUser());
			attachUserToArticle(art);

			articlesToReturn.add(art);
		}

		logger.info("Returning {} articles and exiting from retriveAllArticles", articlesToReturn.size());
		return articlesToReturn;

	}

	public List<Articles> retrieveArticleBySubCategory(String subCategory) {
		logger.info("Entry to retrieveArticleBySubCategory {}", subCategory);

		List<Articles> articles = articleRepository.findByIsPublishAndSubcategory(true, subCategory);

		if (articles.isEmpty()) {
			logger.info("Article with given sub category {} not found", subCategory);
			return null;
		} else {

			/**
			 * TODO: In future it would be good idea to have DTO classes rather than using
			 * Database model classes for transporting data
			 */
			logger.info("Number of articles found {} with given subcategory", articles.size());

			List<Articles> articlesToReturn = new ArrayList<Articles>();

			for (Articles a : articles) {
				Articles art = new Articles();
				art.setId(a.getId());
				art.setCategory(a.getCategory());
				art.setSubcategory(a.getSubcategory());
				art.setTitle(a.getTitle());
				art.setIntroduction(a.getIntroduction());
				art.setPremium(a.isPremium());
				art.setImages(a.getImages());
				art.setUser(a.getUser());
				art.setPublish(a.isPublish());
				attachUserToArticle(art);
				articlesToReturn.add(art);
			}
			logger.info("Returning articles and exiting from retrieveArticleBySubCategory");
			return articlesToReturn;
		}
	}
	
	public List<Articles> retrieveArticleByCategory(String category) {
		logger.info("Entry to retrieveArticleByCategory {}", category);

		List<Articles> articles = articleRepository.findByIsPublishAndCategory(true, category);

		if (articles.isEmpty()) {
			logger.info("Article with given  category {} not found", category);
			return null;
		} else {

			/**
			 * TODO: In future it would be good idea to have DTO classes rather than using
			 * Database model classes for transporting data
			 */
			logger.info("Number of articles found {} with given subcategory", articles.size());

			List<Articles> articlesToReturn = new ArrayList<Articles>();

			for (Articles a : articles) {
				Articles art = new Articles();
				art.setId(a.getId());
				art.setCategory(a.getCategory());
				art.setSubcategory(a.getSubcategory());
				art.setTitle(a.getTitle());
				art.setIntroduction(a.getIntroduction());
				art.setPremium(a.isPremium());
				art.setImages(a.getImages());
				art.setUser(a.getUser());
				art.setPublish(a.isPublish());
				attachUserToArticle(art);
				articlesToReturn.add(art);
			}
			logger.info("Returning articles and exiting from retrieveArticleByCategory");
			return articlesToReturn;
		}
	}

	private void addPreviousArticle(List<Articles> articlesToReturn, Articles previousArticle) {
		logger.debug("Entry to addPreviousArticle");
		if (previousArticle != null) {
			logger.debug("Previous Article Id value is {}", previousArticle.getId());
			Articles art = new Articles();
			art.setId(previousArticle.getId());
			art.setCategory(previousArticle.getCategory());
			art.setSubcategory(previousArticle.getSubcategory());
			art.setTitle(previousArticle.getTitle());
			art.setPremium(previousArticle.isPremium());
			art.setPublish(previousArticle.isPublish());
			/**
			 * Even though adding element at specified index with underlying Array data
			 * structure can have performance impact, assumption is that maximum subsequent
			 * previous articles size should not be more than 30 articles and therefore
			 * performance impact could be negligible.
			 */
			articlesToReturn.add(0, art);

			addPreviousArticle(articlesToReturn, previousArticle.getPreviousArticle());
		} else {
			logger.debug("Exiting from addPreviousArticle as previous Article is null");
		}

	}

	private void addNextArticle(List<Articles> articlesToReturn, Articles nextArticle) {
		logger.debug("Entry to addNextArticle");
		if (nextArticle != null) {
			logger.debug("Next Article Id value is {}", nextArticle.getId());
			Articles art = new Articles();
			art.setId(nextArticle.getId());
			art.setCategory(nextArticle.getCategory());
			art.setSubcategory(nextArticle.getSubcategory());
			art.setTitle(nextArticle.getTitle());
			art.setPremium(nextArticle.isPremium());
			art.setPublish(nextArticle.isPublish());
			articlesToReturn.add(art);

			addNextArticle(articlesToReturn, nextArticle.getNextArticle());
		} else {
			logger.debug("Exiting from addNextArticle as next Article is null");
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
		existingArticles.get().setPremium(article.isPremium());
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
		articlesToReturn.setPremium(updatedArticle.isPremium());
		articlesToReturn.setImages(updatedArticle.getImages());

		logger.info("Returning newly updated article id {} and exiting from updateArticle", updatedArticle.getId(),
				articlesToReturn);

		return new ResponseEntity<>(articlesToReturn, HttpStatus.CREATED);
	}

	/** Helper methods **/
	private void attachUserToArticle(Articles article) {
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
	 * 
	 * @param articles which existing previous and/or next article are going to be
	 *                 replaced with created articles.
	 */
	public void attachPreviousAndNextArticle(Articles articles) {
		logger.info("Entry to attachPreviousAndNextArticle");

		if (articles.getPreviousArticle() != null) {
			logger.info(
					"Article {} has previous article with id {}, only previous article id value would be returned back along with article",
					articles.getId(), articles.getPreviousArticle().getId());
			Articles previousArticle = new Articles();
			previousArticle.setId(articles.getPreviousArticle().getId());
			articles.setPreviousArticle(previousArticle);
		}

		if (articles.getNextArticle() != null) {
			logger.info(
					"Article {} has next article with id {}, only next article id value would be returned back along with article",
					articles.getId(), articles.getNextArticle().getId());
			Articles nextArticle = new Articles();
			nextArticle.setId(articles.getNextArticle().getId());
			articles.setNextArticle(nextArticle);
		}

		logger.info("Existing from attachPreviousAndNextArticle");
	}

}
