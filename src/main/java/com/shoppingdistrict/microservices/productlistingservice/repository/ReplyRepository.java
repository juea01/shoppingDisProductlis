package com.shoppingdistrict.microservices.productlistingservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.shoppingdistrict.microservices.model.model.Reply;
import java.util.List;

public interface ReplyRepository extends JpaRepository<Reply, Integer> {
  List<Reply> findByArticleIdAndCommentId( Integer articleId, Integer commentId);
  
}
