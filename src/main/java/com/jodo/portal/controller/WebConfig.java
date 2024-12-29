package com.jodo.portal.controller;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig {
	@Bean
	public WebMvcConfigurer corsConfigurer() {
		return new WebMvcConfigurer() {
			@Override
			public void addCorsMappings(CorsRegistry registry) {
				
				registry.addMapping("/**")
//						.allowedOrigins("http://localhost:5173", "http://localhost:6000")
						.allowedOrigins("*").allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
						.allowedHeaders("*")
						.allowedHeaders("Authorization", "Content-Type", "X-Requested-With", "Accept")
						.exposedHeaders("Authorization")
//						.allowCredentials(true)
						.maxAge(31536000);

			}
		};
	}
}