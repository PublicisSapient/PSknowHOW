/*******************************************************************************
 * Copyright 2014 CapitalOne, LLC.
 * Further development Copyright 2022 Sapient Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package com.publicissapient.kpidashboard.jira.helper;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

import org.bson.types.ObjectId;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.atlassian.jira.rest.client.api.domain.BasicComponent;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.IssueField;
import com.publicissapient.kpidashboard.common.model.application.AdditionalFilterCategory;
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
	public void getAdditionalFilterTest() {

		when(projectConfig.getBasicProjectConfigId()).thenReturn(ObjectId.get());
		AdditionalFilterConfig additionalFilterConfig1 =
				getAdditionalFilterConfig("afOne", "Labels", "", getValueSet());
		AdditionalFilterConfig additionalFilterConfig2 =
				getAdditionalFilterConfig("", "", "", getValueSet());
		when(projectConfig.getFieldMapping())
				.thenReturn(
						getFieldMapping(Arrays.asList(additionalFilterConfig1, additionalFilterConfig2)));
		assertEquals(0, additionalFilterHelper.getAdditionalFilter(issue, projectConfig).size());
	}

	@Test
	public void getAdditionalFilterOtherTest() {

		when(projectConfig.getBasicProjectConfigId()).thenReturn(ObjectId.get());
		AdditionalFilterConfig additionalFilterConfig1 =
				getAdditionalFilterConfig("afOne", "Labels", "", getValueSet());
		AdditionalFilterConfig additionalFilterConfig2 =
				getAdditionalFilterConfig("", "", "", getValueSet());
		when(projectConfig.getFieldMapping())
				.thenReturn(
						getFieldMapping(Arrays.asList(additionalFilterConfig1, additionalFilterConfig2)));
		List<AdditionalFilterCategory> additionalFilterCategories = new ArrayList<>();
		additionalFilterCategories.add(getAdditionalFilterCategory(1, "afOne", "Teams"));
		additionalFilterCategories.add(getAdditionalFilterCategory(2, "afOne1", "Teams1"));
		when(additionalFilterCategoryService.getAdditionalFilterCategories())
				.thenReturn(additionalFilterCategories);
		assertEquals(0, additionalFilterHelper.getAdditionalFilter(issue, projectConfig).size());
	}

	@Test
	public void getAdditionalFilterLabelsTest() {

		when(projectConfig.getBasicProjectConfigId()).thenReturn(ObjectId.get());
		AdditionalFilterConfig additionalFilterConfig1 =
				getAdditionalFilterConfig("afOne", "Labels", "", getValueSet());
		AdditionalFilterConfig additionalFilterConfig2 =
				getAdditionalFilterConfig("", "", "", getValueSet());
		when(projectConfig.getFieldMapping())
				.thenReturn(
						getFieldMapping(Arrays.asList(additionalFilterConfig1, additionalFilterConfig2)));
		List<AdditionalFilterCategory> additionalFilterCategories = new ArrayList<>();
		additionalFilterCategories.add(getAdditionalFilterCategory(1, "afOne", "Teams"));
		additionalFilterCategories.add(getAdditionalFilterCategory(2, "afOne1", "Teams1"));
		when(additionalFilterCategoryService.getAdditionalFilterCategories())
				.thenReturn(additionalFilterCategories);
		when(issue.getLabels()).thenReturn(getLabels("UI", "Prod_defect"));
		assertEquals(1, additionalFilterHelper.getAdditionalFilter(issue, projectConfig).size());
	}

	@Test
	public void getAdditionalFilterComponentsTest() {

		when(projectConfig.getBasicProjectConfigId()).thenReturn(ObjectId.get());
		AdditionalFilterConfig additionalFilterConfig1 =
				getAdditionalFilterConfig("afOne", "Component", "", getValueSet());
		AdditionalFilterConfig additionalFilterConfig2 =
				getAdditionalFilterConfig("", "", "", getValueSet());
		when(projectConfig.getFieldMapping())
				.thenReturn(
						getFieldMapping(Arrays.asList(additionalFilterConfig1, additionalFilterConfig2)));
		List<AdditionalFilterCategory> additionalFilterCategories = new ArrayList<>();
		additionalFilterCategories.add(getAdditionalFilterCategory(1, "afOne", "Teams"));
		additionalFilterCategories.add(getAdditionalFilterCategory(2, "afOne1", "Teams1"));
		when(additionalFilterCategoryService.getAdditionalFilterCategories())
				.thenReturn(additionalFilterCategories);
		Iterable<BasicComponent> components = getComponents();
		when(issue.getComponents()).thenReturn(components);
		assertEquals(1, additionalFilterHelper.getAdditionalFilter(issue, projectConfig).size());
	}

	@Test
	public void getAdditionalFilterCustomfieldTest() throws JSONException, URISyntaxException {

		when(projectConfig.getBasicProjectConfigId()).thenReturn(ObjectId.get());
		AdditionalFilterConfig additionalFilterConfig1 =
				getAdditionalFilterConfig("afOne", "CustomField", "", getValueSet());
		additionalFilterConfig1.setIdentificationField("id1");
		AdditionalFilterConfig additionalFilterConfig2 =
				getAdditionalFilterConfig("", "", "", getValueSet());
		when(projectConfig.getFieldMapping())
				.thenReturn(
						getFieldMapping(Arrays.asList(additionalFilterConfig1, additionalFilterConfig2)));
		List<AdditionalFilterCategory> additionalFilterCategories = new ArrayList<>();
		additionalFilterCategories.add(getAdditionalFilterCategory(1, "afOne", "Teams"));
		additionalFilterCategories.add(getAdditionalFilterCategory(2, "afOne1", "Teams1"));
		when(additionalFilterCategoryService.getAdditionalFilterCategories())
				.thenReturn(additionalFilterCategories);
		Collection<IssueField> issueFields =
				Arrays.asList(
						new IssueField("id1", "name1", "type1", getJSONArray()),
						new IssueField("id2", "name2", "type2", getJSONArray()));
		// Issue issue1=spy(issue);
		Issue issue1 =
				new Issue(
						"summary",
						new URI(""),
						"key",
						123l,
						null,
						null,
						null,
						"",
						null,
						null,
						null,
						null,
						null,
						new DateTime(),
						new DateTime(),
						new DateTime(),
						null,
						null,
						null,
						null,
						issueFields,
						null,
						null,
						null,
						null,
						null,
						null,
						null,
						null,
						null,
						null,
						null);
		assertEquals(1, additionalFilterHelper.getAdditionalFilter(issue1, projectConfig).size());
	}

	@Test(expected = NullPointerException.class)
	public void getAdditionalFilterFieldMappingNullTest() {
		when(projectConfig.getBasicProjectConfigId()).thenReturn(ObjectId.get());
		additionalFilterHelper.getAdditionalFilter(issue, projectConfig);
	}

	@Test(expected = NullPointerException.class)
	public void getAdditionalFilterBasicProjectConfigIdNullTest() {
		additionalFilterHelper.getAdditionalFilter(issue, projectConfig);
	}

	@Test
	public void getCustomFieldValuesExceptionTest() throws NoSuchMethodException, InvocationTargetException,
			IllegalAccessException, URISyntaxException, JSONException {

		AdditionalFilterConfig additionalFilterConfig = spy(AdditionalFilterConfig.class);
		additionalFilterConfig.setIdentificationField("id1");
		Method method = AdditionalFilterHelper.class.getDeclaredMethod("getCustomFieldValues", Issue.class,
				AdditionalFilterConfig.class);
		method.setAccessible(true);
		Collection<IssueField> issueFields = Arrays.asList(
				new IssueField("id1", "name1", "type1", getJSONObjectException("name1", "27", "77777")),
				new IssueField("id2", "name2", "type2", getJSONObjectException("name1", "27", "77777")));
		Issue issue1 = new Issue("summary", new URI(""), "key", 123l, null, null, null, "", null, null, null, null, null,
				new DateTime(), new DateTime(), new DateTime(), null, null, null, null, issueFields, null, null, null, null,
				null, null, null, null, null, null, null);

		Set resultSet = (HashSet) method.invoke(additionalFilterHelper, issue1, additionalFilterConfig);

		assertNotNull(resultSet);
		assertEquals(1, resultSet.size());
		assertTrue(resultSet.contains("name1"));
	}

	@Test
	public void getCustomFieldValuesTest() throws NoSuchMethodException, InvocationTargetException,
			IllegalAccessException, URISyntaxException, JSONException {

		AdditionalFilterConfig additionalFilterConfig = spy(AdditionalFilterConfig.class);

		Method method = AdditionalFilterHelper.class.getDeclaredMethod("getCustomFieldValues", Issue.class,
				AdditionalFilterConfig.class);
		method.setAccessible(true);
		Collection<IssueField> issueFields = Arrays.asList(new IssueField("id1", "name1", "type1", getJSONArray()),
				new IssueField("id2", "name2", "type2", getJSONArray()));

		Issue issue1 = new Issue("summary", new URI(""), "key", 123l, null, null, null, "", null, null, null, null, null,
				new DateTime(), new DateTime(), new DateTime(), null, null, null, null, issueFields, null, null, null, null,
				null, null, null, null, null, null, null);

		Set resultSet = (HashSet) method.invoke(additionalFilterHelper, issue1, additionalFilterConfig);

		assertNotNull(resultSet);
		assertEquals(0, resultSet.size());
	}

	AdditionalFilterConfig getAdditionalFilterConfig(String filterId, String identifyFrom, String identificationField,
			Set<String> valueSet) {
		AdditionalFilterConfig additionalFilterConfig = new AdditionalFilterConfig();
		additionalFilterConfig.setFilterId(filterId);
		additionalFilterConfig.setIdentifyFrom(identifyFrom);
		additionalFilterConfig.setIdentificationField("");
		additionalFilterConfig.setValues(valueSet);
		return additionalFilterConfig;
	}

	private Set<String> getValueSet() {
		Set<String> valueSet = new HashSet<>();
		valueSet.add("JAVA");
		valueSet.add("prod_defect");
		valueSet.add("UI");
		valueSet.add("QA_Defect");
		valueSet.add("Component");
		valueSet.add("CustomField");
		return valueSet;
	}

	FieldMapping getFieldMapping(List<AdditionalFilterConfig> additionalFilterConfigList) {
		FieldMapping fieldMapping = new FieldMapping();
		fieldMapping.setAdditionalFilterConfig(additionalFilterConfigList);
		return fieldMapping;
	}

	AdditionalFilterCategory getAdditionalFilterCategory(int level, String filterCategoryId, String filterCategoryName) {
		AdditionalFilterCategory additionalFilterCategory = new AdditionalFilterCategory();
		additionalFilterCategory.setLevel(level);
		additionalFilterCategory.setFilterCategoryId(filterCategoryId);
		additionalFilterCategory.setFilterCategoryName(filterCategoryName);
		return additionalFilterCategory;
	}

	private Set<String> getLabels(String... labelArr) {
		Set<String> labels = new HashSet<>();
		for (String s : labelArr) {
			labels.add(s);
		}
		return labels;
	}

	private JSONObject getJSONObject(String name, String age, String salary) throws JSONException {
		JSONObject obj = new JSONObject();
		obj.put("name", name);
		obj.put("age", age);
		obj.put("value", salary);
		return obj;
	}

	private JSONObject getJSONObjectException(String name, String age, String salary) throws JSONException {
		JSONObject obj = new JSONObject();
		obj.put("name", name);
		obj.put("age", age);
		obj.put("valueExcption", salary);
		return obj;
	}

	private JSONArray getJSONArrayException(String name, int age, int salary) throws JSONException {
		JSONArray obj = new JSONArray();
		obj.put(name);
		obj.put(age);
		obj.put(salary);
		return obj;
	}

	private JSONArray getJSONArray() throws JSONException {
		JSONArray arr = new JSONArray();
		arr.put(getJSONObject("name1", "27", "77777"));
		arr.put(getJSONObject("name2", "37", "77777"));
		return arr;
	}

	private Iterable<BasicComponent> getComponents() {
		List<BasicComponent> componentList = Arrays.asList(new BasicComponent(null, 1234567L, "Component", "desc"));
		Iterable<BasicComponent> components = new Iterable<BasicComponent>() {
			@Override
			public Iterator<BasicComponent> iterator() {
				return componentList.iterator();
			}
		};
		return components;
	}
}
