package com.publicissapient.kpidashboard.common.model.rbac;

import java.util.List;

import org.bson.types.ObjectId;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AutoApproveAccessConfigDTO {
	private ObjectId id;
	private String enableAutoApprove;
	private List<RoleData> roles;

	@Override
	public String toString() {
		return "AutoAccessApprovalDTO [isAutoApproved=" + enableAutoApprove + ", roles=" + roles + "]";
	}

}
