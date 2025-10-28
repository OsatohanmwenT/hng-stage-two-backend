package com.osato.countries.config;

public class ExternalApiException extends RuntimeException {
	public ExternalApiException(String apiName) {
		super(apiName);
	}
}
