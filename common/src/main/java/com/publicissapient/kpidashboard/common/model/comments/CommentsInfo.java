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
public class CommentsInfo {

	private String commentId;
	private String commentBy;
	private String commentOn;
	private String comment;
	private boolean isDeleted; //soft delete on comment history
}
