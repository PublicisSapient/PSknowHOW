package com.publicissapient.kpidashboard.common.model.jira;

import java.time.LocalDateTime;

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
public class JiraHistoryChangeLog {

	private String changedFrom;
	private String changedTo;
	private LocalDateTime updatedOn;

}
