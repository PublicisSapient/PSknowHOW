package com.publicissapient.kpidashboard.common.model.application;

import java.io.Serializable;
import java.time.LocalDateTime;

import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.publicissapient.kpidashboard.common.model.generic.BasicModel;

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
@Document(collection = "organization_hierarchy")
public class OrganizationHierarchy extends BasicModel implements Serializable {

	private static final long serialVersionUID = 67050747445127809L;

	// UniqueId of Central Hierarchy for Each Node
	@Indexed(unique = true)
	private String nodeId;

	private String nodeName;

	private String nodeDisplayName;

	// Todo same as labelName in Account Hierarchy
	private String hierarchyLevelId;

	@Indexed(unique = true)
	private String parentId;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private LocalDateTime createdDate;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private LocalDateTime modifiedDate;

	public boolean equals(Object obj) {
		return super.equals(obj);
	}

}
