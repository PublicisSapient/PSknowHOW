package com.publicissapient.kpidashboard.common.model.comments;

import lombok.*;

import java.util.List;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CommentSubmitDTO {

	private String node;
	private String level;
	private String sprintId;
	private List<CommentsKpiWise> commentsKpiWise;

}
