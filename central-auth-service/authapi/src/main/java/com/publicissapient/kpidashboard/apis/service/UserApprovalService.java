package com.publicissapient.kpidashboard.apis.service;

import com.publicissapient.kpidashboard.common.model.UserAccessRequest;

import java.util.List;

public interface UserApprovalService {
	List<UserAccessRequest> findAllUnapprovedUsers();

	boolean approveUser(String username);

	boolean rejectUser(String username);
}
