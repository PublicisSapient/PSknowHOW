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

package com.publicissapient.kpidashboard.apis.common.rest;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.BadRequestException;

import org.apache.commons.lang3.NotImplementedException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.publicissapient.kpidashboard.apis.auth.exceptions.DeleteLastAdminException;
import com.publicissapient.kpidashboard.apis.auth.exceptions.InvalidAuthTypeConfigException;
import com.publicissapient.kpidashboard.apis.auth.exceptions.PendingApprovalException;
import com.publicissapient.kpidashboard.apis.auth.exceptions.UserNotFoundException;
import com.publicissapient.kpidashboard.apis.errors.ProjectNotFoundException;
import com.publicissapient.kpidashboard.apis.errors.ToolNotFoundException;
import com.publicissapient.kpidashboard.apis.model.ErrorResponse;
import com.publicissapient.kpidashboard.apis.model.ServiceResponse;
import com.publicissapient.kpidashboard.apis.pushdata.util.PushDataException;
import com.publicissapient.kpidashboard.common.exceptions.ApplicationException;
import com.publicissapient.kpidashboard.common.util.UnsafeDeleteException;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller advice to handle exceptions globally.
 */
@EnableWebMvc
@ControllerAdvice(annotations = RestController.class)
@Slf4j
public class RestApiExceptionHandler extends ResponseEntityExceptionHandler {
	public static final String ERROR_CODE_STR = "Error code=: ";

