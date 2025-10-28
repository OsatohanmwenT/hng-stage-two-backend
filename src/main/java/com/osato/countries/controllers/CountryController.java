package com.osato.countries.controllers;

import com.osato.countries.config.ExternalApiException;
import com.osato.countries.models.dtos.CountryDto;
import com.osato.countries.models.dtos.StatusResponse;
import com.osato.countries.services.CountryService;
import com.osato.countries.services.CountryWebClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping
public class CountryController {
	private final CountryWebClientService  countryWebClientService;
	private final CountryService countryService;

	@PostMapping("/countries/refresh")
	public ResponseEntity<?> refresh() {
		try {
			int processed = countryWebClientService.syncAllCountries();
			return ResponseEntity.ok(Map.of(
					"success", true,
					"processed", processed,
					"last_refreshed_at", Instant.now().toString()
			));
		} catch (ExternalApiException e) {
			// GlobalExceptionHandler will also handle this, but returning here keeps explicit behavior
			return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of(
					"error", "External data source unavailable",
					"details", "Could not fetch data from " + e.getMessage()
			));
		} catch (Exception e) {
			log.error("Refresh failed", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Internal server error"));
		}
	}

	@GetMapping("/countries")
	public ResponseEntity<List<CountryDto>> getCountries(
			@RequestParam(required = false) String region,
			@RequestParam(required = false) String currency,
			@RequestParam(required = false) String sort
	) {
		return ResponseEntity.ok(countryService.getCountries(region, currency, sort));
	}

	@GetMapping("/countries/{name}")
	public ResponseEntity<CountryDto> getCountryByName(@PathVariable String name) {
		return ResponseEntity.ok(countryService.getByName(name));
	}

	@DeleteMapping("/countries/{name}")
	public ResponseEntity<String> deleteCountry(@PathVariable String name) {
		countryService.deleteByName(name);
		return ResponseEntity.ok("Country deleted successfully: " + name);
	}

	@GetMapping("/status")
	public ResponseEntity<StatusResponse> getStatus() {
		return ResponseEntity.ok(countryService.getStatus());
	}

	@GetMapping(value = "/countries/image", produces = MediaType.IMAGE_PNG_VALUE)
	public ResponseEntity<String> getSummaryImage() throws Exception {
		try {
			countryService.generateSummaryImage();
			return ResponseEntity.ok("Summary image generated successfully.");
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
								 .body("Error generating image: " + e.getMessage());
		}
	}
}
