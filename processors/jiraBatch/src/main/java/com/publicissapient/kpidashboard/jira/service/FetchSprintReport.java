package com.publicissapient.kpidashboard.jira.service;

import java.util.List;
import java.util.Set;

import com.publicissapient.kpidashboard.common.client.KerberosClient;
import com.publicissapient.kpidashboard.common.model.jira.SprintDetails;
import com.publicissapient.kpidashboard.jira.model.ProjectConfFieldMapping;

public interface FetchSprintReport {

    Set<SprintDetails> fetchSprints(ProjectConfFieldMapping projectConfig, Set<SprintDetails> sprintDetailsSet, Set<SprintDetails> setForCacheClean, KerberosClient krb5Client) throws InterruptedException;

    List<SprintDetails> createSprintDetailBasedOnBoard(ProjectConfFieldMapping projectConfig, Set<SprintDetails> setForCacheClean, KerberosClient krb5Client)
            throws InterruptedException;

    List<SprintDetails> getSprints(ProjectConfFieldMapping projectConfig, String boardId,
                                   KerberosClient krb5Client);
}
