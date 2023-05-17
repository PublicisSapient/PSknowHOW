package com.publicissapient.kpidashboard.common.model.comments;

import lombok.*;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CommentsInfo {

	private String commentId;
	private String commentBy;
	private String commentOn;
	private String comment;
}
