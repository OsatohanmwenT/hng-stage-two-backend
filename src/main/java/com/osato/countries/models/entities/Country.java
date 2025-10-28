package com.osato.countries.models.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "countries", uniqueConstraints = @UniqueConstraint(columnNames = {"name_normalized"}))
@Getter
@Setter
@Builder
@ToString
public class Country {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String name;

	@Column(name = "name_normalized", nullable = false)
	private String nameNormalized;

	private String capital;
	private String region;

	@Column(nullable = false)
	private Long population;

	@Column(name = "currency_code")
	private String currencyCode;

	@Column(name = "exchange_rate")
	private Double exchangeRate;

	@Column(name = "estimated_gdp")
	private Double estimatedGdp;

	@Column(name = "flag_url", length = 1024)
	private String flagUrl;

	@Column(name = "last_refreshed_at")
	private Instant lastRefreshedAt;

	@PrePersist
	public void prePersist() {
		if (lastRefreshedAt == null) lastRefreshedAt = Instant.now();
	}
}
