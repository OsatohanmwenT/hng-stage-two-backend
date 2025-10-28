// src/main/java/com/osato/countries/mappers/CountryMapper.java
package com.osato.countries.mappers;

import com.osato.countries.models.dtos.CountryDto;
import com.osato.countries.models.entities.Country;
import org.springframework.stereotype.Component;

@Component
public class CountryMapper {

	public CountryDto toDto(Country entity) {
		return CountryDto.builder()
						 .id(entity.getId())
						 .name(entity.getName())
						 .capital(entity.getCapital())
						 .region(entity.getRegion())
						 .population(entity.getPopulation())
						 .currencyCode(entity.getCurrencyCode())
						 .exchangeRate(entity.getExchangeRate())
						 .estimatedGdp(entity.getEstimatedGdp())
						 .flagUrl(entity.getFlagUrl())
						 .lastRefreshedAt(entity.getLastRefreshedAt())
						 .build();
	}

	public Country toEntity(CountryDto dto) {
		return Country.builder()
					  .id(dto.getId())
					  .name(dto.getName())
					  .capital(dto.getCapital())
					  .region(dto.getRegion())
					  .population(dto.getPopulation())
					  .currencyCode(dto.getCurrencyCode())
					  .exchangeRate(dto.getExchangeRate())
					  .estimatedGdp(dto.getEstimatedGdp())
					  .flagUrl(dto.getFlagUrl())
					  .lastRefreshedAt(dto.getLastRefreshedAt())
					  .build();
	}
}