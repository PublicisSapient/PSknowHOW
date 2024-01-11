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

import com.publicissapient.kpidashboard.apis.data.DeploymentDataFactory;
import com.publicissapient.kpidashboard.apis.data.PushDataFactory;
import com.publicissapient.kpidashboard.apis.pushdata.model.PushBuildDeploy;
import com.publicissapient.kpidashboard.apis.pushdata.model.PushDataDetail;
import com.publicissapient.kpidashboard.apis.pushdata.model.PushErrorData;
import com.publicissapient.kpidashboard.apis.pushdata.model.dto.PushBuildDeployDTO;
import com.publicissapient.kpidashboard.common.model.application.Deployment;
import com.publicissapient.kpidashboard.common.repository.application.DeploymentRepository;

@RunWith(MockitoJUnitRunner.class)
public class DeployServiceImplTest {

	List<Deployment> deploymentList;
	@InjectMocks
	private DeployServiceImpl deployService;
	@Mock
	private DeploymentRepository deploymentRepository;
	@Mock
	private PushDataValidationServiceImpl pushDataValidationService;
	private ObjectId projectBasicConfigId;
	private Validator validator;

	@Before
	public void setUp() {
		projectBasicConfigId = new ObjectId("632824e949794a18e8a44787");
		DeploymentDataFactory deployDataFactory = DeploymentDataFactory.newInstance("/json/pushdata/deployment.json");
		deploymentList = deployDataFactory.getDeploymentDataList();

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
		doReturn(deploymentList.get(0)).when(deploymentRepository)
				.findByNumberAndJobNameAndBasicProjectConfigId(Mockito.anyString(), Mockito.anyString(), Mockito.any());
		List<Deployment> deploymentList = new ArrayList<>();
		List<PushErrorData> errorDataList = new ArrayList<>();
		List<PushDataDetail> pushDataDetails = new ArrayList<>();
		int deployFailedRecords = deployService.checkandCreateDeployment(projectBasicConfigId,
				pushBuildDeployCorrectData.getDeployments(), deploymentList, errorDataList, pushDataDetails);
		Assert.assertEquals(0, deployFailedRecords);
		Assert.assertEquals(2, deploymentList.size());
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
		Map<String, String> errorsMap = new HashMap<>();
		errorsMap.put("EnvName", "EnvName is Blank");
		errorsMap.put("deploymentStatus", "deploymentStatus is Blank");
		doReturn(errorsMap).when(pushDataValidationService).createBuildDeployErrorMap(anyMap());
		List<Deployment> deploymentList = new ArrayList<>();
		List<PushErrorData> errorDataList = new ArrayList<>();
		List<PushDataDetail> pushDataDetails = new ArrayList<>();
		int deployFailedRecords = deployService.checkandCreateDeployment(projectBasicConfigId,
				pushBuildDeployCorrectData.getDeployments(), deploymentList, errorDataList, pushDataDetails);
		Assert.assertEquals(2, deployFailedRecords);
		Assert.assertEquals(0, deploymentList.size());
		Assert.assertEquals(2,
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
	public void builddeployAllValidation() {
		PushBuildDeploy pushBuildDeployCorrectData = new PushBuildDeploy();
		Set<ConstraintViolation<PushBuildDeployDTO>> validate = validator
				.validate(PushDataFactory.newInstance().getPushBuildDeploy().get(3));
		if (validate.isEmpty()) {
			pushBuildDeployCorrectData = new ModelMapper()
					.map(PushDataFactory.newInstance().getPushBuildDeploy().get(3), PushBuildDeploy.class);
		}
		Map<String, String> errorsMap = new HashMap<>();
		errorsMap.put("jobName", "jobName is Blank");
		errorsMap.put("number", "number should be in digits");
		doReturn(errorsMap).when(pushDataValidationService).createBuildDeployErrorMap(anyMap());
		List<Deployment> deploymentList = new ArrayList<>();
		List<PushDataDetail> pushDataDetails = new ArrayList<>();
		List<PushErrorData> errorDataList = new ArrayList<>();
		int deployFailedRecords = deployService.checkandCreateDeployment(projectBasicConfigId,
				pushBuildDeployCorrectData.getDeployments(), deploymentList, errorDataList, pushDataDetails);
		Assert.assertEquals(1, deployFailedRecords);
		Assert.assertEquals(0, deploymentList.size());
		Assert.assertEquals(1,
				errorDataList.stream()
						.filter(buildDeployErrorData -> MapUtils.isNotEmpty(buildDeployErrorData.getErrors()))
						.collect(Collectors.toList()).size());

	}

}
