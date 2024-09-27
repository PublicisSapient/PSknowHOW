package com.publicissapient.kpidashboard.apis.service;

import java.util.List;

import com.publicissapient.kpidashboard.apis.errors.UsernameNotFoundException;
import com.publicissapient.kpidashboard.apis.service.dto.UnapprovedUserDTO;


public interface UserApprovalService {
	List<UnapprovedUserDTO> findAllUnapprovedUsers();

	boolean approveUser(String username) throws UsernameNotFoundException;

	boolean rejectUser(String username);
}
