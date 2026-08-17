package com.example.dms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DmsServiceApplication {
	public static void main(String[] args) {
		SpringApplication.run(DmsServiceApplication.class, args);
	}
}
