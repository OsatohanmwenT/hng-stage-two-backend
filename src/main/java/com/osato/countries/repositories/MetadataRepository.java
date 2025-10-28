package com.osato.countries.repositories;

import com.osato.countries.models.entities.Metadata;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MetadataRepository extends JpaRepository<Metadata, String> {
}
