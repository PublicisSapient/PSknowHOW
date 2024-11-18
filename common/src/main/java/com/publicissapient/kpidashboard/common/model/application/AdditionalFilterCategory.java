package com.publicissapient.kpidashboard.common.model.application;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document("additional_filter_categories")
public class AdditionalFilterCategory {
	private int level;
	private String filterCategoryId;
	private String filterCategoryName;

}
