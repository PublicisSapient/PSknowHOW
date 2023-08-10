package com.publicissapient.kpidashboard.jira.writer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.common.model.application.AccountHierarchy;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.repository.application.AccountHierarchyRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueCustomHistoryRepository;
import com.publicissapient.kpidashboard.common.repository.jira.JiraIssueRepository;
import com.publicissapient.kpidashboard.jira.model.CompositeResult;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class IssueScrumWriter implements ItemWriter<CompositeResult> {

	@Autowired
	private JiraIssueRepository jiraIssueRepository;

	@Autowired
	private JiraIssueCustomHistoryRepository jiraIssueCustomHistoryRepository;

	@Autowired
	private AccountHierarchyRepository accountHierarchyRepository;

	@Override
	public void write(List<? extends CompositeResult> compositeResults) throws Exception {
		List<JiraIssue> jiraIssues = new ArrayList<>();
		List<JiraIssueCustomHistory> jiraHistoryItems = new ArrayList<>();
		Set<AccountHierarchy> accountHierarchies = new HashSet<>();
		for (CompositeResult compositeResult : compositeResults) {
			if (null != compositeResult.getJiraIssue()) {
				jiraIssues.add(compositeResult.getJiraIssue());
			}
			if (null != compositeResult.getJiraIssueCustomHistory()) {
				jiraHistoryItems.add(compositeResult.getJiraIssueCustomHistory());
			}
			if (CollectionUtils.isNotEmpty(compositeResult.getAccountHierarchies())) {
				accountHierarchies.addAll(compositeResult.getAccountHierarchies());
			}
		}
		if (CollectionUtils.isNotEmpty(jiraIssues)) {
			writeJiraItem(jiraIssues);
		}
		if (CollectionUtils.isNotEmpty(jiraHistoryItems)) {
			writeJiraHistory(jiraHistoryItems);
		}
		if (CollectionUtils.isNotEmpty(accountHierarchies)) {
			writeAccountHierarchy(accountHierarchies);
		}

	}

	public void writeJiraItem(List<JiraIssue> jiraItems) {
		log.info("Writing issues to Jira_Issue Collection");
		jiraIssueRepository.saveAll(jiraItems);
	}

	public void writeJiraHistory(List<JiraIssueCustomHistory> jiraHistoryItems) {
		log.info("Writing issues to Jira_Issue_custom_history Collection");
		jiraIssueCustomHistoryRepository.saveAll(jiraHistoryItems);
	}

	public void writeAccountHierarchy(Set<AccountHierarchy> accountHierarchies) {
		log.info("Writing issues to account_hierarchy Collection");
		accountHierarchyRepository.saveAll(accountHierarchies);
	}
}
