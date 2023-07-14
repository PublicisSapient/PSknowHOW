package com.publicissapient.kpidashboard.common.model.application;

import java.time.LocalDate;

import lombok.Data;

@Data
public class Week {
	private LocalDate startDate;
	private LocalDate endDate;
}
