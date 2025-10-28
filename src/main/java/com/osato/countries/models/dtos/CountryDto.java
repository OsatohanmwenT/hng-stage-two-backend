package com.osato.countries.models.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CountryDto {
	private Long id;
	private String name;
	private String capital;
	private String region;
	private Long population;
	private String currencyCode;
	private Double exchangeRate;
	private Double estimatedGdp;
	private String flagUrl;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
	private Instant lastRefreshedAt;
}