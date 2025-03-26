package com.publicissapient.kpidashboard.rally.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.publicissapient.kpidashboard.rally.model.ProjectConfFieldMapping;
import com.publicissapient.kpidashboard.rally.model.RallyReleaseResponse;
import com.publicissapient.kpidashboard.rally.util.RallyRestClient;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.publicissapient.kpidashboard.common.client.KerberosClient;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.HierarchyLevel;
import com.publicissapient.kpidashboard.common.model.application.ProjectBasicConfig;
import com.publicissapient.kpidashboard.common.model.application.ProjectHierarchy;
import com.publicissapient.kpidashboard.common.model.application.ProjectRelease;
import com.publicissapient.kpidashboard.common.model.application.ProjectVersion;
import com.publicissapient.kpidashboard.common.repository.application.ProjectReleaseRepo;
import com.publicissapient.kpidashboard.common.service.HierarchyLevelService;
import com.publicissapient.kpidashboard.common.service.ProjectHierarchyService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class FetchScrumReleaseDataImpl implements FetchScrumReleaseData {

    @Autowired
    private ProjectReleaseRepo projectReleaseRepo;
    @Autowired
    private HierarchyLevelService hierarchyLevelService;
    @Autowired
    private ProjectHierarchyService projectHierarchyService;
    @Autowired
    private ProjectHierarchySyncService projectHierarchySyncService;
    @Autowired
    private RallyRestClient rallyRestClient;

    @Override
    public void processReleaseInfo(ProjectConfFieldMapping projectConfig, KerberosClient krb5Client)
            throws IOException, ParseException {
        log.info("Start Fetching Release Data from Rally");
        saveProjectRelease(projectConfig);
    }

    private void saveProjectRelease(ProjectConfFieldMapping confFieldMapping) throws IOException, ParseException {
        List<ProjectVersion> projectVersionList = getRallyVersions(confFieldMapping);
        if (CollectionUtils.isNotEmpty(projectVersionList)) {
            ProjectBasicConfig projectBasicConfig = confFieldMapping.getProjectBasicConfig();
            if (null != projectBasicConfig.getProjectNodeId()) {
                ProjectRelease projectRelease = projectReleaseRepo.findByConfigId(projectBasicConfig.getId());
                projectRelease = projectRelease == null ? new ProjectRelease() : projectRelease;
                projectRelease.setListProjectVersion(projectVersionList);
                projectRelease.setProjectName(projectBasicConfig.getProjectName());
                projectRelease.setProjectId(projectBasicConfig.getProjectNodeId());
                projectRelease.setConfigId(projectBasicConfig.getId());
                saveScrumAccountHierarchy(projectBasicConfig, projectRelease);
                projectReleaseRepo.save(projectRelease);
            }
            log.debug("Rally versions processed: {}", 
                    projectVersionList.stream().map(ProjectVersion::getName).collect(Collectors.toList()));
        }
    }

    private List<ProjectVersion> getRallyVersions(ProjectConfFieldMapping projectConfig) throws JsonProcessingException {
        List<ProjectVersion> versions = new ArrayList<>();
        String releasesUrl = String.format("%s/release", rallyRestClient.getBaseUrl());
        
        ResponseEntity<RallyReleaseResponse> response = rallyRestClient.get(releasesUrl, projectConfig, RallyReleaseResponse.class);
        if (response != null && response.getBody() != null && CollectionUtils.isNotEmpty(response.getBody().getQueryResult().getResults()
        )) {
            versions = response.getBody().getQueryResult().getResults().stream()
                    .map(release -> {
                        ProjectVersion version = new ProjectVersion();
                        version.setId(release.getId());
                        version.setName(release.getName());
                        version.setDescription(release.getDescription());
                        version.setStartDate(release.getReleaseStartDate());
                        version.setReleaseDate(release.getReleaseDate());
                        version.setReleased("Released".equalsIgnoreCase(release.getState()));
                        return version;
                    })
                    .collect(Collectors.toList());
        }
        return versions;
    }

    private void saveScrumAccountHierarchy(ProjectBasicConfig projectConfig, ProjectRelease projectRelease) {
        Map<String, ProjectHierarchy> existingHierarchy = projectHierarchyService
                .getProjectHierarchyMapByConfigIdAndHierarchyLevelId(projectConfig.getId().toString(),
                        CommonConstant.HIERARCHY_LEVEL_ID_RELEASE);

        Set<ProjectHierarchy> setToSave = new HashSet<>();
        List<ProjectHierarchy> hierarchyForRelease = createScrumHierarchyForRelease(projectRelease, projectConfig);
        setToSaveAccountHierarchy(setToSave, hierarchyForRelease, existingHierarchy);
        projectHierarchySyncService.syncReleaseHierarchy(projectConfig.getId(), hierarchyForRelease);
        if (CollectionUtils.isNotEmpty(setToSave)) {
            log.info("Updated Rally Release Hierarchies: {}", setToSave.size());
            projectHierarchyService.saveAll(setToSave);
        }
    }

    private void setToSaveAccountHierarchy(Set<ProjectHierarchy> setToSave, List<ProjectHierarchy> accountHierarchy,
            Map<String, ProjectHierarchy> existingHierarchy) {
        if (CollectionUtils.isNotEmpty(accountHierarchy)) {
            accountHierarchy.forEach(hierarchy -> {
                if (StringUtils.isNotBlank(hierarchy.getParentId())) {
                    ProjectHierarchy exHiery = existingHierarchy.get(hierarchy.getNodeId());
                    if (null == exHiery) {
                        hierarchy.setCreatedDate(LocalDateTime.now());
                        setToSave.add(hierarchy);
                    } else if (!exHiery.equals(hierarchy)) {
                        exHiery.setBeginDate(hierarchy.getBeginDate());
                        exHiery.setNodeName(hierarchy.getNodeName());
                        exHiery.setEndDate(hierarchy.getEndDate());
                        exHiery.setReleaseState(hierarchy.getReleaseState());
                        setToSave.add(exHiery);
                    }
                }
            });
        }
    }

    private List<ProjectHierarchy> createScrumHierarchyForRelease(ProjectRelease projectRelease,
            ProjectBasicConfig projectBasicConfig) {
        log.info("Creating Rally Release Hierarchy");
        List<HierarchyLevel> hierarchyLevelList = hierarchyLevelService
                .getFullHierarchyLevels(projectBasicConfig.isKanban());
        Map<String, HierarchyLevel> hierarchyLevelsMap = hierarchyLevelList.stream()
                .collect(Collectors.toMap(HierarchyLevel::getHierarchyLevelId, x -> x));
        HierarchyLevel hierarchyLevel = hierarchyLevelsMap.get(CommonConstant.HIERARCHY_LEVEL_ID_RELEASE);

        List<ProjectHierarchy> hierarchyArrayList = new ArrayList<>();
        try {
            projectRelease.getListProjectVersion().forEach(projectVersion -> {
                ProjectHierarchy releaseHierarchy = new ProjectHierarchy();
                releaseHierarchy.setBasicProjectConfigId(projectBasicConfig.getId());
                releaseHierarchy.setHierarchyLevelId(hierarchyLevel.getHierarchyLevelId());
                String versionName = projectVersion.getName();
                String versionId = projectVersion.getId() + CommonConstant.ADDITIONAL_FILTER_VALUE_ID_SEPARATOR +
                        projectBasicConfig.getProjectNodeId();
                releaseHierarchy.setNodeId(versionId);
                releaseHierarchy.setNodeName(versionName);
                releaseHierarchy.setNodeDisplayName(versionName);
                releaseHierarchy.setBeginDate(ObjectUtils.isNotEmpty(projectVersion.getStartDate())
                        ? projectVersion.getStartDate().toString()
                        : CommonConstant.BLANK);
                releaseHierarchy.setEndDate(ObjectUtils.isNotEmpty(projectVersion.getReleaseDate())
                        ? projectVersion.getReleaseDate().toString()
                        : CommonConstant.BLANK);
                releaseHierarchy.setReleaseState(projectVersion.isReleased() ? CommonConstant.RELEASED : CommonConstant.UNRELEASED);
                releaseHierarchy.setParentId(projectBasicConfig.getProjectNodeId());
                hierarchyArrayList.add(releaseHierarchy);
            });
        } catch (Exception e) {
            log.error("Rally Processor Failed to get Release Hierarchy data", e);
        }
        return hierarchyArrayList;
    }
}
