package com.publicissapient.kpidashboard.common.model.application;

import com.publicissapient.kpidashboard.common.model.generic.BasicModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "hierarchy_master")
public class HierarchyMaster extends BasicModel implements Serializable {

	private static final long serialVersionUID = 67050747445127809L;

	// UniqueId of Central Hierarchy for Each Node
	@Indexed(unique = true)
	private String nodeId;

	private String nodeName;

	private String nodeDisplayName;

	// same as labelName in Account Hierarchy
	// match with HierarchyLevel.hierarchyLevelId
	private String levelId;
	private String parentId;

	// Hierarchy Level of Node
	private int level;

	private LocalDateTime createdDate;
	private LocalDateTime modifiedDate;

}
