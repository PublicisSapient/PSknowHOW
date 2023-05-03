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

/**
 * Cors constants
 * 
 * @author anisingh4
 * 
 */
public final class CORSConstants {

	public static final String HEADER_NAME_ACCESS_CONTROL_ALLOW_ORIGIN = "Access-Control-Allow-Origin";
	public static final String HEADER_VALUE_ACCESS_CONTROL_ORIGIN = "Origin";
	public static final String HEADER_NAME_ACCESS_CONTROL_ALLOW_METHODS = "Access-Control-Allow-Methods";
	public static final String HEADER_VALUE_ALLOWED_METHODS = "GET, POST, PUT, DELETE, OPTIONS, PATCH";
	public static final String HEADER_NAME_ACCESS_CONTROL_MAX_AGE = "Access-Control-Max-Age";
	public static final String HEADER_VALUE_MAX_AGE = "3600";
	public static final String HEADER_NAME_ACCESS_CONTROL_ALLOW_HEADERS = "Access-Control-Allow-Headers";
	public static final String HEADER_VALUE_ALLOWED_HEADERS = "authorization,cache-control,content-type,x-accept-filter,x-filter-id,x-filter-level,xsrf-token,selectedMap,userId,x-requested-with,request-Id";
	public static final String HEADER_NAME_ACCESS_CONTROL_EXPOSE_HEADERS = "Access-Control-Expose-Headers";
	public static final String HEADER_VALUE_EXPOSE_HEADERS = "xsrf-token, auth-details-updated";
	public static final String REQUEST_ID = "request-Id";

	private CORSConstants() {
	}
}
