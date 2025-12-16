package com.EsiMediaG03;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableMongoRepositories(basePackages = "com.EsiMediaG03.dao")
@EnableAsync
public class EsiMediaContenidosG03Application {

	public static void main(String[] args) {
		SpringApplication.run(EsiMediaContenidosG03Application.class, args);
	}

	
}
