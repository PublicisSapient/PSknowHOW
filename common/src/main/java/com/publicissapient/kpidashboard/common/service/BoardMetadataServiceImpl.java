package com.publicissapient.kpidashboard.common.service;

import java.util.List;
import java.util.stream.StreamSupport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.common.model.jira.BoardMetadata;
import com.publicissapient.kpidashboard.common.repository.jira.BoardMetadataRepository;

/**
 * @author shunaray
 */
@Service
public class BoardMetadataServiceImpl implements BoardMetadataService {

	@Autowired
	private BoardMetadataRepository boardMetadataRepository;

	public List<BoardMetadata> findAll() {
		Iterable<BoardMetadata> iterable = boardMetadataRepository.findAll();
		return StreamSupport.stream(iterable.spliterator(), false).toList();
	}
}
