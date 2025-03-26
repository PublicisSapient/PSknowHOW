package com.publicissapient.kpidashboard.rally.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bson.types.ObjectId;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.atlassian.jira.rest.client.api.domain.BasicComponent;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.IssueField;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.AdditionalFilter;
import com.publicissapient.kpidashboard.common.model.application.AdditionalFilterCategory;
import com.publicissapient.kpidashboard.common.model.application.AdditionalFilterConfig;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.service.AdditionalFilterCategoryService;
import com.publicissapient.kpidashboard.rally.model.ProjectConfFieldMapping;

@ExtendWith(MockitoExtension.class)
public class AdditionalFilterHelperTest {

    @InjectMocks
    private AdditionalFilterHelper additionalFilterHelper;

    @Mock
    private AdditionalFilterCategoryService additionalFilterCategoryService;

    private Issue issue;
    private ProjectConfFieldMapping projectConfig;
    private FieldMapping fieldMapping;

    @BeforeEach
    public void setup() {
        issue = mock(Issue.class);
        projectConfig = new ProjectConfFieldMapping();
        fieldMapping = new FieldMapping();
        projectConfig.setBasicProjectConfigId(new ObjectId());
        projectConfig.setFieldMapping(fieldMapping);
    }

    @Test
    public void testGetAdditionalFilterWithLabels() {
        // Setup
        Set<String> labels = new HashSet<>(Arrays.asList("label1", "label2"));
        when(issue.getLabels()).thenReturn(labels);

        AdditionalFilterConfig filterConfig = new AdditionalFilterConfig();
        filterConfig.setFilterId("labelFilter");
        filterConfig.setIdentifyFrom(CommonConstant.LABELS);
        filterConfig.setValues(new HashSet<>(Arrays.asList("label1")));

        fieldMapping.setAdditionalFilterConfig(Arrays.asList(filterConfig));

        AdditionalFilterCategory category = new AdditionalFilterCategory();
        category.setFilterCategoryId("labelFilter");
        when(additionalFilterCategoryService.getAdditionalFilterCategories())
            .thenReturn(Arrays.asList(category));

        // Execute
        List<AdditionalFilter> filters = additionalFilterHelper.getAdditionalFilter(issue, projectConfig);

        // Verify
        assertNotNull(filters);
        assertEquals(1, filters.size());
        assertEquals("labelFilter", filters.get(0).getFilterId());
        assertEquals(1, filters.get(0).getFilterValues().size());
        assertEquals("label1", filters.get(0).getFilterValues().get(0).getValue());
    }

    @Test
    public void testGetAdditionalFilterWithComponents() {
        // Setup
        BasicComponent component = mock(BasicComponent.class);
        when(component.getName()).thenReturn("component1");
        when(issue.getComponents()).thenReturn(Arrays.asList(component));

        AdditionalFilterConfig filterConfig = new AdditionalFilterConfig();
        filterConfig.setFilterId("componentFilter");
        filterConfig.setIdentifyFrom(CommonConstant.COMPONENT);
        filterConfig.setValues(new HashSet<>(Arrays.asList("component1")));

        fieldMapping.setAdditionalFilterConfig(Arrays.asList(filterConfig));

        AdditionalFilterCategory category = new AdditionalFilterCategory();
        category.setFilterCategoryId("componentFilter");
        when(additionalFilterCategoryService.getAdditionalFilterCategories())
            .thenReturn(Arrays.asList(category));

        // Execute
        List<AdditionalFilter> filters = additionalFilterHelper.getAdditionalFilter(issue, projectConfig);

        // Verify
        assertNotNull(filters);
        assertEquals(1, filters.size());
        assertEquals("componentFilter", filters.get(0).getFilterId());
        assertEquals(1, filters.get(0).getFilterValues().size());
        assertEquals("component1", filters.get(0).getFilterValues().get(0).getValue());
    }

    @Test
    public void testGetAdditionalFilterWithCustomField() throws Exception {
        // Setup
        IssueField customField = mock(IssueField.class);
        JSONObject fieldValue = new JSONObject();
        fieldValue.put("value", "customValue");
        when(customField.getValue()).thenReturn(fieldValue);

        when(issue.getFields()).thenReturn(Arrays.asList(customField));

        AdditionalFilterConfig filterConfig = new AdditionalFilterConfig();
        filterConfig.setFilterId("customFilter");
        filterConfig.setIdentifyFrom(CommonConstant.CUSTOM_FIELD);
        filterConfig.setIdentificationField("customField");

        fieldMapping.setAdditionalFilterConfig(Arrays.asList(filterConfig));

        AdditionalFilterCategory category = new AdditionalFilterCategory();
        category.setFilterCategoryId("customFilter");
        when(additionalFilterCategoryService.getAdditionalFilterCategories())
            .thenReturn(Arrays.asList(category));

        // Execute
        List<AdditionalFilter> filters = additionalFilterHelper.getAdditionalFilter(issue, projectConfig);

        // Verify
        assertNotNull(filters);
        assertEquals(1, filters.size());
        assertEquals("customFilter", filters.get(0).getFilterId());
    }

    @Test
    public void testGetAdditionalFilterWithCustomFieldArray() throws Exception {
        // Setup
        IssueField customField = mock(IssueField.class);
        JSONArray fieldArray = new JSONArray();
        JSONObject value1 = new JSONObject();
        value1.put("value", "arrayValue1");
        JSONObject value2 = new JSONObject();
        value2.put("value", "arrayValue2");
        fieldArray.put(value1);
        fieldArray.put(value2);
        when(customField.getValue()).thenReturn(fieldArray);

        when(issue.getFields()).thenReturn(Arrays.asList(customField));

        AdditionalFilterConfig filterConfig = new AdditionalFilterConfig();
        filterConfig.setFilterId("customArrayFilter");
        filterConfig.setIdentifyFrom(CommonConstant.CUSTOM_FIELD);
        filterConfig.setIdentificationField("customField");

        fieldMapping.setAdditionalFilterConfig(Arrays.asList(filterConfig));

        AdditionalFilterCategory category = new AdditionalFilterCategory();
        category.setFilterCategoryId("customArrayFilter");
        when(additionalFilterCategoryService.getAdditionalFilterCategories())
            .thenReturn(Arrays.asList(category));

        // Execute
        List<AdditionalFilter> filters = additionalFilterHelper.getAdditionalFilter(issue, projectConfig);

        // Verify
        assertNotNull(filters);
        assertEquals(1, filters.size());
        assertEquals("customArrayFilter", filters.get(0).getFilterId());
        assertEquals(2, filters.get(0).getFilterValues().size());
    }

    @Test
    public void testGetAdditionalFilterWithNoMatchingCategory() {
        // Setup
        AdditionalFilterConfig filterConfig = new AdditionalFilterConfig();
        filterConfig.setFilterId("nonExistentFilter");
        fieldMapping.setAdditionalFilterConfig(Arrays.asList(filterConfig));

        when(additionalFilterCategoryService.getAdditionalFilterCategories())
            .thenReturn(Arrays.asList());

        // Execute
        List<AdditionalFilter> filters = additionalFilterHelper.getAdditionalFilter(issue, projectConfig);

        // Verify
        assertNotNull(filters);
        assertTrue(filters.isEmpty());
    }
}
