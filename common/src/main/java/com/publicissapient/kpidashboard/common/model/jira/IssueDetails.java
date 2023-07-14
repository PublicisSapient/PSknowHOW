package com.publicissapient.kpidashboard.common.model.jira;

import com.google.common.base.Objects;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class IssueDetails {

	private SprintIssue sprintIssue;
	private String url;
	private String desc;

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof IssueDetails))
			return false;
		IssueDetails that = (IssueDetails) o;
		return Objects.equal(sprintIssue, that.sprintIssue) && Objects.equal(url, that.url)
				&& Objects.equal(desc, that.desc);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(sprintIssue, url, desc);
	}
}
