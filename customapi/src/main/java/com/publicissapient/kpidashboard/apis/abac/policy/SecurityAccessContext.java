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

package com.publicissapient.kpidashboard.apis.abac.policy;

public class SecurityAccessContext {
	private Object projectAccessManager;
	private Object subject;
	private Object resource;
	private Object action;
	private Object environment;

	public SecurityAccessContext(Object projectAccessManager, Object subject, Object resource, Object action,
			Object environment) {
		super();
		this.projectAccessManager = projectAccessManager;
		this.subject = subject;
		this.resource = resource;
		this.action = action;
		this.environment = environment;
	}

	public Object getProjectAccessManager() {
		return projectAccessManager;
	}

	public void setProjectAccessManager(Object projectAccessManager) {
		this.projectAccessManager = projectAccessManager;
	}

	public Object getSubject() {
		return subject;
	}

	public void setSubject(Object subject) {
		this.subject = subject;
	}

	public Object getResource() {
		return resource;
	}

	public void setResource(Object resource) {
		this.resource = resource;
	}

	public Object getAction() {
		return action;
	}

	public void setAction(Object action) {
		this.action = action;
	}

	public Object getEnvironment() {
		return environment;
	}

	public void setEnvironment(Object environment) {
		this.environment = environment;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((action == null) ? 0 : action.hashCode());
		result = prime * result + ((environment == null) ? 0 : environment.hashCode());
		result = prime * result + ((resource == null) ? 0 : resource.hashCode());
		result = prime * result + ((subject == null) ? 0 : subject.hashCode());
		result = prime * result + ((projectAccessManager == null) ? 0 : projectAccessManager.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		SecurityAccessContext that = (SecurityAccessContext) o;

		if (!projectAccessManager.equals(that.projectAccessManager)) {
			return false;
		}
		if (!subject.equals(that.subject)) {
			return false;
		}
		if (!resource.equals(that.resource)) {
			return false;
		}
		if (!action.equals(that.action)) {
			return false;
		}
		return environment.equals(that.environment);
	}
}
