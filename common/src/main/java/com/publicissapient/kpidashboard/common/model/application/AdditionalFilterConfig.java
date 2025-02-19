package com.publicissapient.kpidashboard.common.model.application;

import java.util.Objects;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;

import lombok.Data;

@Data
public class AdditionalFilterConfig {
	private String filterId;
	private String identifyFrom;
	private String identificationField;
	private Set<String> values;

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		AdditionalFilterConfig that = (AdditionalFilterConfig) o;
		return filterId.equalsIgnoreCase(that.filterId) && identifyFrom.equalsIgnoreCase(that.identifyFrom) &&
				identificationField.equalsIgnoreCase(that.identificationField) &&
				CollectionUtils.isEqualCollection(that.values, values);
	}

	@Override
	public int hashCode() {
		return Objects.hash(filterId, identifyFrom, identificationField, values);
	}
}
