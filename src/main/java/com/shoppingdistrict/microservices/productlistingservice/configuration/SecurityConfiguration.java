package com.shoppingdistrict.microservices.productlistingservice.configuration;

import java.util.Arrays;
import java.util.Collections;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import com.shoppingdistrict.microservices.productlistingservice.controller.ProductlistingController;

//import com.example.security.securitydemo.config.KeycloakRoleConverter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {
	
	private Logger logger = LoggerFactory.getLogger(ProductlistingController.class);
	
	@Value("${keycloak.url}")
	private String keyCloakUrl;
	
	@Value("${frontend.url}")
	private String frontEndUrl;
	
	@Value("${gateway.url}")
	private String gatewayUrl;

	/**
	 * Please note that for role level security to work two ** need to be put at the end of url.
	 * For example: /customer-order-fulfillment-service/orders/**
	 * Otherwise, all authenticated user regardless of their role (for example ADMIN user) will be able to make a call.
	 */
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		logger.info("SecurityConfiguration.configure(), allowed Urls {},{},{}",keyCloakUrl, frontEndUrl, gatewayUrl);		
		JwtAuthenticationConverter authenticationConverter = new JwtAuthenticationConverter();
		authenticationConverter.setJwtGrantedAuthoritiesConverter(new KeycloakRoleConverter());

		http.cors().configurationSource(new CorsConfigurationSource() {

			@Override
			public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
				// TODO Auto-generated method stub
				CorsConfiguration config = new CorsConfiguration();
				config.setAllowedOrigins(
						Arrays.asList( keyCloakUrl, frontEndUrl));
				// config.setAllowedOrigins(Collections.singletonList("*"));
				config.setAllowedMethods(Collections.singletonList("*"));
				config.setAllowCredentials(true);
				config.setAllowedHeaders(Collections.singletonList("*"));
				config.setMaxAge(3600L);
				return config;
			}
		}).and()//don't need csrf if using JWT token
				
				.authorizeRequests().antMatchers(HttpMethod.POST,"/product-listing-service/products/**").hasRole("ADMIN").and()
				.authorizeRequests().antMatchers(HttpMethod.PUT,"/product-listing-service/products/**").hasRole("ADMIN").and()
				.authorizeRequests().antMatchers(HttpMethod.POST,"/product-listing-service/articles/**").hasAnyRole("ADMIN", "LECTURER").and()
				.authorizeRequests().antMatchers(HttpMethod.PUT,"/product-listing-service/articles/**").hasAnyRole( "ADMIN","LECTURER").and()
				.authorizeRequests().antMatchers(HttpMethod.POST,"/product-listing-service/subjects/**").hasAnyRole( "ADMIN","LECTURER").and()
				.authorizeRequests().antMatchers(HttpMethod.PUT,"/product-listing-service/subjects/**").hasAnyRole( "ADMIN","LECTURER").and()
				.authorizeRequests().antMatchers(HttpMethod.POST,"/product-listing-service/questions/**").hasAnyRole("ADMIN", "LECTURER").and()
				.authorizeRequests().antMatchers(HttpMethod.PUT,"/product-listing-service/questions/**").hasAnyRole("ADMIN", "LECTURER").and()
				.authorizeRequests().antMatchers(HttpMethod.PUT,"/product-listing-service/comments/**").hasAnyRole("ADMIN", "CUSTOMER").and()
				.authorizeRequests().antMatchers(HttpMethod.POST,"/product-listing-service/comments/**").hasAnyRole("ADMIN", "CUSTOMER").and()
				.authorizeRequests().antMatchers(HttpMethod.PUT,"/product-listing-service/userSubject/**").hasAnyRole("ADMIN", "CUSTOMER").and()
				.authorizeRequests().antMatchers(HttpMethod.POST,"/product-listing-service/userSubject/**").hasAnyRole("ADMIN", "CUSTOMER").and()
				.authorizeRequests().antMatchers(HttpMethod.GET, "/product-listing-service/products/**").permitAll().and()
				.authorizeRequests().antMatchers(HttpMethod.GET, "/product-listing-service/questions/**").permitAll().and()
				.authorizeRequests().antMatchers(HttpMethod.GET, "/product-listing-service/subjects/**").permitAll().and()
				.authorizeRequests().antMatchers(HttpMethod.GET, "/product-listing-service/userSubject/**").permitAll()
				.anyRequest().permitAll()
				.and().csrf().disable() // oauth2 has already taken care of csrf protection
				.oauth2ResourceServer().jwt().jwtAuthenticationConverter(authenticationConverter);

	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}
