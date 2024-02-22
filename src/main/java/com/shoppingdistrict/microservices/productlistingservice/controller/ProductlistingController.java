package com.shoppingdistrict.microservices.productlistingservice.controller;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.validation.Valid;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.shoppingdistrict.microservices.model.model.ArticleImage;
import com.shoppingdistrict.microservices.model.model.Articles;
import com.shoppingdistrict.microservices.model.model.Comment;
import com.shoppingdistrict.microservices.model.model.CompletedQuestion;
import com.shoppingdistrict.microservices.model.model.Image;
import com.shoppingdistrict.microservices.model.model.Products;
import com.shoppingdistrict.microservices.model.model.Question;
import com.shoppingdistrict.microservices.model.model.QuestionOption;
import com.shoppingdistrict.microservices.model.model.Reply;
import com.shoppingdistrict.microservices.model.model.Subject;
import com.shoppingdistrict.microservices.model.model.UserSubject;
import com.shoppingdistrict.microservices.model.model.Users;
import com.shoppingdistrict.microservices.productlistingservice.configuration.Configuration;
import com.shoppingdistrict.microservices.productlistingservice.repository.ArticleImageRepository;
import com.shoppingdistrict.microservices.productlistingservice.repository.ArticleRepository;
import com.shoppingdistrict.microservices.productlistingservice.repository.CommentRepository;
import com.shoppingdistrict.microservices.productlistingservice.repository.CompletedQuestionRepository;
import com.shoppingdistrict.microservices.productlistingservice.repository.ImageRepository;
import com.shoppingdistrict.microservices.productlistingservice.repository.ProductRepository;
import com.shoppingdistrict.microservices.productlistingservice.repository.QuestionRepository;
import com.shoppingdistrict.microservices.productlistingservice.repository.ReplyRepository;
import com.shoppingdistrict.microservices.productlistingservice.repository.SubjectRepository;
import com.shoppingdistrict.microservices.productlistingservice.repository.UserSubjectRepository;

