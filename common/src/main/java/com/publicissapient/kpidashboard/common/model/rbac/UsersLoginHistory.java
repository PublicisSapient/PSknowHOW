package com.publicissapient.kpidashboard.common.model.rbac;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "users_login_history")
public class UsersLoginHistory {

	private ObjectId userId;
	private String userName;
	private String emailId;
	private String loginType;
	private String dateAndTime;
	private String status;

}
