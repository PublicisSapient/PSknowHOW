package com.publicissapient.kpidashboard.common.model.comments;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CommentViewResponseDTO {

	private String node;
	private String level;
	private String nodeChildId;
	private String kpiId;
	private String commentId;
	private String commentBy;
	private String commentOn;
	private String comment;
}