	/**
	 * 
	 * @param ex
	 * @param body
	 * @param headers
	 * @param status
	 * @param request
	 * @return {@code ResponseEntity<Object>}
	 */
	@Override
	protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers,
															 HttpStatusCode status, WebRequest request) {
		headers.setContentType(MediaType.APPLICATION_JSON);
		log.error(ex.getMessage());
		ErrorResponse errorResponse = createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
				HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), "Please try after some time.");

		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
	}

	/**
	 * 
	 * @param ex
	 * @param headers
	 * @param status
	 * @param request
	 * @return {@code ResponseEntity<Object>}
	 */
	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
			HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		headers.setContentType(MediaType.APPLICATION_JSON);
		log.warn("Bad Request - bind exception: ", ex);
		return new ResponseEntity<>(ErrorResponse.fromBindException(ex), headers, status);
	}

	/**
	 * Handles Unrecognized property exception
	 * 
	 * @param ex
	 * @param request
	 * @return {@code ResponseEntity<ErrorResponse>}
	 */
	@ExceptionHandler(UnrecognizedPropertyException.class)
	public ResponseEntity<ErrorResponse> handleUnrecognizedProperty(UnrecognizedPropertyException ex,
			HttpServletRequest request) {
		ErrorResponse response = new ErrorResponse();
		response.addFieldError(ex.getPropertyName(), ex.getMessage());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
	}

	/**
	 * 
	 * @param ex
	 * @param request
	 * @return {@code ResponseEntity<Map<String, String>>}
	 */
	@ExceptionHandler(UnsafeDeleteException.class)
	protected ResponseEntity<Map<String, String>> handleUnsafeDelete(UnsafeDeleteException ex,
			HttpServletRequest request) {
		log.error(ex.getMessage());
		Map<String, String> errorResponse = new HashMap<>();
		errorResponse.put("errorMessage", ex.getMessage());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
	}

	/**
	 * Handles Runtime Exception
	 * 
	 * @param exeption
	 * @return {@code ResponseEntity<Object>}
	 */
	@ExceptionHandler(RuntimeException.class)
	public ResponseEntity<Object> handleConflict(RuntimeException exeption) {
		log.error(exeption.getMessage(), exeption);
		ErrorResponse errorResponse = createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
				HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), "Please try after some time.");

		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
	}

	/**
	 * Handles Speedy Exceptions
	 * 
	 * @param applicationException
	 * @return {@code ResponseEntity<Object>}
	 */
	@ExceptionHandler(ApplicationException.class)
	public ResponseEntity<Object> handleSpeedyException(ApplicationException applicationException) {
		log.error(applicationException.getMessage(), applicationException);
		switch (applicationException.getErrorCode()) {
		case ApplicationException.ERROR_INSERTING_DATA:
		case ApplicationException.COLLECTOR_CREATE_ERROR:
		case ApplicationException.COLLECTOR_ITEM_CREATE_ERROR:

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
							HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
							"Internal logError." + applicationException.getMessage() + ERROR_CODE_STR
									+ applicationException.getErrorCode()));
		case ApplicationException.DUPLICATE_DATA:
		case ApplicationException.JSON_FORMAT_ERROR:
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(createErrorResponse(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(),
							"Bad request. " + applicationException.getMessage() + ERROR_CODE_STR
									+ applicationException.getErrorCode()));
		case ApplicationException.NOTHING_TO_UPDATE:
			return ResponseEntity.status(HttpStatus.NOT_MODIFIED)
					.body(createErrorResponse(HttpStatus.NOT_MODIFIED.value(),
							HttpStatus.NOT_MODIFIED.getReasonPhrase(),
							"Internal logError. " + applicationException.getMessage() + ERROR_CODE_STR
									+ applicationException.getErrorCode()));
		default:
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
							HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
							"Internal logError. " + applicationException.getMessage() + ERROR_CODE_STR
									+ applicationException.getErrorCode()));
		}
	}

	/**
	 * Handles Access Denied Exception
	 * 
	 * @param accessDeniedException
	 * @return {@code ResponseEntity<Object>}
	 */
	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<Object> handleAccessDenied(AccessDeniedException accessDeniedException) {

		ErrorResponse errorResponse = createErrorResponse(HttpStatus.FORBIDDEN.value(),
				HttpStatus.FORBIDDEN.getReasonPhrase(), "Access Denied.");
		return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
	}

	/**
	 * Handles Delete Last Admin Exception
	 * 
	 * @param deleteLastAdminException
	 * @return {@code ResponseEntity<Object>}
	 */
	@ExceptionHandler(DeleteLastAdminException.class)
	public ResponseEntity<Object> handleDeletingLastAdmin(DeleteLastAdminException deleteLastAdminException) {
		ErrorResponse errorResponse = createErrorResponse(HttpStatus.BAD_REQUEST.value(),
				HttpStatus.BAD_REQUEST.getReasonPhrase(), deleteLastAdminException.getMessage());

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
	}

	/**
	 * Handles UserNotFound exception
	 * 
	 * @param exception
	 * @return {@code ResponseEntity<Object>}
	 */
	@ExceptionHandler(UserNotFoundException.class)
	public ResponseEntity<Object> handleUserNotFound(UserNotFoundException exception) {
		ErrorResponse errorResponse = createErrorResponse(HttpStatus.NOT_FOUND.value(),
				HttpStatus.NOT_FOUND.getReasonPhrase(), exception.getMessage());

		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
	}

	@ExceptionHandler(BadRequestException.class)
	public ResponseEntity<Object> handleBadRequestException(BadRequestException exception) {
		ErrorResponse errorResponse = createErrorResponse(HttpStatus.BAD_REQUEST.value(),
				HttpStatus.BAD_REQUEST.getReasonPhrase(), exception.getMessage());

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<Object> handleBadRequestException(IllegalArgumentException exception) {
		ErrorResponse errorResponse = createErrorResponse(HttpStatus.BAD_REQUEST.value(),
				HttpStatus.BAD_REQUEST.getReasonPhrase(), exception.getMessage());

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
	}

	@ExceptionHandler(IllegalStateException.class)
	public ResponseEntity<Object> handleBadRequestException(IllegalStateException exception) {
		ErrorResponse errorResponse = createErrorResponse(HttpStatus.BAD_REQUEST.value(),
				HttpStatus.BAD_REQUEST.getReasonPhrase(), exception.getMessage());

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
	}

	@ExceptionHandler(NotImplementedException.class)
	public ResponseEntity<Object> handleBadRequestException(NotImplementedException exception) {
		ErrorResponse errorResponse = createErrorResponse(HttpStatus.NOT_IMPLEMENTED.value(),
				HttpStatus.NOT_IMPLEMENTED.getReasonPhrase(), exception.getMessage());

		return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(errorResponse);
	}

	@ExceptionHandler(ToolNotFoundException.class)
	public ResponseEntity<Object> handleInvalidToolException(ToolNotFoundException exception) {
		ErrorResponse errorResponse = createErrorResponse(HttpStatus.NOT_FOUND.value(),
				HttpStatus.NOT_FOUND.getReasonPhrase(), exception.getMessage());

		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
	}

	@ExceptionHandler(ProjectNotFoundException.class)
	public ResponseEntity<Object> handleProjectNotFound(ProjectNotFoundException exception) {
		ErrorResponse errorResponse = createErrorResponse(HttpStatus.NOT_FOUND.value(),
				HttpStatus.NOT_FOUND.getReasonPhrase(), exception.getMessage());

		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
	}

	@ExceptionHandler(PendingApprovalException.class)
	public ResponseEntity<Object> handlePendingApprovalException(PendingApprovalException exception) {
		ErrorResponse errorResponse = createErrorResponse(HttpStatus.ACCEPTED.value(),
				HttpStatus.ACCEPTED.getReasonPhrase(), exception.getMessage());

		return ResponseEntity.status(HttpStatus.ACCEPTED).body(errorResponse);
	}

	@ExceptionHandler(InvalidAuthTypeConfigException.class)
	public ResponseEntity<Object> handleInvalidAuthTypeConfigException(
			InvalidAuthTypeConfigException invalidAuthTypeConfigException) {

		ErrorResponse errorResponse = createErrorResponse(HttpStatus.BAD_REQUEST.value(),
				HttpStatus.BAD_REQUEST.getReasonPhrase(), invalidAuthTypeConfigException.getMessage());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
	}

	@ExceptionHandler(PushDataException.class)
	protected ResponseEntity<Object> handlePushDataExceptions(PushDataException ex) {
		log.error(ex.getMessage());
		if (ex.getPushBuildDeployResponse() != null) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(new ServiceResponse(false, ex.getMessage(), ex.getPushBuildDeployResponse()));
		}
		if (ex.getCode() != null) {
			return ResponseEntity.status(ex.getCode())
					.body(new ServiceResponse(false, ex.getMessage(), ex.getPushBuildDeployResponse()));
		}
		Map<String, String> errorResponse = new HashMap<>();
		errorResponse.put("error", ex.getMessage());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
	}

	private ErrorResponse createErrorResponse(int code, String error, String message) {
		ErrorResponse errorResponse = new ErrorResponse();
		errorResponse.setCode(code);
		errorResponse.setError(error);
		errorResponse.setMessage(message);
		return errorResponse;
	}
}
