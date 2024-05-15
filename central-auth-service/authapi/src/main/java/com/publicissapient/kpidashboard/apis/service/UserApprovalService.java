package com.publicissapient.kpidashboard.apis.service;

import java.util.List;

import com.publicissapient.kpidashboard.apis.errors.UserNotFoundException;
import com.publicissapient.kpidashboard.apis.service.dto.UnapprovedUserDTO;


public interface UserApprovalService {
	List<UnapprovedUserDTO> findAllUnapprovedUsers();

	boolean approveUser(String username) throws UserNotFoundException;

	boolean rejectUser(String username);
}
