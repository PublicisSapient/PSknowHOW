package com.publicissapient.kpidashboard.apis.model;

import java.time.LocalDate;

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
}
