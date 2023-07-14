package com.publicissapient.kpidashboard.common.model.application;

import java.util.List;

import lombok.Data;
import lombok.Setter;
import lombok.ToString;

@Data
@Setter
@ToString
public class ProjectWiseData {
	List<DataCount> value;
	private String data;
}
