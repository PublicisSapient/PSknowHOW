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

package com.publicissapient.kpidashboard.common.context;

import java.io.Serializable;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import org.jboss.logging.MDC;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.common.constant.CommonConstant;

@Component
public class ExecutionLogContext implements Serializable {

	private static final long serialVersionUID = -6751490154133933000L;
	// Atomic integer containing the next thread ID to be assigned
	private static final AtomicInteger nextId = new AtomicInteger(0);

	private static final String CONTEXT_ERROR = "Unauthorize to access.";
	private static final ThreadLocal<ExecutionLogContext> EXECUTION_CONTEXT = new ThreadLocal<ExecutionLogContext>() {

		@Override
		protected ExecutionLogContext initialValue() {
			return new ExecutionLogContext(nextId.getAndIncrement());
		}
	};
	private String requestId;
	private String environment;
	private String projectName;
	private String projectBasicConfgId;
	private String isCron;
	private int threadId;

	public ExecutionLogContext() {
	}

	private ExecutionLogContext(int threadId) {
		this.threadId = threadId;
	}

	public static synchronized ExecutionLogContext getContext() {

		if (Objects.isNull(EXECUTION_CONTEXT.get())) {
			EXECUTION_CONTEXT.set(new ExecutionLogContext(nextId.getAndIncrement()));
		}
		return EXECUTION_CONTEXT.get();
	}

	public static void set(ExecutionLogContext executionContextUtil) {
		if (Objects.nonNull(executionContextUtil)) {
			EXECUTION_CONTEXT.set(executionContextUtil);
		}
	}

	public static ExecutionLogContext updateContext(ExecutionLogContext context) {
		ExecutionLogContext currentContext = ExecutionLogContext.getContext();
		currentContext.setRequestId(context.getRequestId());
		currentContext.setThreadId(context.getThreadId());
		currentContext.setEnvironment(context.getEnvironment());
		currentContext.setProjectName(context.getProjectName());
		currentContext.setProjectBasicConfgId(context.getProjectBasicConfgId());
		currentContext.setIsCron(context.getIsCron());
		return currentContext;
	}

	public String getProjectBasicConfgId() {
		return projectBasicConfgId;
	}

	public void setProjectBasicConfgId(String projectBasicConfgId) {
		MDC.put(CommonConstant.PROJECT_CONFIG_ID, projectBasicConfgId);
		this.projectBasicConfgId = projectBasicConfgId;
	}

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		MDC.put(CommonConstant.PROJECTNAME, projectName);
		this.projectName = projectName;
	}

	public String getIsCron() {
		return isCron;
	}

	public void setIsCron(String isCron) {
		MDC.put(CommonConstant.CRON, isCron);
		this.isCron = isCron;
	}

	public String getEnvironment() {
		return environment;
	}

	public void setEnvironment(String environment) {
		MDC.put(CommonConstant.ENVIRONMENT, environment);
		this.environment = environment;
	}

	public void destroy() {
		EXECUTION_CONTEXT.remove();
	}

	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(String requestId) {
		MDC.put(CommonConstant.REQUESTID, requestId);
		this.requestId = requestId;
	}

	public int getThreadId() {
		return threadId;
	}

	public void setThreadId(int threadId) {
		this.threadId = threadId;
	}

}