package com.shoppingdistrict.microservices.productlistingservice.controller;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.shoppingdistrict.microservices.model.model.Users;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.shoppingdistrict.microservices.model.model.ArticleImage;
import com.shoppingdistrict.microservices.model.model.Articles;
import com.shoppingdistrict.microservices.model.model.Comment;
import com.shoppingdistrict.microservices.model.model.Image;
import com.shoppingdistrict.microservices.model.model.Orders;
import com.shoppingdistrict.microservices.model.model.Reply;
import com.shoppingdistrict.microservices.model.model.Products;
import com.shoppingdistrict.microservices.productlistingservice.configuration.Configuration;
import com.shoppingdistrict.microservices.productlistingservice.repository.ArticleImageRepository;
import com.shoppingdistrict.microservices.productlistingservice.repository.ArticleRepository;
import com.shoppingdistrict.microservices.productlistingservice.repository.CommentRepository;
import com.shoppingdistrict.microservices.productlistingservice.repository.ImageRepository;
import com.shoppingdistrict.microservices.productlistingservice.repository.ProductRepository;
import com.shoppingdistrict.microservices.productlistingservice.repository.ReplyRepository;
import java.sql.Timestamp;

@RestController
@RequestMapping("/product-listing-service")
public class ProductlistingController {

	private Logger logger = LoggerFactory.getLogger(ProductlistingController.class);

	@Autowired
	private Environment environment;

	@Autowired
	private ProductRepository repository;

	@Autowired
	private ArticleRepository articleRepository;
	
	@Autowired
	private CommentRepository commentRepository;
	
	@Autowired
	private ReplyRepository replyRepository;
	
	@Autowired
	private ImageRepository imageRepository;
	
	@Autowired
	private ArticleImageRepository articleImageRepository;

	@Autowired
	private Configuration configuration;
	
	@Autowired
	private AmazonS3 amazonS3;
	
	private String s3BucketName = "tech-district-nanobit"; 

	// retrieveOrder
	@GetMapping("/products/{id}")
	public Products retrieveProduct(@PathVariable Integer id) {
		logger.info("Entry to retrieveProduct");

//		logger.info("Port used {}", environment.getProperty("local.server.port"));
//		logger.info("Minimum from configuration {}", configuration.getMinimum());
//		

		Products product = repository.findById(id).get();

		logger.info("Returning product {} and exiting from retrieveProduct", product);
		return product;
	}

	@PostMapping("/products")
	public ResponseEntity<Products> createProduct(@Valid @RequestBody Products product) {
		logger.info("Entry to createProduct");

		logger.info("Product to be created {}", product);
		product.setPublishDate(new Timestamp(System.currentTimeMillis()));
		product.setLastEditDate(new Timestamp(System.currentTimeMillis()));
		Products savedProduct = repository.saveAndFlush(product);

//		URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(savedProduct.getId())
//				.toUri();

		logger.info("Returning newly created product id {} {} and exiting from createProduct", savedProduct.getId(),
				savedProduct);

		return new ResponseEntity<>(savedProduct, HttpStatus.CREATED);

	}
	
	@PostMapping("/products/images")
	public List<Image> uploadImage(@RequestParam("file") List<MultipartFile> files, @RequestParam("productId") Integer productId) throws IOException {
		
		List<Image> images = new ArrayList<>();
		
		for (MultipartFile file: files) {
		
			//upload image to AWS s3 bucket
			String fileName = StringUtils.cleanPath(file.getOriginalFilename());
			String key = UUID.randomUUID().toString() + "-" + fileName;
			
			ObjectMetadata metadata = new ObjectMetadata();
			metadata.setContentType(file.getContentType());
			metadata.setContentLength(file.getSize());
			amazonS3.putObject(s3BucketName, key, file.getInputStream(), metadata);
			String url = amazonS3.getUrl(s3BucketName, key).toString();
			
			//save image metadata in database
			Image image = new Image();
			image.setLocation(url);
			image.setName(fileName);
			
			Products product = new Products();
			product.setId(productId);
			image.setProduct(product);
			images.add(imageRepository.saveAndFlush(image));
			
		}
		
		return images;
		
	}
	
	@DeleteMapping("/products/images/{id}")
    public ResponseEntity<String> deleteImageById(@PathVariable Integer id) {
		logger.info("Entry to deleteImageById", id);
		
		imageRepository.deleteById(id);
		
		logger.info("Sucessfully deleted image with id {} and exiting from deleteImageById", id);
		return ResponseEntity.ok("{\"message\":\"Image deleted sucessfully\"}");
	}

