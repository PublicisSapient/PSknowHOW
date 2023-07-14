package com.publicissapient.kpidashboard.common.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.publicissapient.kpidashboard.common.model.application.HierarchyLevel;
import com.publicissapient.kpidashboard.common.model.application.HierarchyLevelSuggestion;
import com.publicissapient.kpidashboard.common.repository.application.HierarchyLevelRepository;
import com.publicissapient.kpidashboard.common.repository.application.HierarchyLevelSuggestionRepository;

@RunWith(MockitoJUnitRunner.class)
public class HierarchyLevelSuggestionsServiceTest {

	List<HierarchyLevelSuggestion> hierarchyLevelSuggestions = new ArrayList<>();
	@InjectMocks
	private HierarchyLevelSuggestionsServiceImpl hierarchyLevelSuggestionsService;
	@Mock
	private HierarchyLevelSuggestionRepository hierarchyLevelSuggestionRepository;
	@Mock
	private HierarchyLevelService hierarchyLevelService;
	@Mock
	private HierarchyLevelRepository hierarchyLevelRepository;
	private List<HierarchyLevel> hierarchyLevels = new ArrayList<>();
	private HierarchyLevel hierarchyLevel1 = new HierarchyLevel();
	private HierarchyLevel hierarchyLevel2 = new HierarchyLevel();
	private HierarchyLevel hierarchyLevel3 = new HierarchyLevel();

	private HierarchyLevelSuggestion hierarchyLevel1Suggestion = new HierarchyLevelSuggestion();
	private HierarchyLevelSuggestion hierarchyLevel2Suggestion = new HierarchyLevelSuggestion();
	private HierarchyLevelSuggestion hierarchyLevel3Suggestion = new HierarchyLevelSuggestion();

	@Before
	public void setUp() {

		hierarchyLevel1.setLevel(1);
		hierarchyLevel1.setHierarchyLevelId("hierarchyLevel1Id");
		hierarchyLevel1.setHierarchyLevelName("hierarchyLevel1Name");

		hierarchyLevel2.setLevel(1);
		hierarchyLevel2.setHierarchyLevelId("hierarchyLevel2Id");
		hierarchyLevel2.setHierarchyLevelName("hierarchyLevel2Name");

		hierarchyLevel3.setLevel(1);
		hierarchyLevel3.setHierarchyLevelId("hierarchyLevel3Id");
		hierarchyLevel3.setHierarchyLevelName("hierarchyLevel3Name");

		hierarchyLevels.add(hierarchyLevel1);
		hierarchyLevels.add(hierarchyLevel2);
		hierarchyLevels.add(hierarchyLevel3);

		TreeSet<String> hierarchyLevel1Values = new TreeSet<>();
		TreeSet<String> hierarchyLevel2Values = new TreeSet<>();
		TreeSet<String> hierarchyLevel3Values = new TreeSet<>();
		hierarchyLevel1Values.add("hierarchyLevel1Value1");
		hierarchyLevel1Values.add("hierarchyLevel1Value2");
		hierarchyLevel2Values.add("hierarchyLevel2Value1");
		hierarchyLevel3Values.add("hierarchyLevel3Value1");

		hierarchyLevel1Suggestion.setId(new ObjectId("60ed70a572dafe33d3e37111"));
		hierarchyLevel1Suggestion.setHierarchyLevelId("hierarchyLevel1Id");
		hierarchyLevel1Suggestion.setValues(hierarchyLevel1Values);
		hierarchyLevel2Suggestion.setId(new ObjectId("60ed70a572dafe33d3e37222"));
		hierarchyLevel2Suggestion.setHierarchyLevelId("hierarchyLevel2Id");
		hierarchyLevel2Suggestion.setValues(hierarchyLevel2Values);
		hierarchyLevel3Suggestion.setId(new ObjectId("60ed70a572dafe33d3e37333"));
		hierarchyLevel3Suggestion.setHierarchyLevelId("hierarchyLevel3Id");
		hierarchyLevel3Suggestion.setValues(hierarchyLevel3Values);

		hierarchyLevelSuggestions.add(hierarchyLevel1Suggestion);
		hierarchyLevelSuggestions.add(hierarchyLevel2Suggestion);
		hierarchyLevelSuggestions.add(hierarchyLevel3Suggestion);
	}

	@Test
	public void getSuggestionsTest() {
		when(hierarchyLevelSuggestionRepository.findAll()).thenReturn(hierarchyLevelSuggestions);
		List<HierarchyLevelSuggestion> result = hierarchyLevelSuggestionsService.getSuggestions();
		assertEquals(result, hierarchyLevelSuggestions);
	}

	@Test
	public void addIfNotPresent_AlreadyExistsHierarchyLevel() {

		when(hierarchyLevelSuggestionRepository.findByHierarchyLevelId(anyString()))
				.thenReturn(hierarchyLevel2Suggestion);
		TreeSet<String> existingValues = hierarchyLevel2Suggestion.getValues();
		existingValues.add("hierarchyLevel2Value2");
		hierarchyLevel2Suggestion.setValues(existingValues);
		when(hierarchyLevelSuggestionRepository.save(hierarchyLevel2Suggestion)).thenReturn(hierarchyLevel2Suggestion);
		HierarchyLevelSuggestion result = hierarchyLevelSuggestionsService.addIfNotPresent("hierarchyLevel2Id",
				"hierarchyLevel2Value2");
		assertEquals(2, result.getValues().size());

	}

	@Test
	public void addIfNotPresent_HierarchyLevel() {

		when(hierarchyLevelSuggestionRepository.findByHierarchyLevelId(anyString())).thenReturn(null);
		HierarchyLevelSuggestion hierarchyLevel4Suggestion = new HierarchyLevelSuggestion();
		TreeSet<String> values = new TreeSet<>();
		values.add("hierarchyLevel4Value1");
		hierarchyLevel4Suggestion.setId(new ObjectId("60ed70a572dafe33d3e37444"));
		hierarchyLevel4Suggestion.setHierarchyLevelId("hierarchyLevel4Id");
		hierarchyLevel4Suggestion.setValues(values);
		when(hierarchyLevelSuggestionRepository.save(hierarchyLevel4Suggestion)).thenReturn(hierarchyLevel4Suggestion);
		HierarchyLevelSuggestion result = hierarchyLevelSuggestionsService.addIfNotPresent("hierarchyLevel4Id",
				"hierarchyLevel4Value1");
		assertEquals("60ed70a572dafe33d3e37444", result.getId().toHexString());

	}

}
