package com.shoppingdistrict.microservices.productlistingservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(
		{"com.shoppingdistrict.microservices.exceptionshandling",
		"com.shoppingdistrict.microservices.productlistingservice"})
@EntityScan(basePackages="com.shoppingdistrict.microservices.model.model")
public class ProductlistingserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProductlistingserviceApplication.class, args);
	}

}
