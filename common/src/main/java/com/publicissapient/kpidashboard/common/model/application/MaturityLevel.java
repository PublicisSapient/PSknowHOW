package com.publicissapient.kpidashboard.common.model.application;

import lombok.Data;

import java.util.List;

@Data
public class MaturityLevel {
    private String level;
    private List<String> range;
}
