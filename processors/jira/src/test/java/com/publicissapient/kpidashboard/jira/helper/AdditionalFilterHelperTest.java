package com.publicissapient.kpidashboard.jira.helper;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bson.types.ObjectId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.atlassian.jira.rest.client.api.domain.Issue;
import com.publicissapient.kpidashboard.common.model.application.AdditionalFilterConfig;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.service.AdditionalFilterCategoryService;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;

@RunWith(MockitoJUnitRunner.class)
public class AdditionalFilterHelperTest {
	@Mock
	AdditionalFilterCategoryService additionalFilterCategoryService;

	@Mock
	Issue issue;

	@Mock
	ProjectConfFieldMapping projectConfig;

	@InjectMocks
	AdditionalFilterHelper additionalFilterHelper;

	@Test
    public void getAdditionalFilterTest(){

        when(projectConfig.getBasicProjectConfigId()).thenReturn(ObjectId.get());
        AdditionalFilterConfig additionalFilterConfig1=getAdditionalFilterConfig("afOne","Labels","",getValueSet());
        AdditionalFilterConfig additionalFilterConfig2=getAdditionalFilterConfig("","","",getValueSet());
        when(projectConfig.getFieldMapping()).thenReturn(getFieldMapping(Arrays.asList(additionalFilterConfig1,additionalFilterConfig2)));        ;
		assertEquals(0,additionalFilterHelper.getAdditionalFilter(issue,projectConfig).size());

    }

	@Test(expected = NullPointerException.class)
    public void getAdditionalFilterFieldMappingNullTest(){
        when(projectConfig.getBasicProjectConfigId()).thenReturn(ObjectId.get());
        additionalFilterHelper.getAdditionalFilter(issue,projectConfig);
    }

	@Test(expected = NullPointerException.class)
	public void getAdditionalFilterBasicProjectConfigIdNullTest() {
		additionalFilterHelper.getAdditionalFilter(issue, projectConfig);
	}

	AdditionalFilterConfig getAdditionalFilterConfig(String filterId, String identifyFrom, String identificationField,
			Set<String> valueSet) {
		AdditionalFilterConfig additionalFilterConfig = new AdditionalFilterConfig();
		additionalFilterConfig.setFilterId("afOne");
		additionalFilterConfig.setIdentifyFrom("Labels");
		additionalFilterConfig.setIdentificationField("");
		additionalFilterConfig.setValues(valueSet);
		return additionalFilterConfig;

	}

	Set<String> getValueSet() {
		Set<String> valueSet = new HashSet<>();
		valueSet.add("JAVA");
		valueSet.add("prod_defect");
		valueSet.add("UI");
		valueSet.add("QA_Defect");
		return valueSet;
	}

	FieldMapping getFieldMapping(List<AdditionalFilterConfig> additionalFilterConfigList) {
		FieldMapping fieldMapping = new FieldMapping();
		fieldMapping.setAdditionalFilterConfig(additionalFilterConfigList);
		return fieldMapping;
	}
}
