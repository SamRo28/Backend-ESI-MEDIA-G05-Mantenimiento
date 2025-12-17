package com.example.usersbe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class UsersBeApplication {

	public static void main(String[] args) {
		SpringApplication.run(UsersBeApplication.class, args);
	}

}