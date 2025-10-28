package com.osato.countries;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CountriesApplication {

	public static void main(String[] args) {
		SpringApplication.run(CountriesApplication.class, args);
		System.out.println("========== ENVIRONMENT VARIABLES ==========");
		System.out.println("PGHOST: " + System.getenv("PGHOST"));
		System.out.println("PGPORT: " + System.getenv("PGPORT"));
		System.out.println("PGDATABASE: " + System.getenv("PGDATABASE"));
		System.out.println("PGUSER: " + System.getenv("PGUSER"));
		System.out.println("PGPASSWORD: " + (System.getenv("PGPASSWORD") != null ? "***SET***" : "NOT SET"));
		System.out.println("SPRING_DATASOURCE_URL: " + System.getenv("SPRING_DATASOURCE_URL"));
		System.out.println("SPRING_DATASOURCE_USERNAME: " + System.getenv("SPRING_DATASOURCE_USERNAME"));
		System.out.println("DATABASE_URL: " + System.getenv("DATABASE_URL"));
		System.out.println("==========================================");

	}

}