	@GetMapping("/articles")
	public List<Articles> retriveAllArticles() {
		logger.info("Entry to retriveAllArticles");

		List<Articles> articles = articleRepository.findAll();
		logger.info("Size of all articles", articles.size());
		
		 List<Articles> articlesToReturn = new ArrayList<Articles>();
		 
		 for (Articles a : articles) {
			 Articles art = new Articles();
			 art.setId(a.getId());
			 art.setCategory(a.getCategory());
			 art.setSubcategory(a.getSubcategory());
			 art.setTitle(a.getTitle());
			 art.setIntroduction(a.getIntroduction());
			 art.setImages(a.getImages());
			 articlesToReturn.add(art);
		 }

		logger.info("Returning articles and exiting from retriveAllArticles");

		return articlesToReturn;

	}
	
	@GetMapping("/articles/{id}")
	public Articles retriveArticleById(@PathVariable Integer id) {
		logger.info("Entry to retriveArticleById");
		
		//	URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(savedProduct.getId())
		//				.toUri();


		Optional<Articles> article = articleRepository.findById(id);
		
		if (article.isEmpty()) {
			logger.info("Article with given id {} not found", id);
			return null;
		} else {
			
			 Articles articles = article.get();
			 List<Comment> comments = articles.getComments();
			 
			 /**
			  * TODO: In future it would be good idea to have DTO classes rather than using Database model classes for transporting data
			  */
			
			
			 articles.setComments(attachUserToComment(comments));
			 
			 logger.info("Returning article {} and exiting from retriveArticleById", id);
			 return articles;
		}
		
	}
	
	public List<Comment> attachUserToComment(List<Comment> comments) {
		 logger.info("Attaching users to comments in attachUserToComment");
		 List<Comment> commentsWithUser = new ArrayList<>();
		 for (Comment comment : comments) {
			 Users user = new Users();
			 logger.info("user Id:",comment.getUser().getId());
			 user.setId(  comment.getUser().getId());
			 user.setEmail(comment.getUser().getEmail());
			 
			 comment.setUser(user);
			 comment.setReply(null);
			 commentsWithUser.add(comment);
		 }
		 logger.info("Returning comments with users and exiting from attachUserToComment");
		 return commentsWithUser;
		 
	}
	
	public List<Reply> attachUserToReply(List<Reply> replies) {
		 logger.info("Attaching users to replies in attachUserToReply");
		 List<Reply> repliesWithUser = new ArrayList<>();
		 for (Reply rep: replies) {
				Users user = new Users();
				user.setId(rep.getUser().getId());
				user.setEmail(rep.getUser().getEmail());
				rep.setUser(user);
				repliesWithUser.add(rep);
			}
		 logger.info("Returning replies with users and exiting from attachUserToReply");
		 return repliesWithUser;
		 
	}
	
	@GetMapping("/articles/{articleId}/comments/{commentId}/replies")
	public List<Reply> retriveRepliesByArticleandCommentId(@PathVariable Integer articleId, @PathVariable Integer commentId) {
		
		logger.info("Entries to retriveRepliesByArticleandCommentId {}, {}",articleId, commentId );
		List<Reply> replies = replyRepository.findByArticleIdAndCommentId(articleId, commentId );
		logger.info("Size of all replies {} by article id {} and comment id{}", replies.size(), articleId, commentId );
		
		 /**
		  * TODO: In future it would be good idea to have DTO classes rather than using Database model classes for transporting data
		  */
		
		replies = attachUserToReply(replies);	
		
		logger.info("Returning replies and exiting from retriveRepliesByArticleandCommentId {}, {}",articleId, commentId );
		return replies;
	}
	
	@GetMapping("/articles/subcategory/{subCategory}")
	public List<Articles> retriveArticleBySubCategory(@PathVariable String subCategory) {
		logger.info("Entry to retriveArticleBySubCategory {}", subCategory);
		
		//	URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(savedProduct.getId())
		//				.toUri();


		 List<Articles> articles = articleRepository.findBySubcategory(subCategory);
		
		if (articles.isEmpty()) {
			logger.info("Article with given sub category {} not found", subCategory);
			return null;
		} else {
			
			 /**
			  * TODO: In future it would be good idea to have DTO classes rather than using Database model classes for transporting data
			  */
			logger.info("Number of articles found {} with given subcategory",articles.size());
			
			 List<Articles> articlesToReturn = new ArrayList<Articles>();
			 
			 for (Articles a : articles) {
				 Articles art = new Articles();
				 art.setId(a.getId());
				 art.setCategory(a.getCategory());
				 art.setSubcategory(a.getSubcategory());
				 art.setTitle(a.getTitle());
				 art.setIntroduction(a.getIntroduction());
				 art.setImages(a.getImages());
				 articlesToReturn.add(art);
			 }
			 logger.info("Returning articles and exiting from retriveArticleBySubCategory");
			 return articlesToReturn;
		}
		
	}
	