import commonModule.ApiResponse;

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
	private QuestionRepository questionRepository;

	@Autowired
	private SubjectRepository subjectRepository;

	@Autowired
	private UserSubjectRepository userSubjectRepository;
	
	@Autowired
	private CompletedQuestionRepository completedQuestionRepository;

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
	public List<Image> uploadImage(@RequestParam("file") List<MultipartFile> files,
			@RequestParam("productId") Integer productId) throws IOException {

		List<Image> images = new ArrayList<>();

		for (MultipartFile file : files) {

			// upload image to AWS s3 bucket
			String fileName = StringUtils.cleanPath(file.getOriginalFilename());
			String key = UUID.randomUUID().toString() + "-" + fileName;

			ObjectMetadata metadata = new ObjectMetadata();
			metadata.setContentType(file.getContentType());
			metadata.setContentLength(file.getSize());
			amazonS3.putObject(s3BucketName, key, file.getInputStream(), metadata);
			String url = amazonS3.getUrl(s3BucketName, key).toString();

			// save image metadata in database
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
	public List<Articles> retrieveAllArticles() {
		logger.info("Entry to retriveAllArticles");
		
		List<Articles> articles = articleRepository.findAll(Sort.by(Sort.Direction.DESC,"publishDate"));
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
			
			art.setUser(a.getUser());
			attachUserToArticle(art);
			
			articlesToReturn.add(art);
		}

		logger.info("Returning articles and exiting from retriveAllArticles");
		return articlesToReturn;

	}
	
	@GetMapping("/articles/authors/{id}")
	public List<Articles> retrieveAllArticlesByAuthorId(@PathVariable Integer id) {
		logger.info("Entry to retrieveAllArticlesByAuthorId, {}", id);

		List<Articles> articles = articleRepository.findByUsersId(id);
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

		logger.info("Returning articles and exiting from retrieveAllArticlesByAuthorId");
		return articlesToReturn;

	}
	
	public void attachUserToArticle(Articles article) {
		logger.info("Entry to attachUserToArticle");
		Users user = new Users();
		Users artUser = article.getUser();
		if(artUser != null) {
			user.setId(artUser.getId());
			user.setUsername(article.getUser().getUsername());
			article.setUser(user);
		} else {
			logger.info("Can't retrieve associated user for this article {}",article.getId());	
			article.setUser(null);
		}
		
		logger.info("Exiting from attachUserToArticle");
		
	}
	
	public void attachUserToSubject(List<Subject> subjects, Subject subject) {
		logger.info("Entry to attachUserToSubject");
		if (subjects != null) {
			for (Subject sub: subjects) {
				Users subjectUser = sub.getUser();
				if(subjectUser != null) {
					Users user = new Users();
					user.setId(subjectUser.getId());
					user.setUsername(subjectUser.getUsername());
					sub.setUser(user);
				} else {
					logger.info("Can't retrieve associated user for this subject {}",sub.getId());	
					sub.setUser(null);
				}
			}
		} else {
			Users subjectUser = subject.getUser();
			if(subjectUser != null) {
				Users user = new Users();
				user.setId(subjectUser.getId());
				user.setUsername(subjectUser.getUsername());
				subject.setUser(user);
			} else {
				logger.info("Can't retrieve associated user for this subject {}",subject.getId());	
				subject.setUser(null);
			}
		}
		
		
		logger.info("Exiting from attachUserToSubject");
		
	}

	@GetMapping("/articles/{id}")
	public Articles retriveArticleById(@PathVariable Integer id) {
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

			logger.info("Returning article {} and exiting from retriveArticleById", id);
			return articles;
		}

	}

	public List<Comment> attachUserToComment(List<Comment> comments) {
		logger.info("Attaching users to comments in attachUserToComment");
		List<Comment> commentsWithUser = new ArrayList<>();
		for (Comment comment : comments) {
			Users user = new Users();
			logger.info("user Id:", comment.getUser().getId());
			user.setId(comment.getUser().getId());
			//user.setEmail(comment.getUser().getEmail());
			user.setUsername(comment.getUser().getUsername());
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
		for (Reply rep : replies) {
			Users user = new Users();
			user.setId(rep.getUser().getId());
			//user.setEmail(rep.getUser().getEmail());
			user.setUsername(rep.getUser().getUsername());
			rep.setUser(user);
			repliesWithUser.add(rep);
		}
		logger.info("Returning replies with users and exiting from attachUserToReply");
		return repliesWithUser;

	}

	public void attachUserToUserSubject(List<UserSubject> userSubjects) {
		logger.info("Attaching users to userSubject in attachUserToUserSubject");
		for (UserSubject us : userSubjects) {
			Users user = new Users();
			user.setId(us.getUser().getId());
			us.setUser(user);
		}
		logger.info("Have attached users to user subject and exiting from attachUserToUserSubject");
	}

	public void attachSubjectToUserSubject(List<UserSubject> userSubjects) {
		logger.info("Attaching subject to userSubject in attachSubjectToUserSubject");
		for (UserSubject us : userSubjects) {
			Subject subject = new Subject();
			subject.setId(us.getSubject().getId());
			us.setSubject(subject);
		}
		logger.info("Have attached subject to user subject and exiting from attachSubjectToUserSubject");
	}
	
	/**
	 * This method is used for replacing instance of  Subject class with nearly all fields value populated 
	 * (that is not needed for this scenario) and replacing
	 * that instance with Subject object that only have id field value populated. 
	 * @param questions  List of Question objects which Subject instance are going to be replaced.
	 */
	public void attachSubjectToQuestion(List<Question> questions, Question question) {
		logger.info("Attaching subject ids to question in attachSubjectToQuestion");
		
		if (questions != null) {
			for (Question qu : questions) {
				Subject subject = new Subject();
				subject.setId(qu.getSubject().getId());
				qu.setSubject(subject);
			}
		}
		
		if (question != null) {
			Subject subject = new Subject();
			subject.setId(question.getSubject().getId());
			question.setSubject(subject);
		}
		
		logger.info("Have attached subject ids to question and exiting from attachSubjectToQuestion");
	}
	
	/**
	 * This method is used to detach Question from Subject. Might need to be replaced by DTO classes in near future.
	 * @param subjects   List of Subject objects from which child class called Question is going to be detached.
	 */
	public void detachQuestionFromSubject(List<Subject> subjects, Subject subject) {
		logger.info("Detaching Question instance from Subject instacne in detachQuestionFromSubject");
		
		if (subjects != null) {
			for (Subject sub: subjects) {
				sub.setQuestions(null);
			}
		}
		
		if (subject != null) {
			subject.setQuestions(null);
		}
		
		logger.info("Detaching Question instance from Subject instacne in detachQuestionFromSubject");
	}
	

	public void attachArticleToQuestion(List<Question> questions, Question question) {
		logger.info("Attaching article ids to question in attachArticleToQuestion");
		
		if (questions != null) {
			for (Question qu : questions) {
				Articles article = new Articles();
				article.setId(qu.getArticle().getId());
				qu.setArticle(article);
			}
		}
		
		if (question != null) {
			Articles article = new Articles();
			article.setId(question.getArticle().getId());
			question.setArticle(article);
		}
		
		logger.info("Have attached article ids to question and exiting from attachArticleToQuestion");
	}

	public void attachQuestionToCompletedQuestion(List<UserSubject> userSubjects) {
		logger.info("Attaching question to completed question in attachQuestionToCompletedQuestion");
		for (UserSubject us : userSubjects) {
			for (CompletedQuestion cs : us.getCompletedQuestions()) {
				Question question = new Question();
				question.setId(cs.getQuestion().getId());
				cs.setQuestion(question);
			}

		}
		logger.info("Have attached question to completed question and exiting from attachQuestionToCompletedQuestion");
	}

	@GetMapping("/articles/{articleId}/comments/{commentId}/replies")
	public List<Reply> retriveRepliesByArticleandCommentId(@PathVariable Integer articleId,
			@PathVariable Integer commentId) {

		logger.info("Entries to retriveRepliesByArticleandCommentId {}, {}", articleId, commentId);
		List<Reply> replies = replyRepository.findByArticleIdAndCommentId(articleId, commentId);
		logger.info("Size of all replies {} by article id {} and comment id{}", replies.size(), articleId, commentId);

		/**
		 * TODO: In future it would be good idea to have DTO classes rather than using
		 * Database model classes for transporting data
		 */

		replies = attachUserToReply(replies);

		logger.info("Returning replies and exiting from retriveRepliesByArticleandCommentId {}, {}", articleId,
				commentId);
		return replies;
	}

	@GetMapping("/articles/subcategory/{subCategory}")
	public List<Articles> retriveArticleBySubCategory(@PathVariable String subCategory) {
		logger.info("Entry to retriveArticleBySubCategory {}", subCategory);

		// URI uri =
		// ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(savedProduct.getId())
		// .toUri();

		List<Articles> articles = articleRepository.findBySubcategory(subCategory);

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
				art.setImages(a.getImages());
				art.setUser(a.getUser());
				attachUserToArticle(art);
				articlesToReturn.add(art);
			}
			logger.info("Returning articles and exiting from retriveArticleBySubCategory");
			return articlesToReturn;
		}

	}

	@GetMapping("/articles/search/{searchCategory}")
	public List<Articles> searchArticle(@PathVariable String searchCategory) {
		logger.info("Entry to searchArticle");

		// URI uri =
		// ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(savedProduct.getId())
		// .toUri();

		List<Articles> articles = articleRepository.findByTitleLikeOrCategoryLikeOrSubcategoryLike(searchCategory,
				searchCategory, searchCategory);

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
//	@PreAuthorize("#article.getUser().getUsername().toUpperCase() == authentication.name.substring(authentication.name.indexOf(\":\", 3)+1).toUpperCase()")
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

	@PostMapping("/articles/images")
	public List<ArticleImage> uploadArticleImage(@RequestParam("file") List<MultipartFile> files,
			@RequestParam("articleId") Integer articleId) throws IOException {
		logger.info("Entry to uploadArticleImage for article id {}", articleId);

		List<ArticleImage> images = new ArrayList<>();
		for (MultipartFile file : files) {

			// upload image to AWS s3 bucket
			String fileName = StringUtils.cleanPath(file.getOriginalFilename());
			String key = UUID.randomUUID().toString() + "-" + fileName;

			ObjectMetadata metadata = new ObjectMetadata();
			metadata.setContentType(file.getContentType());
			metadata.setContentLength(file.getSize());
			amazonS3.putObject(s3BucketName, key, file.getInputStream(), metadata);
			String url = amazonS3.getUrl(s3BucketName, key).toString();

			// save image metadata in database
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

		logger.info("Returning newly created comment id {} {} and exiting from createComment", savedComment.getId(),
				savedComment);
		return new ResponseEntity<>(savedComment, HttpStatus.CREATED);
	}

	@PostMapping("/articles/comments/replies")
	public ResponseEntity<Reply> createReply(@Valid @RequestBody Reply reply) {
		logger.info("Entry to createReply");
		logger.info("Reply to be created {} for article id {} and comment id {}", reply.getDescription(),
				reply.getArticle().getId(), reply.getComment().getId());

		Reply savedReply = replyRepository.saveAndFlush(reply);
		List<Reply> replies = new ArrayList<>();
		replies.add(savedReply);
		replies = attachUserToReply(replies);

		logger.info("Returning newly created reply id {} and exiting from createReply", replies.get(0).getId());
		return new ResponseEntity<>(replies.get(0), HttpStatus.CREATED);
	}

	@PutMapping("/articles/comments/replies/{id}")
	public ResponseEntity<Reply> updateReply(@Valid @RequestBody Reply reply, @PathVariable Integer id) {
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

		return new ResponseEntity<>(comments.get(0), HttpStatus.CREATED);

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
	
	@PostMapping("/subjects")
	public Subject createSubject(@Valid @RequestBody Subject subject) {
		logger.info("Entry to createSubject with title {} and Question size {}", subject.getTitle(), subject.getQuestions().size());
		
		//need to set subject reference on question so that foreign key id field won't be null
		for (Question question: subject.getQuestions()) {
			question.setSubject(subject);
			
			//need to set question reference on question option 
			for (QuestionOption questionOption: question.getOptions()) {
				questionOption.setQuestion(question);
			}
		}
		
		Subject savedSubject = subjectRepository.saveAndFlush(subject);
		detachQuestionFromSubject(null, savedSubject);
		attachUserToSubject(null, savedSubject);
		
		logger.info("Returning newly created subject with id", savedSubject.getId());
		return savedSubject;
	}
	
	@PutMapping("/subjects")
	public Subject updateSubject(@Valid @RequestBody Subject subject) {
		logger.info("Entry to updateSubject for subject Id{}", subject.getId());
		
		Optional<Subject> existingSubject = subjectRepository.findById(subject.getId());
		existingSubject.get().setCategory(subject.getCategory());
		existingSubject.get().setSubCategory(subject.getCategory());
		existingSubject.get().setTitle(subject.getTitle());
		existingSubject.get().setLevel(subject.getLevel());
		existingSubject.get().setPremium(subject.isPremium());
		
		Subject savedSubject = subjectRepository.saveAndFlush(existingSubject.get());
		detachQuestionFromSubject(null, savedSubject);
		attachUserToSubject(null, savedSubject);
		
		logger.info("Returning newly updated subject with id", savedSubject.getId());
		return savedSubject;
	}

	/**
	 * Need to create DTO Classes. Probably use Builder design pattern.
	 * Only Subject class information is needed not its child classes.
	 * Annotation with JSON Ignore in Entity class is not an option in this scenario as saving
	 * Subject instance for first time also require saving its descendants.
	 * @param level
	 * @return
	 */
	@GetMapping("/subjects/search/{level}")
	public List<Subject> retrieveSubjectByLevel(@PathVariable("level") int level) {
		logger.info("Entry to retrieveSubjectByLevel, level {}", level);
		List<Subject> subjects = subjectRepository.findByLevel(level);
		logger.info("Size of all subjects, {}", subjects.size());
		detachQuestionFromSubject(subjects, null);
		attachUserToSubject(subjects, null);
		logger.info("Exiting from retrieveSubjectByLevel");
		return subjects;
	}
	
	@GetMapping("/subjects/authors/{id}")
	public List<Subject> retrieveSubjectByAuthorId(@PathVariable("id") int id) {
		logger.info("Entry to retrieveSubjectByAuthorId, Id {}", id);
		List<Subject> subjects = subjectRepository.findByUserId(id);
		logger.info("Size of all subjects, {}", subjects.size());
		detachQuestionFromSubject(subjects, null);
		attachUserToSubject(subjects, null);
		logger.info("Exiting from retrieveSubjectByLevel");
		return subjects;
	}

	@GetMapping("/subjects/search/{level}/{category}/{subcategory}")
	public List<Subject> retrieveSubjectByCategorySubCategoryAndLevel(@PathVariable("level") int level,
			@PathVariable("category") String category, @PathVariable("subcategory") String subcategory) {
		logger.info("Entry to retrieveSubjectByCategorySubCategoryAndLevel, level {}, category {}, subcategory {}",
				level, category, subcategory);
		List<Subject> subjects = subjectRepository.findByLevelAndCategoryLikeAndSubCategoryLike(level, category,
				subcategory);
		logger.info("Size of all subjects", subjects.size());
		detachQuestionFromSubject(subjects, null);
		attachUserToSubject(subjects, null);
		logger.info("Exiting from retrieveSubjectByCategorySubCategoryAndLevel");
		return subjects;
	}
	
	@GetMapping("/subjects/search/{level}/{subcategory}")
	public List<Subject> retrieveSubjectBySubCategoryAndLevel(@PathVariable("level") int level,
			 @PathVariable("subcategory") String subcategory) {
		logger.info("Entry to retrieveSubjectBySubCategoryAndLevel, level {}, subcategory {}",
				level, subcategory);
		List<Subject> subjects = subjectRepository.findByLevelAndSubCategory(level,
				subcategory);
		logger.info("Size of all subjects", subjects.size());
		detachQuestionFromSubject(subjects, null);
		attachUserToSubject(subjects, null);
		logger.info("Exiting from retrieveSubjectBySubCategoryAndLevel");
		return subjects;
	}
	
	@PostMapping("/questions")
	public Question createQuestion(@Valid @RequestBody Question question) {
		logger.info("Entry to createQuestion for question with content {}", question.getContent());
		
		for (QuestionOption option: question.getOptions()) {
			//need to set question reference on question option  so that foreign key id field won't be null
			option.setQuestion(question);
		}
		Question savedQuestion = questionRepository.saveAndFlush(question);
		logger.info("Returning newly created question with question id {} and exiting from createQuestion", savedQuestion.getId());
		return savedQuestion;
	}
	
	@PutMapping("/questions")
	public Question updateQuestion(@Valid @RequestBody Question question) {
		logger.info("Entry to updateQuestion for question with id {}", question.getId());
		
		Optional<Question> optionalQue = questionRepository.findById(question.getId());
		optionalQue.get().setArticle(question.getArticle());
		optionalQue.get().setContent(question.getContent());
		for (int i = 0; i<question.getOptions().size(); i++) {
			QuestionOption existingOption = optionalQue.get().getOptions().get(i);
			QuestionOption updateOption = question.getOptions().get(i);
			
			existingOption.setContent(updateOption.getContent());
			existingOption.setCorrectOption(updateOption.isCorrectOption());
			existingOption.setExplanation(updateOption.getExplanation());
 		}
		Question savedQuestion = questionRepository.saveAndFlush(optionalQue.get());
		
		attachSubjectToQuestion(null,savedQuestion);
		attachArticleToQuestion(null,savedQuestion);
		
		logger.info("Returning newly updated question with question id {} and exiting from updateQuestion", savedQuestion.getId());
		return savedQuestion;
	}
	
//	@PostMapping("/questionOptions")
//	public Question createQuestionOption(@Valid @RequestBody QuestionOption[] questionOptions) {
//		logger.info("Entry to createQuestionOption with number of option is {}",  questionOptions.length);
//		
//		if(questionOptions.length > 0) {
//			logger.info("There is no question option to create.");
//			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
//		} else {
//			
//		}
//		Question savedQuestion = questionRepository.saveAndFlush(question);
//		logger.info("Returning newly created question with question id {} and exiting from createQuestion", savedQuestion.getId());
//		return savedQuestion;
//	}

	@GetMapping("/questions/search/{subjectId}")
	public List<Question> retrieveQuestionBySubjectId(@PathVariable("subjectId") int subjectId) {
		logger.info("Entry to retrieveQuestionBySubjectId, subjectId {}", subjectId);
		List<Question> questions = questionRepository.findBySubjectId(subjectId);
		logger.info("Size of all questions", questions.size());
		attachSubjectToQuestion(questions, null);
		attachArticleToQuestion(questions, null);
		logger.info("Exiting from retrieveQuestionBySubjectId");
		return questions;
	}

	@GetMapping("/userSubject/{userId}")
	public List<UserSubject> retrieveUserSubjectsByUserId(@PathVariable("userId") int userId) {
		logger.info("Entry to retrieveUserSubjectsByUserId, userId {}", userId);
		List<UserSubject> userSubjects = userSubjectRepository.findByUserId(userId);
		attachUserToUserSubject(userSubjects);
		attachSubjectToUserSubject(userSubjects);
		attachQuestionToCompletedQuestion(userSubjects);
		logger.info("Size of all User Subject records", userSubjects.size());
		logger.info("Exiting from retrieveUserSubjectsByUserId");
		return userSubjects;
	}
	
	@GetMapping("/userSubject/{userId}/{subjectId}")
	public ResponseEntity<UserSubject> retrieveUserSubjectsByUserIdAndSubjectId(@PathVariable("userId") int userId, @PathVariable("subjectId") int subjectId) {
		logger.info("Entry to retrieveUserSubjectsByUserIdAndSubjectId, userId {} and subject id {}", userId, subjectId);
		UserSubject userSubject = userSubjectRepository.findByUserIdAndSubjectId(userId, subjectId);
		if(userSubject==null) {
			logger.info("No active user subject found with given user id and subject id.");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
		} else {
			List<UserSubject> userSubjects = new ArrayList<UserSubject>();
			userSubjects.add(userSubject);
			logger.info("Number of user subject found {}", userSubjects.size());
			
			attachUserToUserSubject(userSubjects);
			attachSubjectToUserSubject(userSubjects);
			attachQuestionToCompletedQuestion(userSubjects);
			
			
			logger.info("Exiting from retrieveUserSubjectsByUserIdAndSubjectId");
			return ResponseEntity.status(HttpStatus.OK).body(userSubject);
		}
	}
	
	@PostMapping("/userSubject")
	public UserSubject createUserSubject(@Valid @RequestBody UserSubject userSubject) {
		logger.info("Entry to createUserSubject for user id {} and subject id {}", userSubject.getUser().getId(), userSubject.getSubject().getId());
		UserSubject savedUserSubject = new UserSubject();
		savedUserSubject.setId(userSubjectRepository.saveAndFlush(userSubject).getId());
		logger.info("Returning newly created user subject which id is {} and exiting from createUserSubject", savedUserSubject.getId());
		return savedUserSubject;
	}
	
	/**
	 * This method is allow to update/alter two properties, completed and enabled, of UserSubject class instance.
	 * Both or either of those two properties value can be updated.
	 * @param userSubject  Instance of UserSubject class that need to be updated in database.
	 * @return HTTP Status 204 with ApiResponse object that contain success message.
	 */
	@PutMapping("/userSubject")
	public ResponseEntity<ApiResponse> updateUserSubject(@Valid @RequestBody UserSubject userSubject) {
		logger.info("Entry to updateUserSubject for user_subject id {}", userSubject.getId());
		
		Optional<UserSubject> existingUserSubject = userSubjectRepository.findById(userSubject.getId());
		existingUserSubject.get().setCompleted(userSubject.isCompleted());
		existingUserSubject.get().setEnabled(userSubject.getEnabled());
		userSubjectRepository.saveAndFlush(existingUserSubject.get());
		
		ApiResponse response = new ApiResponse("User Subject updated successfully.", null);
		logger.info("Successfully update user subject and exiting from updateUserSubject", userSubject.getId());
		return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
	}
	
	@PostMapping("/userSubject/progress")
	public ResponseEntity<ApiResponse> createSubjectProgress(@Valid @RequestBody List<CompletedQuestion> completedQuestions) {
		logger.info("Entry to createSubjectProgress for user subject id {} and number of completed questions {} to be created", completedQuestions.get(0).getUserSubject().getId(), completedQuestions.size() );
		List<CompletedQuestion> savedComQuestions = completedQuestionRepository.saveAll(completedQuestions);
		logger.info("{} completed question saved successfully", savedComQuestions.size());
		ApiResponse response = new ApiResponse("Completed Questions successfully saved.", null);
		logger.info("Exiting from createSubjectProgress");
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}
	
	

}
