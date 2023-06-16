package com.publicissapient.kpidashboard.azure.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.azure.client.azureissue.AzureIssueClientUtil;
import com.publicissapient.kpidashboard.azure.model.ProjectConfFieldMapping;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.AdditionalFilter;
import com.publicissapient.kpidashboard.common.model.application.AdditionalFilterCategory;
import com.publicissapient.kpidashboard.common.model.application.AdditionalFilterConfig;
import com.publicissapient.kpidashboard.common.model.application.AdditionalFilterValue;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.azureboards.Value;
import com.publicissapient.kpidashboard.common.service.AdditionalFilterCategoryService;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class AdditionalFilterHelper {

	@Autowired
	private AdditionalFilterCategoryService additionalFilterCategoryService;

	public List<AdditionalFilter> getAdditionalFilter(Value issue, ProjectConfFieldMapping projectConfig) {
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

	private List<AdditionalFilterValue> getAdditionalFilterValues(Value issue,
			AdditionalFilterConfig additionalFilterConfig, String basicProjectConfigId) {

		List<AdditionalFilterValue> values = new ArrayList<>();

		if (CommonConstant.LABELS.equals(additionalFilterConfig.getIdentifyFrom()) && null != issue.getFields()
				&& StringUtils.isNotEmpty(issue.getFields().getSystemTags())) {
			String[] labelArray = issue.getFields().getSystemTags().split(";");
			Set<String> labels = new HashSet<>(Arrays.asList(labelArray));
			labels.forEach(label -> {
				AdditionalFilterValue additionalFilterValue = new AdditionalFilterValue();
				additionalFilterValue.setValue(label);
				additionalFilterValue.setValueId(createAdditionalFilterValueId(label,
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

	private Set<String> getCustomFieldValues(Value issue, AdditionalFilterConfig additionalFilterConfig) {
		Map<String, Object> fields = AzureIssueClientUtil.buildFieldMap(issue.getFields());
		Set<String> values = new HashSet<>();
		String customField = additionalFilterConfig.getIdentificationField();

		if (null != fields.get(customField)) {
			try {
				if (fields.get(customField) instanceof JSONObject) {
					if (StringUtils
							.isNotBlank((String) ((JSONObject) fields.get(customField)).get(AzureConstants.VALUE))) {
						values.add((String) ((JSONObject) fields.get(customField)).get(AzureConstants.VALUE));
					} else {
						values.add(AzureProcessorUtil.deodeUTF8String(fields.get(customField)));
					}
				} else {
					values.add(AzureProcessorUtil.deodeUTF8String(fields.get(customField)));
				}
			} catch (JSONException e) {
				log.error("Error while parsing custom field " + customField, e);
			}
		}
		return values;
	}

}
