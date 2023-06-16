package com.publicissapient.kpidashboard.common.model.application;

import lombok.Data;

import java.time.LocalDate;

@Data
public class Week {
    private LocalDate startDate;
    private LocalDate endDate;
}
