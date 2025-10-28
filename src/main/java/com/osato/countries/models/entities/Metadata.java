package com.osato.countries.models.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "metadata")
@Getter
@Setter
@NoArgsConstructor
public class Metadata {
	@Id
	@Column(name = "key_name", length = 64)
	private String keyName;

	@Column(name = "value_text", columnDefinition = "TEXT")
	private String valueText;

	@Column(name = "updated_at")
	private Instant updatedAt;

	@PrePersist
	@PreUpdate
	public void setUpdatedAt() {
		updatedAt = Instant.now();
	}
}
