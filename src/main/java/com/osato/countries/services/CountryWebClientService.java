package com.osato.countries.services;

import com.osato.countries.config.ExternalApiException;
import com.osato.countries.models.entities.Country;
import com.osato.countries.repositories.CountryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.*;

/**
 * Service that fetches countries and exchange rates, maps them, and upserts into DB.
 * - Transactional: will roll back DB changes if something fails during upsert.
 * - Handles multiple external API shapes (v2 / v3).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CountryWebClientService {
	private final RestTemplate restTemplate;
	private final CountryRepository countryRepository;

	@Value("${app.countries-api:https://restcountries.com/v2/all?fields=name,capital,region,population,flag,currencies}")
	private String COUNTRIES_API;

	@Value("${app.rates-api:https://open.er-api.com/v6/latest/USD}")
	private String RATES_API;

	private static final int MIN_MULTIPLIER = 1000;
	private static final int MAX_MULTIPLIER = 2000;
	private final Random rng = new Random();

	/**
	 * Full refresh: fetch external data and upsert countries.
	 * If external API calls fail -> throws RuntimeException (caller should map to 503) and DB is not modified.
	 * <p>
	 * Returns number of processed countries (inserted + updated).
	 */
	@Transactional
	public int syncAllCountries() {
		// 1) fetch countries
		Object[] countriesRaw = new Object[0];
		try {
			ResponseEntity<Object[]> response = restTemplate.getForEntity(COUNTRIES_API, Object[].class);
			if (response.getBody() == null || response.getBody().length == 0) {
				throw new RuntimeException("Countries not found: ");
			}
			countriesRaw = response.getBody();
		} catch (RestClientException e) {
			log.error("Failed to fetch countries API: {}", e.getMessage(), e);
			throw new ExternalApiException("Countries API");
		}

		Map<String, Object> ratesMap = Map.of();
		try {
			ResponseEntity<Map> rresp = restTemplate.getForEntity(RATES_API, Map.class);
			if (rresp.getBody() == null || !rresp.getBody().containsKey("rates")) {
				log.error("Rates API returned invalid body");
				throw new ExternalApiException("Rates API");
			}
			Object ratesObj = rresp.getBody().get("rates");
			if (!(ratesObj instanceof Map)) {
				log.error("Rates API 'rates' is not a map");
				throw new ExternalApiException("Rates API");
			}

			//noinspection unchecked
			ratesMap = (Map<String, Object>) ratesObj;
		} catch (RestClientException e) {
			log.error("Failed to fetch rates API: {}", e.getMessage(), e);
			throw new ExternalApiException("Rates API");
		}

		List<Country> toUpsert = new ArrayList<>(countriesRaw.length);
		for (Object raw : countriesRaw) {
			if (!(raw instanceof Map)) {
				log.warn("Skipping non-map country record: {}", raw);
				continue;
			}
			@SuppressWarnings("unchecked") Map<String, Object> data = (Map<String, Object>) raw;
			try {
				Country mapped = mapToCountry(data, ratesMap);
				if (mapped != null) {
					mapped.setLastRefreshedAt(Instant.now());
					// ensure normalized name for upsert lookups
					String norm = safeNormalize(mapped.getName());
					mapped.setNameNormalized(norm);
					toUpsert.add(mapped);
				}
			} catch (Exception ex) {
				log.error("Failed to map country: {}", data.get("name"), ex);
			}
		}

		int processed = 0;
		for (Country incoming : toUpsert) {
			Optional<Country> existingOpt = countryRepository.findByNameNormalized(incoming.getNameNormalized());
			if (existingOpt.isPresent()) {
				Country existing = existingOpt.get();
				// update fields
				existing.setCapital(incoming.getCapital());
				existing.setRegion(incoming.getRegion());
				existing.setPopulation(incoming.getPopulation());
				existing.setCurrencyCode(incoming.getCurrencyCode());
				existing.setExchangeRate(incoming.getExchangeRate());
				existing.setEstimatedGdp(incoming.getEstimatedGdp());
				existing.setFlagUrl(incoming.getFlagUrl());
				existing.setLastRefreshedAt(incoming.getLastRefreshedAt());
				countryRepository.save(existing);
			} else {
				countryRepository.save(incoming);
			}
			processed++;
		}

		log.info("Refresh complete - processed {} countries", processed);
		return processed;
	}

	/**
	 * Map a raw country map to our Country entity following the spec rules:
	 * - extract first currency code if available
	 * - if no currencies -> currencyCode=null, exchangeRate=null, estimatedGdp=0
	 * - if currency exists but not found in rates -> exchangeRate=null, estimatedGdp=null
	 * - if found -> compute estimatedGdp = population * multiplier(1000..2000) / exchangeRate
	 */
	private Country mapToCountry(Map<String, Object> data, Map<String, Object> ratesMap) {
		String name = extractName(data);
		if (name == null || name.isBlank()) return null;

		String capital = extractCapital(data);
		String region = safeString(data.get("region"));
		Long population = extractLong(data.get("population"));
		String flagUrl = extractFlag(data);

		// currencies: handle multiple shapes
		String currencyCode = extractCurrencyCode(data);

		Double exchangeRate = null;
		Double estimatedGdp = null;

		if (currencyCode == null) {
			// spec: no currencies -> set currency_code null, exchange_rate null, estimated_gdp = 0
			currencyCode = null;
			exchangeRate = null;
			estimatedGdp = 0.0;
		} else {
			// try to find rate by code in ratesMap (keyed by currency code)
			Object rateObj = ratesMap.get(currencyCode);
			if (rateObj instanceof Number) {
				exchangeRate = ((Number) rateObj).doubleValue();
				// safety: avoid divide by zero
				if (exchangeRate == 0.0) {
					exchangeRate = null;
					estimatedGdp = null;
				} else {
					int multiplier = randomMultiplier();
					long pop = population == null ? 0L : population;
					estimatedGdp = (pop * (double) multiplier) / exchangeRate;
				}
			} else {
				// currency code not present in rates -> per spec set exchange_rate=null, estimated_gdp=null
				exchangeRate = null;
				estimatedGdp = null;
			}
		}

		Country c = Country.builder()
						   .name(name)
						   .nameNormalized(safeNormalize(name))
						   .capital(capital)
						   .region(region)
						   .population(population == null ? 0L : population)
						   .currencyCode(currencyCode)
						   .exchangeRate(exchangeRate)
						   .estimatedGdp(estimatedGdp)
						   .flagUrl(flagUrl)
						   .build();

		return c;
	}

	private String extractName(Map<String, Object> data) {
		Object nameObj = data.get("name");
		if (nameObj == null) return null;
		if (nameObj instanceof String) return (String) nameObj;
		if (nameObj instanceof Map) {
			@SuppressWarnings("unchecked")
			Map<String, Object> m = (Map<String, Object>) nameObj;
			// try v3 shape: common, official
			Object common = m.get("common");
			if (common instanceof String) return (String) common;
			Object official = m.get("official");
			if (official instanceof String) return (String) official;
			// fallback: any string value found
			for (Object v : m.values()) {
				if (v instanceof String) return (String) v;
			}
			return null;
		}
		return String.valueOf(nameObj);
	}

	private String extractCapital(Map<String, Object> data) {
		Object cap = data.get("capital");
		if (cap == null) return null;
		if (cap instanceof String) return (String) cap;
		if (cap instanceof List) {
			List<?> list = (List<?>) cap;
			if (!list.isEmpty()) return String.valueOf(list.get(0));
			return null;
		}
		// fallback
		return String.valueOf(cap);
	}

	private String extractFlag(Map<String, Object> data) {
		Object flagObj = data.get("flag"); // v2 uses "flag" often
		if (flagObj instanceof String) return (String) flagObj;
		Object flagsObj = data.get("flags"); // v3 uses "flags": { svg: ..., png: ... }
		if (flagsObj instanceof Map) {
			@SuppressWarnings("unchecked")
			Map<String, Object> flags = (Map<String, Object>) flagsObj;
			Object svg = flags.get("svg");
			if (svg instanceof String) return (String) svg;
			Object png = flags.get("png");
			if (png instanceof String) return (String) png;
		}
		return null;
	}

	private String extractCurrencyCode(Map<String, Object> data) {
		Object currenciesObj = data.get("currencies");
		if (currenciesObj == null) return null;

		// shape A: map keyed by currency code (value object with name / symbol)
		if (currenciesObj instanceof Map) {
			@SuppressWarnings("unchecked")
			Map<String, Object> currenciesMap = (Map<String, Object>) currenciesObj;
			if (currenciesMap.isEmpty()) return null;
			// first key
			return currenciesMap.keySet().iterator().next();
		}

		// shape B: list/array of objects like [{code: "NGN", name: "..."}]
		if (currenciesObj instanceof List) {
			List<?> list = (List<?>) currenciesObj;
			if (list.isEmpty()) return null;
			Object first = list.get(0);
			if (first instanceof Map) {
				Object code = ((Map<?, ?>) first).get("code");
				return code == null ? null : String.valueOf(code);
			} else {
				return String.valueOf(first);
			}
		}

		// fallback: array
		if (currenciesObj.getClass().isArray()) {
			Object[] arr = (Object[]) currenciesObj;
			if (arr.length == 0) return null;
			Object first = arr[0];
			if (first instanceof Map) {
				Object code = ((Map<?, ?>) first).get("code");
				return code == null ? null : String.valueOf(code);
			} else {
				return String.valueOf(first);
			}
		}

		return null;
	}

	private Long extractLong(Object o) {
		if (o == null) return 0L;
		if (o instanceof Number) return ((Number) o).longValue();
		try {
			return Long.parseLong(String.valueOf(o));
		} catch (Exception e) {
			return 0L;
		}
	}

	private String safeString(Object o) {
		return o == null ? null : String.valueOf(o);
	}

	private String safeNormalize(String s) {
		return s == null ? null : s.trim().toLowerCase(Locale.ROOT);
	}

	private int randomMultiplier() {
		return MIN_MULTIPLIER + rng.nextInt(MAX_MULTIPLIER - MIN_MULTIPLIER + 1);
	}
}
