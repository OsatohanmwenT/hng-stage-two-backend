package com.osato.countries.config;

import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(ExternalApiException.class)
	public ResponseEntity<?> handleExternalApi(ExternalApiException ex) {
		return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
							 .body(Map.of(
									 "error", "External data source unavailable",
									 "details", "Could not fetch data from " + ex.getMessage()
							 ));
	}

	@ExceptionHandler(NotFoundException.class)
	public ResponseEntity<?> handleNotFound(NotFoundException ex) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
							 .body(Map.of("error", ex.getMessage()));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<?> handleOther(Exception ex) {
		ex.printStackTrace(); // keep server logs
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
							 .body(Map.of("error", "Internal server error"));
	}
}