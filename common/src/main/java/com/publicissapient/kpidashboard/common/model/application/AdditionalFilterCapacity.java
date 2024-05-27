package com.publicissapient.kpidashboard.common.model.application;

import java.util.List;

import lombok.Data;

@Data
public class AdditionalFilterCapacity {
	private String filterId;
	private List<LeafNodeCapacity> nodeCapacityList;

}
