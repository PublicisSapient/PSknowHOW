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
	private String requestId;
	private String userName;
	private String environment;
	private String projectName;
	private String projectBasicConfgId;
	private String isCron;
	private int threadId;

	private static final ThreadLocal<ExecutionLogContext> EXECUTION_CONTEXT = new ThreadLocal<ExecutionLogContext>() {

		@Override
		protected ExecutionLogContext initialValue() {
			return new ExecutionLogContext(nextId.getAndIncrement());
		}
	};

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		MDC.put(CommonConstant.USER_NAME, userName);
		this.userName = userName;
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

	public ExecutionLogContext() {
	}


	public void setThreadId(int threadId) {
		this.threadId = threadId;
	}

	private ExecutionLogContext(int threadId) {
		this.threadId = threadId;
	}

	public void destroy() {
		EXECUTION_CONTEXT.remove();
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

	public String getRequestId() {
		return requestId;
	}

	public int getThreadId() {
		return threadId;
	}

	public void setRequestId(String requestId) {
		MDC.put(CommonConstant.REQUESTID, requestId);
		this.requestId = requestId;
	}

	public static ExecutionLogContext updateContext(ExecutionLogContext context) {
		ExecutionLogContext currentContext = ExecutionLogContext.getContext();
		currentContext.setRequestId(context.getRequestId());
		currentContext.setThreadId(context.getThreadId());
		currentContext.setEnvironment(context.getEnvironment());
		currentContext.setUserName(context.getUserName());
		currentContext.setProjectName(context.getProjectName());
		currentContext.setProjectBasicConfgId(context.getProjectBasicConfgId());
		currentContext.setIsCron(context.getIsCron());
		return currentContext;
	}

}