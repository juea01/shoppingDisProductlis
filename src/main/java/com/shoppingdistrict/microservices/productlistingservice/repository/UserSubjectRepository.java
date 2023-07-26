package com.shoppingdistrict.microservices.productlistingservice.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.shoppingdistrict.microservices.model.model.UserSubject;

public interface UserSubjectRepository extends JpaRepository<UserSubject, Integer> {

	List<UserSubject> findByUserId(int id);
	
	UserSubject findByUserIdAndSubjectId(int userId,int subjectId);

}
