package com.publicissapient.kpidashboard.apis.pushdata.service.impl;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.doReturn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.apache.commons.collections4.MapUtils;
import org.bson.types.ObjectId;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.modelmapper.ModelMapper;

import com.publicissapient.kpidashboard.apis.data.BuildDataFactory;
import com.publicissapient.kpidashboard.apis.data.PushDataFactory;
import com.publicissapient.kpidashboard.apis.pushdata.model.PushBuildDeploy;
import com.publicissapient.kpidashboard.apis.pushdata.model.PushDataDetail;
import com.publicissapient.kpidashboard.apis.pushdata.model.PushErrorData;
import com.publicissapient.kpidashboard.apis.pushdata.model.dto.PushBuildDeployDTO;
import com.publicissapient.kpidashboard.common.model.application.Build;
import com.publicissapient.kpidashboard.common.repository.application.BuildRepository;

@RunWith(MockitoJUnitRunner.class)
public class BuildServiceImplTest {

	List<Build> buildList;
	@InjectMocks
	private BuildServiceImpl buildService;
	@Mock
	private BuildRepository buildRepository;
	@Mock
	private PushDataValidationServiceImpl pushDataValidationService;
	private ObjectId projectBasicConfigId;
	private Validator validator;

	@Before
	public void setUp() {
		projectBasicConfigId = new ObjectId("632824e949794a18e8a44787");
		BuildDataFactory buildDataFactory = BuildDataFactory.newInstance("/json/pushdata/build_details.json");
		buildList = buildDataFactory.getbuildDataList();

		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();

	}

	@Test
	public void sucessfullInsert() {
		PushBuildDeploy pushBuildDeployCorrectData = new PushBuildDeploy();
		Set<ConstraintViolation<PushBuildDeployDTO>> validate = validator
				.validate(PushDataFactory.newInstance().getPushBuildDeploy().get(0));
		if (validate.isEmpty()) {
			pushBuildDeployCorrectData = new ModelMapper()
					.map(PushDataFactory.newInstance().getPushBuildDeploy().get(0), PushBuildDeploy.class);
		}
		doReturn(buildList.get(0)).when(buildRepository).findByNumberAndBuildJobAndBasicProjectConfigId(
				Mockito.anyString(), Mockito.anyString(), Mockito.any());
		Map<String, String> noErrors = new HashMap<>();
		doReturn(noErrors).when(pushDataValidationService).createBuildDeployErrorMap(anyMap());
		List<Build> buildList = new ArrayList<>();
		List<PushErrorData> errorDataList = new ArrayList<>();
		List<PushDataDetail> pushDataDetails = new ArrayList<>();
		int buildFailedRecords = buildService.checkandCreateBuilds(projectBasicConfigId,
				pushBuildDeployCorrectData.getBuilds(), buildList, errorDataList, pushDataDetails);
		Assert.assertEquals(0, buildFailedRecords);
		Assert.assertEquals(3, buildList.size());
	}

	@Test
	public void errorData() {
		PushBuildDeploy pushBuildDeployCorrectData = new PushBuildDeploy();
		Set<ConstraintViolation<PushBuildDeployDTO>> validate = validator
				.validate(PushDataFactory.newInstance().getPushBuildDeploy().get(1));
		if (validate.isEmpty()) {
			pushBuildDeployCorrectData = new ModelMapper()
					.map(PushDataFactory.newInstance().getPushBuildDeploy().get(1), PushBuildDeploy.class);
		}
		List<Build> buildList = new ArrayList<>();
		List<PushErrorData> errorDataList = new ArrayList<>();
		List<PushDataDetail> pushDataDetails = new ArrayList<>();
		Map<String, String> errorsMap = new HashMap<>();
		errorsMap.put("jobName", "jobName is Blank");
		errorsMap.put("number", "number should be in digits");
		errorsMap.put("buildStatus",
				"buildStatus should be among SUCCESS/FAILURE/UNSTABLE/ABORTED/IN_PROGRESS/UNKNOWN");
		doReturn(errorsMap).when(pushDataValidationService).createBuildDeployErrorMap(anyMap());
		int buildFailedRecords = buildService.checkandCreateBuilds(projectBasicConfigId,
				pushBuildDeployCorrectData.getBuilds(), buildList, errorDataList, pushDataDetails);
		Assert.assertEquals(3, buildFailedRecords);
		Assert.assertEquals(0, buildList.size());
		Assert.assertEquals(3,
				errorDataList.stream()
						.filter(buildDeployErrorData -> MapUtils.isNotEmpty(buildDeployErrorData.getErrors()))
						.collect(Collectors.toList()).size());

	}

	@Test
	public void wrongJsonVaildation() {
		Set<ConstraintViolation<PushBuildDeployDTO>> validate = validator
				.validate(PushDataFactory.newInstance().getPushBuildDeploy().get(2));
		Assert.assertNotNull(validate);
	}

	@Test
	public void buildAllValidation() {
		PushBuildDeploy pushBuildDeployCorrectData = new PushBuildDeploy();
		Set<ConstraintViolation<PushBuildDeployDTO>> validate = validator
				.validate(PushDataFactory.newInstance().getPushBuildDeploy().get(3));
		if (validate.isEmpty()) {
			pushBuildDeployCorrectData = new ModelMapper()
					.map(PushDataFactory.newInstance().getPushBuildDeploy().get(3), PushBuildDeploy.class);
		}
		List<Build> buildList = new ArrayList<>();
		List<PushErrorData> errorDataList = new ArrayList<>();
		List<PushDataDetail> pushDataDetails = new ArrayList<>();
		Map<String, String> errorsMap = new HashMap<>();
		errorsMap.put("jobName", "jobName is Blank");
		errorsMap.put("number", "number should be in digits");
		errorsMap.put("buildStatus",
				"buildStatus should be among SUCCESS/FAILURE/UNSTABLE/ABORTED/IN_PROGRESS/UNKNOWN");
		doReturn(errorsMap).when(pushDataValidationService).createBuildDeployErrorMap(anyMap());
		int buildFailedRecords = buildService.checkandCreateBuilds(projectBasicConfigId,
				pushBuildDeployCorrectData.getBuilds(), buildList, errorDataList, pushDataDetails);
		Assert.assertEquals(3, buildFailedRecords);
		Assert.assertEquals(0, buildList.size());
		Assert.assertEquals(3,
				errorDataList.stream()
						.filter(buildDeployErrorData -> MapUtils.isNotEmpty(buildDeployErrorData.getErrors()))
						.collect(Collectors.toList()).size());

	}

}
