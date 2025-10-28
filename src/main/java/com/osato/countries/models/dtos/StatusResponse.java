package com.osato.countries.models.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StatusResponse {
	@JsonProperty("total_countries")
	private Long totalCountries;

	@JsonProperty("last_refreshed_at")
	private String lastRefreshedAt;
}