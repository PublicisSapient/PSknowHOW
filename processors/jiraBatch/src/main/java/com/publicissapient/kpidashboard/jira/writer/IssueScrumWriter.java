package com.publicissapient.kpidashboard.jira.writer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.common.repository.jira.SprintRepository;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.common.model.application.AccountHierarchy;
import com.publicissapient.kpidashboard.common.model.jira.AssigneeDetails;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;
import com.publicissapient.kpidashboard.common.model.jira.JiraIssueCustomHistory;
import com.publicissapient.kpidashboard.common.repository.application.AccountHierarchyRepository;
import com.publicissapient.kpidashboard.common.repository.jira.AssigneeDetailsRepository;
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

	@Autowired
	private AssigneeDetailsRepository assigneeDetailsRepository;

	@Autowired
	private SprintRepository sprintRepository;

	@Override
	public void write(List<? extends CompositeResult> compositeResults) throws Exception {
		List<JiraIssue> jiraIssues = new ArrayList<>();
		List<JiraIssueCustomHistory> jiraHistoryItems = new ArrayList<>();
		Set<AccountHierarchy> accountHierarchies = new HashSet<>();
		Map<String, AssigneeDetails> assigneesToSave = new HashMap<>();
		Set<SprintDetails> sprintDetailsSet=new HashSet<>();

		for (CompositeResult compositeResult : compositeResults) {
			if (null != compositeResult.getJiraIssue()) {
				jiraIssues.add(compositeResult.getJiraIssue());
			}
			if (null != compositeResult.getJiraIssueCustomHistory()) {
				jiraHistoryItems.add(compositeResult.getJiraIssueCustomHistory());
			}
			if (null != compositeResult.getSprintDetailsSet()){
				sprintDetailsSet.addAll(compositeResult.getSprintDetailsSet());
			}
			if (CollectionUtils.isNotEmpty(compositeResult.getAccountHierarchies())) {
				accountHierarchies.addAll(compositeResult.getAccountHierarchies());
			}
			if (null != compositeResult.getAssigneeDetails()) {
				assigneesToSave.put(compositeResult.getAssigneeDetails().getBasicProjectConfigId(),
						compositeResult.getAssigneeDetails());
			}
		}
		if (CollectionUtils.isNotEmpty(jiraIssues)) {
			writeJiraItem(jiraIssues);
		}
		if (CollectionUtils.isNotEmpty(jiraHistoryItems)) {
			writeJiraHistory(jiraHistoryItems);
		}
		if (CollectionUtils.isNotEmpty(sprintDetailsSet)){
			writeSprintDetail(sprintDetailsSet);
		}
		if (CollectionUtils.isNotEmpty(accountHierarchies)) {
			writeAccountHierarchy(accountHierarchies);
		}
		if (MapUtils.isNotEmpty(assigneesToSave)) {
			writeAssigneeDetails(assigneesToSave);
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

	private void writeSprintDetail(Set<SprintDetails> sprintDetailsSet) {
		log.info("Writing issues to SprintDetails Collection");
		sprintRepository.saveAll(sprintDetailsSet);
	}

	public void writeAccountHierarchy(Set<AccountHierarchy> accountHierarchies) {
		log.info("Writing issues to account_hierarchy Collection");
		accountHierarchyRepository.saveAll(accountHierarchies);
	}

	public void writeAssigneeDetails(Map<String, AssigneeDetails> assigneesToSave) {
		log.info("Writing assingees to asignee_details Collection");
		List<AssigneeDetails> assignees = assigneesToSave.values().stream().collect(Collectors.toList());
		assigneeDetailsRepository.saveAll(assignees);
	}
}
