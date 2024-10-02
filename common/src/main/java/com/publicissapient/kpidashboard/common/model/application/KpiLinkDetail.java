package com.publicissapient.kpidashboard.common.model.application;

import lombok.Data;

import java.io.Serializable;

@Data
public class KpiLinkDetail implements Serializable {
	private String text;
	private String link;
}
