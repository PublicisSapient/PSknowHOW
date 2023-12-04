package com.publicissapient.kpidashboard.apis.rbac.userinfo.model;

import javax.validation.constraints.NotNull;

public class UserApprovalRequest {
	@NotNull
	private String status;
	@NotNull
	private String approvedBy;
	@NotNull
	private String message;

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getApprovedBy() {
		return approvedBy;
	}

	public void setApprovedBy(String approvedBy) {
		this.approvedBy = approvedBy;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}
