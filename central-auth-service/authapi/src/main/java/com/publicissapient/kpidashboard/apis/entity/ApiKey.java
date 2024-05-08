package com.publicissapient.kpidashboard.apis.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/**
 * This class serves API key provide for API call for consumer resource.
 *
 * @author Hiren Babariya
 */
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "api_key")
public class ApiKey extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "resource_id")
	private Resource resource;

	@Column(name = "key")
	private String key;

	@Column(name = "expiry_date")
	private LocalDate expiryDate;
}