	@GetMapping("/articles/search/{searchCategory}")
	public List<Articles> searchArticle(@PathVariable String searchCategory) {
		logger.info("Entry to searchArticle");
		
		//	URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(savedProduct.getId())
		//				.toUri();


		 List<Articles> articles = articleRepository.findByTitleLikeOrCategoryLikeOrSubcategoryLike(searchCategory, searchCategory, searchCategory);
		
		if (articles.isEmpty()) {
			logger.info("Article with given search value {} not found", searchCategory);
			return null;
		} else {
			
			 /**
			  * TODO: In future it would be good idea to have DTO classes rather than using Database model classes for transporting data
			  */
			
			 List<Articles> articlesToReturn = new ArrayList<Articles>();
			 
			 for (Articles a : articles) {
				 Articles art = new Articles();
				 art.setId(a.getId());
				 art.setCategory(a.getCategory());
				 art.setSubcategory(a.getSubcategory());
				 art.setTitle(a.getTitle());
				 art.setIntroduction(a.getIntroduction());
				 art.setImages(a.getImages());
				 articlesToReturn.add(art);
			 }
			 logger.info("Returning article {} and exiting from searchArticle", searchCategory);
			 return articlesToReturn;
		}
		
	}
	
	
	@PostMapping("/articles")
	public ResponseEntity<Articles> createArticle(@Valid @RequestBody Articles article) {
		logger.info("Entry to createArticle");

		logger.info("Article to be created {}", article);
		article.setPublishDate(new Timestamp(System.currentTimeMillis()));
		article.setLastEditDate(new Timestamp(System.currentTimeMillis()));
		
		Articles savedArticle = articleRepository.saveAndFlush(article);

//		URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(savedProduct.getId())
//				.toUri();

		logger.info("Returning newly created article id {} {} and exiting from createArticle", savedArticle.getId(),
				savedArticle);

		return new ResponseEntity<>(savedArticle, HttpStatus.CREATED);

	}
	
	// TODO: Shall try and catch here or let error handling component to handle
		@PutMapping("/articles/{id}")
		public ResponseEntity<Articles> updateArticle(@Valid @RequestBody Articles article, @PathVariable Integer id) {
			logger.info("Entry to updateArticle");

			logger.info("Article to be updated {}", article.getId());

			Optional<Articles> existingArticles = articleRepository.findById(id);
			existingArticles.get().setCategory(article.getCategory());
			existingArticles.get().setTitle(article.getTitle());
			existingArticles.get().setIntroduction(article.getIntroduction());
			existingArticles.get().setFirstParagraph(article.getFirstParagraph());
			existingArticles.get().setSecondParagraph(article.getSecondParagraph());
			existingArticles.get().setConclusion(article.getConclusion());
			existingArticles.get().setSubcategory(article.getSubcategory());
			existingArticles.get().setLastEditDate(new Timestamp(System.currentTimeMillis()));
			

			Articles updatedArticle = articleRepository.saveAndFlush(existingArticles.get());

//				URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
//						.buildAndExpand(updatedProduct.getId()).toUri();

			logger.info("Returning newly updated article id {} and exiting from updateArticle", updatedArticle.getId(),
					updatedArticle);

			return new ResponseEntity<>(updatedArticle,  HttpStatus.CREATED);

		}

	
	
	@PostMapping("/articles/images")
	public List<ArticleImage> uploadArticleImage(@RequestParam("file") List<MultipartFile> files, @RequestParam("articleId") Integer articleId) throws IOException {
		logger.info("Entry to uploadArticleImage for article id {}", articleId);
		
		List<ArticleImage> images = new ArrayList<>();
		for (MultipartFile file: files) {
		
			//upload image to AWS s3 bucket
			String fileName = StringUtils.cleanPath(file.getOriginalFilename());
			String key = UUID.randomUUID().toString() + "-" + fileName;
			
			ObjectMetadata metadata = new ObjectMetadata();
			metadata.setContentType(file.getContentType());
			metadata.setContentLength(file.getSize());
			amazonS3.putObject(s3BucketName, key, file.getInputStream(), metadata);
			String url = amazonS3.getUrl(s3BucketName, key).toString();
			
			//save image metadata in database
			ArticleImage image = new ArticleImage();
			image.setLocation(url);
			image.setName(fileName);
			
			Articles article = new Articles();
			article.setId(articleId);
			image.setArticle(article);
			images.add(articleImageRepository.saveAndFlush(image));
			
		}
		
		logger.info("Sucessfully uploaded images for Article id {} and exiting from uploadArticleImage", articleId);
		return images;
		
	}
	
