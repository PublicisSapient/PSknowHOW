package com.publicissapient.kpidashboard.jiratest.processor.service.impl;

import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.common.model.ToolCredential;
import com.publicissapient.kpidashboard.common.service.ToolCredentialProvider;

@Service
public class JiraTestToolCredentialProvider implements ToolCredentialProvider {
	@Override
	public ToolCredential findCredential(String credRef) {

		return null;
	}
}
