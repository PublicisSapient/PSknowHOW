package com.publicissapient.kpidashboard.common.model.jira;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

/**
 * user assignee details
 */

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Assignee {

	private String assigneeId;
	private String assigneeName;

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof Assignee))
			return false;

		Assignee assignee = (Assignee) o;

		return Objects.equals(assigneeId, assignee.assigneeId);
	}

	@Override
	public int hashCode() {
		return assigneeId != null ? assigneeId.hashCode() : 0;
	}
}