	@DeleteMapping("/articles/images/{id}")
    public ResponseEntity<String> deleteArticleImageById(@PathVariable Integer id) {
		logger.info("Entry to deleteArticleImageById", id);
		
		articleImageRepository.deleteById(id);
		
		logger.info("Sucessfully deleted image with id {} and exiting from deleteArticleImageById", id);
		return ResponseEntity.ok("{\"message\":\"Image deleted sucessfully\"}");
	}
	
	
	
	@PostMapping("/comments")
	public ResponseEntity<Comment> createComment(@Valid @RequestBody Comment comment) {
		logger.info("Entry to createComment");
		logger.info("Comment to be created {}", comment);
		
		Comment savedComment = commentRepository.saveAndFlush(comment);
		
		logger.info("Returning newly created comment id {} {} and exiting from createComment", savedComment.getId(), savedComment);
		return new ResponseEntity<>(savedComment, HttpStatus.CREATED);
	}
	
	@PostMapping("/articles/comments/replies")
	public ResponseEntity<Reply> createReply(@Valid @RequestBody Reply reply ) {
		logger.info("Entry to createReply");
		logger.info("Reply to be created {} for article id {} and comment id {}", reply.getDescription(), reply.getArticle().getId(), reply.getComment().getId());
		
		Reply savedReply = replyRepository.saveAndFlush(reply);
		List<Reply> replies = new ArrayList<>();
		replies.add(savedReply);
		replies = attachUserToReply(replies);
		
		logger.info("Returning newly created reply id {} and exiting from createReply", replies.get(0).getId());
		return new ResponseEntity<>(replies.get(0), HttpStatus.CREATED);
	}
	
	@PutMapping("/articles/comments/replies/{id}")
	public ResponseEntity<Reply> updateReply(@Valid @RequestBody Reply reply, @PathVariable Integer id ) {
		logger.info("Entry to updateReply");
		logger.info("Reply {} to be updated for id {} ", reply.getDescription(), id);
		
		Optional<Reply> existingReply = replyRepository.findById(id);
		existingReply.get().setDescription(reply.getDescription());
		
		Reply updatedReply = replyRepository.saveAndFlush(existingReply.get());
		
		List<Reply> replies = new ArrayList<>();
		replies.add(updatedReply);
		replies = attachUserToReply(replies);	
		
		logger.info("Returning newly updated reply id {} and exiting from updateReply", replies.get(0).getId());
		return new ResponseEntity<>(replies.get(0), HttpStatus.CREATED);
	}

	
	// TODO: Shall try and catch here or let error handling component to handle
	@PutMapping("/comments/{id}")
	public ResponseEntity<Comment> updateComment(@Valid @RequestBody Comment comment, @PathVariable Integer id) {
		logger.info("Entry to updateComment");

		logger.info("Comment to be updated {}", comment.getId());

		Optional<Comment> existingComment = commentRepository.findById(id);
		existingComment.get().setDescription(comment.getDescription());
		
		Comment updatedComment = commentRepository.saveAndFlush(existingComment.get());
		
		List<Comment> comments = new ArrayList<>();
		comments.add(updatedComment);
		
		comments = attachUserToComment(comments);

//			URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
//					.buildAndExpand(updatedProduct.getId()).toUri();

		logger.info("Returning newly updated comment id {} and exiting from updateComment", comments.get(0).getId(),
				updatedComment);

		return new ResponseEntity<>(comments.get(0),  HttpStatus.CREATED);

	}
	
	
	
	

	// TODO: Shall try and catch here or let error handling component to handle
	@PutMapping("/products/{id}")
	public ResponseEntity<Products> updateProduct(@Valid @RequestBody Products product, @PathVariable Integer id) {
		logger.info("Entry to updateProduct");

		logger.info("Product to be updated {}", product.getId());

		Optional<Products> existingProduct = repository.findById(id);
		existingProduct.get().setCategory(product.getCategory());
		existingProduct.get().setName(product.getName());
		existingProduct.get().setDescription(product.getDescription());
		existingProduct.get().setFeatures(product.getFeatures());
		existingProduct.get().setSellerLink(product.getSellerLink());
		existingProduct.get().setSuitableAudience(product.getSuitableAudience());
		existingProduct.get().setLastEditDate(new Timestamp(System.currentTimeMillis()));

		Products updatedProduct = repository.saveAndFlush(existingProduct.get());

//			URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
//					.buildAndExpand(updatedProduct.getId()).toUri();

		logger.info("Returning newly updated product id {} and exiting from updateProduct", updatedProduct.getId(),
				updatedProduct);

		return new ResponseEntity<>(updatedProduct, HttpStatus.CREATED);

	}

	@GetMapping("/products")
	public List<Products> retrieveAllProducts() {
		logger.info("Entry to retrieveAllProducts");
		List<Products> products = repository.findAll();
		logger.info("Size of all orders", products.size());
		logger.info("Returning orders and exiting from retrieveAllProducts");
		return products;
	}

}
