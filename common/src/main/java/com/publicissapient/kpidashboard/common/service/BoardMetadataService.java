package com.publicissapient.kpidashboard.common.service;

import java.util.List;

import com.publicissapient.kpidashboard.common.model.jira.BoardMetadata;

/**
 * @author shunaray
 */
public interface BoardMetadataService {

	List<BoardMetadata> findAll();
}
