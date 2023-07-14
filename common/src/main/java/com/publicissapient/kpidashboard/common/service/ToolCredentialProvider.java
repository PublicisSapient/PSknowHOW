package com.publicissapient.kpidashboard.common.service;

import com.publicissapient.kpidashboard.common.model.ToolCredential;

public interface ToolCredentialProvider {

	ToolCredential findCredential(String credRef);
}
