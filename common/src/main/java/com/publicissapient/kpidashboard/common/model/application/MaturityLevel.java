package com.publicissapient.kpidashboard.common.model.application;

import java.util.List;

import lombok.Data;

@Data
public class MaturityLevel {
	private String level;
	private List<String> range;
	private String bgColor;
	private String label;
	private String displayRange;
}
