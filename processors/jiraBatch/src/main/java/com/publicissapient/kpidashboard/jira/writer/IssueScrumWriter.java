package com.publicissapient.kpidashboard.jira.writer;

import java.util.List;

import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.common.model.jira.JiraIssue;

@Component
public class IssueScrumWriter implements ItemWriter<JiraIssue> {

	@Override
	public void write(List<? extends JiraIssue> items) throws Exception {
		System.out.println("in item writer");
		items.stream().forEach(System.out::println);
	}

}
