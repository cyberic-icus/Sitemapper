package com.example.Sitemapper;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class SitemapperApplication {

	public static void main(String[] args) {
		SpringApplication.run(SitemapperApplication.class, args);
	}

}
