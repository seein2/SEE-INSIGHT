package com.seein;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.seein")
public class SeeInsightApplication {

	public static void main(String[] args) {
		SpringApplication.run(SeeInsightApplication.class, args);
	}

}
