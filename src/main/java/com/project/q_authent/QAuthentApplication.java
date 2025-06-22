package com.project.q_authent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class QAuthentApplication {

	public static void main(String[] args) {
		SpringApplication.run(QAuthentApplication.class, args);
		System.out.println("API docs at: http://localhost:8080/swagger-ui/index.html");
	}

}
