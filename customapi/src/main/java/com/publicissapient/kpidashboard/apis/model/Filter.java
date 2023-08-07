package com.publicissapient.kpidashboard.apis.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Filter {
	String filterKey;
	String filterName;
	String filterType;
	List<String> options;
}
