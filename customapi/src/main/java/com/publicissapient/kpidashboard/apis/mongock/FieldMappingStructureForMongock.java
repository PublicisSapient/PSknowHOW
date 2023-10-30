package com.publicissapient.kpidashboard.apis.mongock;

import java.util.List;

import com.publicissapient.kpidashboard.common.model.application.BaseFieldMappingStructure;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FieldMappingStructureForMongock {
	private String fieldName;
	private String fieldLabel;
	private String fieldType;
	private String fieldCategory;
	private String toggleLabel;
	private String section;
	private boolean processorCommon;
	private MappingToolTip tooltip;
	private List<Options> options;
	private List<String> filterGroup;
	private List<BaseFieldMappingStructure> nestedFields;

	@Data
	@Getter
	@Setter
	static class MappingToolTip {
		String definition;
		String kpiImpacted;
		String toggleDefinition;
	}

	@Data
	@Getter
	@Setter
	@NoArgsConstructor
	public static class Options {
		String label;
		Object value;
	}
}
