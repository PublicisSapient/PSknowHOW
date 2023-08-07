package com.publicissapient.kpidashboard.common.model.comments;

import java.util.List;

import org.springframework.data.mongodb.core.mapping.Document;

import com.publicissapient.kpidashboard.common.model.generic.BasicModel;

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
@Document(collection = "kpi_comments_history")
public class KpiCommentsHistory extends BasicModel {
	private String node;
	private String level;
	private String nodeChildId;
	private String kpiId;
	private List<CommentsInfo> commentsInfo;
}
