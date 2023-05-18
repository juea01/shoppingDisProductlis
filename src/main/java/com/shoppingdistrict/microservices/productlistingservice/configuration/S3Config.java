package com.shoppingdistrict.microservices.productlistingservice.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.context.annotation.Configuration;

@Configuration
public class S3Config {
	 @Value("${aws.accessKey}")
	  private String accessKey;

	   @Value("${aws.secretKey}")
	   private String secretKey;

	    @Bean
	    public AmazonS3 amazonS3() {
	        BasicAWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);
	        return AmazonS3ClientBuilder.standard()
	                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
	                .withRegion(Regions.AP_SOUTHEAST_2) // Change this to your desired region
	                .build();
	    }
	
}
