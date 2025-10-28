package com.osato.countries.services;

import com.osato.countries.config.NotFoundException;
import com.osato.countries.mappers.CountryMapper;
import com.osato.countries.models.dtos.CountryDto;
import com.osato.countries.models.dtos.StatusResponse;
import com.osato.countries.models.entities.Country;
import com.osato.countries.repositories.CountryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CountryService {
	private final CountryRepository countryRepository;
	private final CountryMapper mapper;
	private final ImageService imageService;

	public List<CountryDto> getAllCountries() {
		return countryRepository.findAll()
								.stream()
								.map(mapper::toDto)
								.collect(Collectors.toList());
	}

	public List<CountryDto> getCountries(String region, String currency, String sort) {
		List<Country> countries = countryRepository.findAll();

		// Filters
		if (region != null) {
			countries = countries.stream()
								 .filter(c -> region.equalsIgnoreCase(c.getRegion()))
								 .collect(Collectors.toList());
		}

		if (currency != null) {
			countries = countries.stream()
								 .filter(c -> currency.equalsIgnoreCase(c.getCurrencyCode()))
								 .collect(Collectors.toList());
		}

		// Sorting
		if (sort != null) {
			switch (sort.toLowerCase()) {
				case "gdp_desc" -> countries.sort(
						Comparator.comparing(Country::getEstimatedGdp,
								Comparator.nullsLast(Comparator.naturalOrder())
						).reversed()
				);
				case "gdp_asc" -> countries.sort(
						Comparator.comparing(Country::getEstimatedGdp,
								Comparator.nullsFirst(Comparator.naturalOrder())
						)
				);
				case "name_asc" -> countries.sort(Comparator.comparing(c -> c.getName() == null ? "" : c.getName().toLowerCase()));
				case "name_desc" -> countries.sort(Comparator.comparing(
						(Country c) -> c.getName() == null ? "" : c.getName().toLowerCase()
				).reversed());
			}
		}

		return countries.stream()
						.map(mapper::toDto)
						.collect(Collectors.toList());
	}

	public CountryDto getByName(String name) {
		Optional<Country> country = countryRepository.findByNameNormalized(name.toLowerCase());
		return country.map(mapper::toDto)
					  .orElseThrow(() -> new NotFoundException("Country not found"));
	}

	public List<CountryDto> getByRegion(String region) {
		return countryRepository.findByRegion(region)
								.stream()
								.map(mapper::toDto)
								.collect(Collectors.toList());
	}

	public List<CountryDto> getByCurrency(String currencyCode) {
		return countryRepository.findByCurrencyCode(currencyCode)
								.stream()
								.map(mapper::toDto)
								.collect(Collectors.toList());
	}

	public StatusResponse getStatus() {
		Long total = countryRepository.count();
		String lastRefreshedAt = countryRepository.findAll().stream()
												  .map(Country::getLastRefreshedAt)
												  .filter(Objects::nonNull)
												  .max(Instant::compareTo)
												  .map(Instant::toString)
												  .orElse(null);
		return new StatusResponse(total, lastRefreshedAt);
	}

	public void deleteByName(String name) {
		Optional<Country> country = countryRepository.findByNameNormalized(name.toLowerCase());
		country.ifPresentOrElse(countryRepository::delete,
								() -> { throw new NotFoundException("Country not found"); });
	}

	public void generateSummaryImage() throws Exception {
		long total = countryRepository.count();
		List<Country> top5 = countryRepository.findTop5ByGdp(PageRequest.of(0, 5));
		imageService.generateSummaryImage(total, top5, Instant.now());
	}
}
