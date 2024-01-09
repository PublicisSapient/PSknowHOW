package com.publicissapient.kpidashboard.apis.auth.model;

import java.time.LocalDate;

import com.publicissapient.kpidashboard.common.model.rbac.UserInfoDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Class to hold request data for save action policy.
 * 
 * @author aksshriv1
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActionPoliciesDTO {
	private Long id;

	private String name;

	private String description;

	private String roleAllowed;

	private String roleActionCheck;

	private String condition;

	private LocalDate createdDate;

	private LocalDate modifiedDate;

	private UserInfoDTO createdBy;

	private UserInfoDTO modifiedBy;
}
