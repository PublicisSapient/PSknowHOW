package com.publicissapient.kpidashboard.apis.model;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DateRangeFilter {
	List<String> types;
	List<Integer> counts;

	public DateRangeFilter(List<String> types, List<Integer> counts) {
		this.types = types;
		this.counts = counts;
	}

}
