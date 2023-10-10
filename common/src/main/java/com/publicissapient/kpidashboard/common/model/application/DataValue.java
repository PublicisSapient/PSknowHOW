package com.publicissapient.kpidashboard.common.model.application;

import java.io.Serializable;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class DataValue implements Serializable {

	private String name;
	private String lineType;
	private String data;
	private Object value;
	private Map<String, Object> hoverValue;

}
