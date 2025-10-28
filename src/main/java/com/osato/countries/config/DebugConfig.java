package com.osato.countries.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DebugConfig {

	@Bean
	CommandLineRunner logEnvironment() {
		return args -> {
			System.out.println("=== DATABASE ENVIRONMENT VARIABLES ===");
			System.out.println("PGHOST: " + System.getenv("PGHOST"));
			System.out.println("PGPORT: " + System.getenv("PGPORT"));
			System.out.println("PGDATABASE: " + System.getenv("PGDATABASE"));
			System.out.println("PGUSER: " + System.getenv("PGUSER"));
			System.out.println("PGPASSWORD: " + (System.getenv("PGPASSWORD") != null ? "***SET***" : "NOT SET"));
			System.out.println("DATABASE_URL: " + System.getenv("DATABASE_URL"));
			System.out.println("SPRING_DATASOURCE_URL: " + System.getenv("SPRING_DATASOURCE_URL"));
			System.out.println("======================================");
		};
	}
}
