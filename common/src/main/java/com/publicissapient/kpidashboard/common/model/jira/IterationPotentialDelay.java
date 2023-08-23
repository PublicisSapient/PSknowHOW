package com.publicissapient.kpidashboard.common.model.jira;

import java.util.Objects;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class IterationPotentialDelay {
	private String issueId;
	private String dueDate;
	private int potentialDelay;
	private String predictedCompletedDate;
	private boolean maxMarker;
	private String assigneeId;
	private String status;

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		IterationPotentialDelay that = (IterationPotentialDelay) o;
		return issueId.equals(that.issueId) && dueDate.equals(that.dueDate) && potentialDelay == that.potentialDelay
				&& predictedCompletedDate.equals(that.predictedCompletedDate) && assigneeId.equals(that.assigneeId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(issueId, dueDate, potentialDelay, predictedCompletedDate, assigneeId);
	}
}
