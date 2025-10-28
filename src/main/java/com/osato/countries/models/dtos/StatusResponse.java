package com.osato.countries.models.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StatusResponse {
	private Long totalCountries;
	private String lastRefreshedAt;
}
