package com.publicissapient.kpidashboard.apis.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@NoArgsConstructor
@Getter
@Setter
public class CustomDateRange {
	private LocalDate startDate;
	private LocalDate endDate;
	private LocalDateTime startDateTime;
	private LocalDateTime endDateTime;
}
