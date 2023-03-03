package com.publicissapient.kpidashboard.common.model.jira;

import com.publicissapient.kpidashboard.common.model.generic.BasicModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Set;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Document(collection = "assignee_details")
public class AssigneeDetails extends BasicModel {

	private String basicProjectConfigId;
	private String source;
	private Set<Assignee> assignee;
}
