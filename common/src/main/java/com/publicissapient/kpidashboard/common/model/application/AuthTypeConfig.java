package com.publicissapient.kpidashboard.common.model.application;

import com.publicissapient.kpidashboard.common.activedirectory.modal.ADServerDetail;

import lombok.Data;

@Data
public class AuthTypeConfig {

	private AuthTypeStatus authTypeStatus;
	private ADServerDetail adServerDetail;
}
