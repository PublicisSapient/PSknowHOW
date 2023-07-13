package com.publicissapient.kpidashboard.common.model.comments;

import java.util.List;

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
public class CommentSubmitDTO {

	private String node;
	private String level;
	private String nodeChildId;
	private String kpiId;
	private List<CommentsInfo> commentsInfo;

}
