	package com.osato.countries.models.dtos;

	import com.fasterxml.jackson.annotation.JsonFormat;
	import com.fasterxml.jackson.annotation.JsonProperty;
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

		@JsonProperty("currency_code")
		private String currencyCode;

		@JsonProperty("exchange_rate")
		private Double exchangeRate;

		@JsonProperty("estimated_gdp")
		private Double estimatedGdp;

		@JsonProperty("flag_url")
		private String flagUrl;

		@JsonProperty("last_refreshed_at")
		@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
		private Instant lastRefreshedAt;
	}