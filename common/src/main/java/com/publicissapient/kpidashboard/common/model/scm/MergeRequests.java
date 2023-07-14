package com.publicissapient.kpidashboard.common.model.scm;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import com.publicissapient.kpidashboard.common.model.generic.BasicModel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author yasbano
 * 
 *         Represents the merge requests in a repository.
 */

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "merge_requests")

public class MergeRequests extends BasicModel {
	private ObjectId processorItemId;
	private String title;
	private String state;
	private Long count;
	private boolean isOpen;
	private boolean isClosed;
	private long createdDate;
	private long updatedDate;
	private long closedDate;
	private String fromBranch;
	private String toBranch;
	private String repoSlug;
	private String projKey;
	private String author;
	private List<String> reviewers;
	private String revisionNumber;
	private String date;
}
