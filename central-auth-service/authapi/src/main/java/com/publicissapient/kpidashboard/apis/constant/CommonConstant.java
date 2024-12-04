/*******************************************************************************
 * Copyright 2014 CapitalOne, LLC.
 * Further development Copyright 2022 Sapient Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/
package com.publicissapient.kpidashboard.apis.constant;

public final class CommonConstant {
	public static final String ROOT = "Root";
	public static final String PATH_SEPARATOR = ".";
	public static final String SUPER_ADMIN = "SUPERADMIN";
	public static final String ACCESS_REQUEST_STATUS_PENDING = "Pending";
	public static final String ACCESS_REQUEST_STATUS_APPROVED = "Approved";
	public static final String ACCESS_REQUEST_STATUS_REJECTED = "Rejected";
	public static final String ROLE_PROJECT_ADMIN = "ROLE_PROJECT_ADMIN";
	public static final String SUBJECT = "sub";
	public static final String ROLE_SUPERADMIN = "ROLE_SUPERADMIN";
	public static final String APPROVAL_SUCCESS_TEMPLATE_KEY = "Approve_User_Success";
	public static final String APPROVAL_NOTIFICATION_KEY = "approvalRequest";
	public static final String APPROVAL_REJECT_TEMPLATE_KEY = "Approve_User_Reject";
	public static final String APPROVAL_REJECT_NOTIFICATION_KEY = "approvalRequest";
	public static final String PRE_APPROVAL_NOTIFICATION_SUBJECT_KEY = "preApproval";
	public static final String PRE_APPROVAL_NOTIFICATION_KEY = "Pre_Approval";
	public static final String USER_VERIFICATION_TEMPLATE_KEY = "User_Verification";
	public static final String USER_VERIFICATION_FAILED_TEMPLATE_KEY = "User_Verification_Failed";
	public static final String USER_VERIFICATION_NOTIFICATION_KEY = "userVerification";
	public static final String USER_VERIFICATION_FAILED_NOTIFICATION_KEY = "userVerificationFailed";
	public static final String AUTH_RESPONSE_HEADER = "X-Authentication-Token";
	public static final String SUCCESS_STATUS = "Success";
	public static final String EMAIL_PATTERN = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
	public static final String PASSWORD_PATTERN = "((?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[$@$!%*?&]).{8,20})";
	public static final String USERNAME_PATTERN = "^[a-zA-Z0-9]{3,30}$"; // Username can only contain letters and numbers
	public static final String WRONG_CREDENTIALS_ERROR_MESSAGE = "Login Failed: The username or password entered is incorrect";
	// Maximum length is 30 and min is 3 characters
	public static final String SUCCESS_VALID_TOKEN = "success_valid_token";
	public static final String ERROR_INVALID_USER = "error_invalid_user";
	public static final String SUCCESS_DELETE_TOKEN = "success_delete_token";
	public static final String ERROR_UNAUTHORIZED_USER = "error_unauthorized_user";
	public static final String SUCCESS_LOGIN = "success_login";
	public static final String SUCCESS_SENT_APPROVAL = "success_sent_approval";
	public static final String ERROR_REGISTER_AGAIN = "error_register_again";

	private CommonConstant() {
	}
}
