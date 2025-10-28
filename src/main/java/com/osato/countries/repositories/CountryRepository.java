package com.osato.countries.repositories;

import com.osato.countries.models.entities.Country;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CountryRepository extends JpaRepository<Country, Long> {
	Optional<Country> findByNameNormalized(String nameNormalized);
	List<Country> findByRegion(String region);
	List<Country> findByCurrencyCode(String currencyCode);
	@Query("SELECT c FROM Country c ORDER BY c.estimatedGdp DESC")
	List<Country> findTop5ByGdp(org.springframework.data.domain.Pageable pageable);
}