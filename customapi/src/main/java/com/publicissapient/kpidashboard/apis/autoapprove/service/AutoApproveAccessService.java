package com.publicissapient.kpidashboard.apis.autoapprove.service;

import com.publicissapient.kpidashboard.common.model.rbac.AutoApproveAccessConfig;

public interface AutoApproveAccessService {
	AutoApproveAccessConfig saveAutoApproveConfig(AutoApproveAccessConfig autoApproveRole);

	AutoApproveAccessConfig getAutoApproveConfig();

	AutoApproveAccessConfig modifyAutoApprovConfigById(String id, AutoApproveAccessConfig autoApproveRole);

	boolean isAutoApproveEnabled(String role);
}
