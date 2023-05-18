package com.shoppingdistrict.microservices.productlistingservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.shoppingdistrict.microservices.model.model.Image;

public interface ImageRepository extends JpaRepository<Image, Integer> {

}
