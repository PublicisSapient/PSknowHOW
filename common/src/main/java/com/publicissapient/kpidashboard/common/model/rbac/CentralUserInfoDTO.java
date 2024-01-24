package com.publicissapient.kpidashboard.common.model.rbac;

import java.util.List;

import org.joda.time.DateTime;

import com.publicissapient.kpidashboard.common.constant.AuthType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CentralUserInfoDTO {

	private String username;
	private List<String> authorities;
	private AuthType authType;
	private String firstName;
	private String middleName;
	private String lastName;
	private String displayName;
	private String createdOn;
	private String email;

	private String password;
	private Integer loginAttemptCount;
	private DateTime lastUnsuccessfulLoginTime;
	private List<UserRoleData> userRoleAssigned;
	private String userRole;
	private boolean approved;

	private String samlEmail;

}
