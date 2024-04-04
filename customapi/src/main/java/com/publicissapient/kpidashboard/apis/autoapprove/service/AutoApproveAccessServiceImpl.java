package com.publicissapient.kpidashboard.apis.autoapprove.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.common.model.rbac.AutoApproveAccessConfig;
import com.publicissapient.kpidashboard.common.model.rbac.RoleData;
import com.publicissapient.kpidashboard.common.repository.rbac.AutoApproveAccessConfigRepository;
import com.publicissapient.kpidashboard.common.repository.rbac.RolesRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AutoApproveAccessServiceImpl implements AutoApproveAccessService {
	@Autowired
	private AutoApproveAccessConfigRepository autoAccessRepository;

	@Autowired
	private RolesRepository rolesRepository;

	@Override
	public AutoApproveAccessConfig saveAutoApproveConfig(AutoApproveAccessConfig autoApproveRole) {
		autoAccessRepository.save(autoApproveRole);
		log.info("Successfully pushed role into roles db");
		return autoApproveRole;
	}

	@Override
	public AutoApproveAccessConfig getAutoApproveConfig() {
		List<AutoApproveAccessConfig> listAutoApproveAccessConfig = autoAccessRepository.findAll();
		if (CollectionUtils.isNotEmpty(listAutoApproveAccessConfig)) {
			return listAutoApproveAccessConfig.get(0);
		}
		return null;
	}

	@Override
	public AutoApproveAccessConfig modifyAutoApprovConfigById(String id, AutoApproveAccessConfig autoApproveRole) {
		List<RoleData> rolesDataList = new ArrayList<>();
		if (null != autoApproveRole.getRoles()) {
			rolesDataList = autoApproveRole.getRoles().stream()
					.filter(roleData -> rolesRepository.findByRoleName(roleData.getRoleName()) != null)
					.collect(Collectors.toList());
		}

		autoApproveRole.setRoles(rolesDataList);
		autoAccessRepository.save(autoApproveRole);
		return autoApproveRole;
	}

	@Override
	public boolean isAutoApproveEnabled(String roleName) {

		AutoApproveAccessConfig autoApproveConfig = getAutoApproveConfig();
		if (autoApproveConfig == null) {
			return false;
		}

		boolean isEnabled = autoApproveConfig.getEnableAutoApprove().equalsIgnoreCase("true");
		List<String> roles = autoApproveConfig.getRoles().stream().map(RoleData::getRoleName)
				.collect(Collectors.toList());
		return isEnabled && roles.contains(roleName);
	}

}
