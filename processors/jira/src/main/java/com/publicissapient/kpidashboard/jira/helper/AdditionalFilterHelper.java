package com.publicissapient.kpidashboard.jira.helper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.atlassian.jira.rest.client.api.domain.BasicComponent;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.IssueField;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.AdditionalFilter;
import com.publicissapient.kpidashboard.common.model.application.AdditionalFilterCategory;
import com.publicissapient.kpidashboard.common.model.application.AdditionalFilterConfig;
import com.publicissapient.kpidashboard.common.model.application.AdditionalFilterValue;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.service.AdditionalFilterCategoryService;
import com.publicissapient.kpidashboard.jira.constant.JiraConstants;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;
import com.publicissapient.kpidashboard.jira.util.JiraIssueClientUtil;
import com.publicissapient.kpidashboard.jira.util.JiraProcessorUtil;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class AdditionalFilterHelper {

	@Autowired
	private AdditionalFilterCategoryService additionalFilterCategoryService;

	public List<AdditionalFilter> getAdditionalFilter(Issue issue, ProjectConfFieldMapping projectConfig) {
		List<AdditionalFilter> additionalFilters = new ArrayList<>();
		if (issue != null && projectConfig != null) {
			String basicProjectConfigId = projectConfig.getBasicProjectConfigId().toHexString();

			FieldMapping fieldMapping = projectConfig.getFieldMapping();

			List<AdditionalFilterConfig> additionalFilterConfigs = ListUtils
					.emptyIfNull(fieldMapping.getAdditionalFilterConfig());

			List<AdditionalFilterCategory> additionalFilterCategories = additionalFilterCategoryService
					.getAdditionalFilterCategories();
			Map<String, AdditionalFilterCategory> additionalFilterCategoryMap = additionalFilterCategories.stream()
					.collect(Collectors.toMap(AdditionalFilterCategory::getFilterCategoryId, afc -> afc));

			for (AdditionalFilterConfig additionalFilterConfig : additionalFilterConfigs) {
				if (additionalFilterCategoryMap.get(additionalFilterConfig.getFilterId()) != null) {
					AdditionalFilter additionalFilter = new AdditionalFilter();
					additionalFilter.setFilterId(additionalFilterConfig.getFilterId());
					List<AdditionalFilterValue> additionalFilterValues = getAdditionalFilterValues(issue,
							additionalFilterConfig, basicProjectConfigId);
					additionalFilter.setFilterValues(additionalFilterValues);
					if (CollectionUtils.isNotEmpty(additionalFilterValues)) {
						additionalFilters.add(additionalFilter);
					}
				}

			}
		}

		return additionalFilters;
	}

	private String createAdditionalFilterValueId(String value, String filterId, String basicProjectConfigId) {
		return value + CommonConstant.ADDITIONAL_FILTER_VALUE_ID_SEPARATOR + filterId
				+ CommonConstant.ADDITIONAL_FILTER_VALUE_ID_SEPARATOR + basicProjectConfigId;
	}

	private List<AdditionalFilterValue> getAdditionalFilterValues(Issue issue,
			AdditionalFilterConfig additionalFilterConfig, String basicProjectConfigId) {

		List<AdditionalFilterValue> values = new ArrayList<>();

		if (CommonConstant.LABELS.equals(additionalFilterConfig.getIdentifyFrom())
				&& CollectionUtils.isNotEmpty(issue.getLabels())) {
			Set<String> labels = getLabels(issue, additionalFilterConfig);
			labels.forEach(label -> {
				AdditionalFilterValue additionalFilterValue = new AdditionalFilterValue();
				additionalFilterValue.setValue(label);
				additionalFilterValue.setValueId(createAdditionalFilterValueId(label,
						additionalFilterConfig.getFilterId(), basicProjectConfigId));
				values.add(additionalFilterValue);
			});

		} else if (CommonConstant.COMPONENT.equals(additionalFilterConfig.getIdentifyFrom())) {
			Set<BasicComponent> components = getComponents(issue, additionalFilterConfig);
			components.forEach(component -> {
				AdditionalFilterValue additionalFilterValue = new AdditionalFilterValue();
				additionalFilterValue.setValue(component.getName());
				additionalFilterValue.setValueId(createAdditionalFilterValueId(component.getName(),
						additionalFilterConfig.getFilterId(), basicProjectConfigId));
				values.add(additionalFilterValue);
			});
		} else if (CommonConstant.CUSTOM_FIELD.equals(additionalFilterConfig.getIdentifyFrom())) {

			Set<String> customFieldValues = getCustomFieldValues(issue, additionalFilterConfig);

			customFieldValues.forEach(customFieldValue -> {
				AdditionalFilterValue additionalFilterValue = new AdditionalFilterValue();
				additionalFilterValue.setValue(customFieldValue);
				additionalFilterValue.setValueId(createAdditionalFilterValueId(customFieldValue,
						additionalFilterConfig.getFilterId(), basicProjectConfigId));
				values.add(additionalFilterValue);
			});
		}

		return values;
	}
//	private Set<String> getLabels(Issue issue, AdditionalFilterConfig additionalFilterConfig) {
//		Set<String> configuredLabels = additionalFilterConfig.getValues();
//		Set<String> issueLabels = issue.getLabels();
//
//		Set<String> commonLabels = issueLabels.stream()
//				.filter(label -> configuredLabels.stream()
//						.anyMatch(configuredLabel -> configuredLabel.equalsIgnoreCase(label)))
//				.collect(Collectors.toSet());
//		return commonLabels;
//	}

	private Set<String> getLabels(Issue issue, AdditionalFilterConfig additionalFilterConfig) {
		Set<String> configuredLabelsValuesFromUI = additionalFilterConfig.getValues();
		Set<String> issueLabelsValuesFromJira = issue.getLabels();
		// Convert configuredLabels to lowercase once
		Set<String> configuredLabelsLower = new HashSet<>();
		for (String label : configuredLabelsValuesFromUI) {
			configuredLabelsLower.add(label.toLowerCase());
		}
		// Convert issueLabelsValuesFromJira to lowercase once
		Set<String> issueLabelsFromJiraLower = new HashSet<>();
		for (String label : issueLabelsValuesFromJira) {
			issueLabelsFromJiraLower.add(label.toLowerCase());
		}
		Set<String> commonLabels = new HashSet<>();
		commonLabels = new HashSet<>(configuredLabelsLower);
		commonLabels.retainAll(issueLabelsFromJiraLower);
		return commonLabels;
	}

	private Set<BasicComponent> getComponents(Issue issue, AdditionalFilterConfig additionalFilterConfig) {
		Set<String> configuredComponentNames = additionalFilterConfig.getValues();
		Iterable<BasicComponent> components = issue.getComponents();
		List<BasicComponent> componentList = new ArrayList<>();
		components.forEach(componentList::add);

		Set<BasicComponent> common = new HashSet<>();

		for (BasicComponent basicComponent : componentList) {
			if (CollectionUtils.isNotEmpty(configuredComponentNames)
					&& configuredComponentNames.contains(basicComponent.getName())) {
				common.add(basicComponent);
			}
		}

		return common;
	}

	private Set<String> getCustomFieldValues(Issue issue, AdditionalFilterConfig additionalFilterConfig) {
		Map<String, IssueField> fields = JiraIssueClientUtil.buildFieldMap(issue.getFields());
		Set<String> values = new HashSet<>();
		String customField = additionalFilterConfig.getIdentificationField();

		if (null != fields.get(customField)
				&& StringUtils.isNotEmpty(JiraProcessorUtil.deodeUTF8String(fields.get(customField).getValue()))) {
			try {
				if (fields.get(customField).getValue() instanceof JSONObject) {
					JSONObject jsonObject = (JSONObject) fields.get(customField).getValue();
					getValueFromFieldJsonObject(values, jsonObject);
				} else if (fields.get(customField).getValue() instanceof JSONArray) {
					JSONArray fieldArray = (JSONArray) fields.get(customField).getValue();
					if (fieldArray.length() > 0) {
						for (int i = 0; i < fieldArray.length(); i++) {
							getValueFromFieldJsonObject(values, (JSONObject) fieldArray.get(i));
						}
					}
				} else {
					values.add(JiraProcessorUtil.deodeUTF8String(fields.get(customField).getValue()));
				}
			} catch (JSONException e) {
				log.error("Error while parsing custom field " + customField, e);
			}
		}
		return values;
	}

	private void getValueFromFieldJsonObject(Set<String> values, JSONObject jsonObject) {
		if (jsonObject != null) {
			String value = jsonObject.optString(JiraConstants.VALUE, null);
			if (StringUtils.isNotBlank(value)) {
				values.add(value);
			} else {
				String name = jsonObject.optString(JiraConstants.NAME, null);
				if (StringUtils.isNotBlank(name)) {
					values.add(name);
				}
			}
		}

	}

}
