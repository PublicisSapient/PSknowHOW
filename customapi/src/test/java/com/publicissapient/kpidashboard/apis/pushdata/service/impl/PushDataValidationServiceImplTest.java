package com.publicissapient.kpidashboard.apis.pushdata.service.impl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import org.modelmapper.ModelMapper;

import com.publicissapient.kpidashboard.apis.data.BuildDataFactory;
import com.publicissapient.kpidashboard.apis.data.PushDataFactory;
import com.publicissapient.kpidashboard.apis.enums.PushValidationType;
import com.publicissapient.kpidashboard.apis.pushdata.model.PushBuildDeploy;
import com.publicissapient.kpidashboard.apis.pushdata.model.dto.PushBuild;
import com.publicissapient.kpidashboard.apis.pushdata.model.dto.PushBuildDeployDTO;
import com.publicissapient.kpidashboard.apis.pushdata.model.dto.PushDeploy;
import com.publicissapient.kpidashboard.common.model.application.Build;

@RunWith(MockitoJUnitRunner.class)
public class PushDataValidationServiceImplTest {

	List<Build> buildList;
	@InjectMocks
	private PushDataValidationServiceImpl pushDataValidationService;
	private Validator validator;

	@Before
	public void setUp() {
		BuildDataFactory buildDataFactory = BuildDataFactory.newInstance("/json/pushdata/build_details.json");
		buildList = buildDataFactory.getbuildDataList();
		buildList.get(0);

		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();

	}

	@Test
	public void sucessfullBuildInsert() {
		PushBuildDeploy pushBuildDeployCorrectData = new PushBuildDeploy();
		Set<ConstraintViolation<PushBuildDeployDTO>> validate = validator
				.validate(PushDataFactory.newInstance().getPushBuildDeploy().get(0));
		if (validate.isEmpty()) {
			pushBuildDeployCorrectData = new ModelMapper()
					.map(PushDataFactory.newInstance().getPushBuildDeploy().get(0), PushBuildDeploy.class);
		}

		Map<Pair<String, String>, List<PushValidationType>> buildValidationMap = createBuildValidationMap(
				pushBuildDeployCorrectData.getBuilds().stream().findFirst().get());
		Map<String, String> errorsMap = pushDataValidationService.createBuildDeployErrorMap(buildValidationMap);
		Assert.assertEquals(0, errorsMap.size());

	}

	@Test
	public void sucessfullDeployInsert() {
		PushBuildDeploy pushBuildDeployCorrectData = new PushBuildDeploy();
		Set<ConstraintViolation<PushBuildDeployDTO>> validate = validator
				.validate(PushDataFactory.newInstance().getPushBuildDeploy().get(0));
		if (validate.isEmpty()) {
			pushBuildDeployCorrectData = new ModelMapper()
					.map(PushDataFactory.newInstance().getPushBuildDeploy().get(0), PushBuildDeploy.class);
		}

		Map<Pair<String, String>, List<PushValidationType>> buildValidationMap = createDeployValidationMap(
				pushBuildDeployCorrectData.getDeployments().stream().findFirst().get());
		Map<String, String> errorsMap = pushDataValidationService.createBuildDeployErrorMap(buildValidationMap);
		Assert.assertEquals(0, errorsMap.size());

	}

	private Map<Pair<String, String>, List<PushValidationType>> createDeployValidationMap(PushDeploy pushDeploy) {
		Map<Pair<String, String>, List<PushValidationType>> validations = new HashMap<>();
		validations.put(Pair.of("jobName", pushDeploy.getJobName()), Arrays.asList(PushValidationType.BLANK));
		validations.put(Pair.of("number", pushDeploy.getNumber()), Arrays.asList(PushValidationType.BLANK));
		validations.put(Pair.of("deploymentStatus", pushDeploy.getDeploymentStatus()),
				Arrays.asList(PushValidationType.BLANK, PushValidationType.DEPLOYMENT_STATUS));
		validations.put(Pair.of("envName", pushDeploy.getEnvName()), Arrays.asList(PushValidationType.BLANK));
		validations.put(Pair.of("startTime", pushDeploy.getStartTime().toString()),
				Arrays.asList(PushValidationType.BLANK));
		validations.put(Pair.of("endTime", pushDeploy.getEndTime().toString()),
				Arrays.asList(PushValidationType.BLANK));
		validations.put(Pair.of("duration", pushDeploy.getDuration().toString()),
				Arrays.asList(PushValidationType.BLANK));
		return validations;
	}

	private Map<Pair<String, String>, List<PushValidationType>> createBuildValidationMap(PushBuild pushBuild) {
		Map<Pair<String, String>, List<PushValidationType>> validations = new HashMap<>();
		validations.put(Pair.of("jobName", pushBuild.getJobName()), Arrays.asList(PushValidationType.BLANK));
		validations.put(Pair.of("number", pushBuild.getNumber()),
				Arrays.asList(PushValidationType.BLANK, PushValidationType.NUMERIC));
		validations.put(Pair.of("buildStatus", pushBuild.getBuildStatus()),
				Arrays.asList(PushValidationType.BLANK, PushValidationType.BUILD_STATUS));
		validations.put(Pair.of("startTime", pushBuild.getStartTime().toString()),
				Arrays.asList(PushValidationType.BLANK, PushValidationType.TIME_DETAILS));
		validations.put(Pair.of("endTime", pushBuild.getEndTime().toString()),
				Arrays.asList(PushValidationType.BLANK, PushValidationType.TIME_DETAILS));
		validations.put(Pair.of("duration", pushBuild.getDuration().toString()),
				Arrays.asList(PushValidationType.BLANK, PushValidationType.TIME_DETAILS));
		return validations;

	}

}
