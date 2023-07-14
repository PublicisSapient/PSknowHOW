package com.publicissapient.kpidashboard.common.model.rbac;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "auto_approve_access_config")
public class AutoApproveAccessConfig {
	@Id
	private ObjectId id;

	@Field("enableAutoApprove")
	private String enableAutoApprove;

	@Field("roles")
	private List<RoleData> roles;

}
