package com.publicissapient.kpidashboard.common.repository.jira;

import com.publicissapient.kpidashboard.common.model.jira.HappinessKpiData;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HappinessKpiDataRepository extends MongoRepository<HappinessKpiData, ObjectId>{

    /**
     * Find all which matches provided ids
     * @param sprintIDs sprint ids
     * @return list of HappinessKpiData details
     */
    List<HappinessKpiData> findBySprintIDIn(List<String> sprintIDs);

    HappinessKpiData findExistingByBasicProjectConfigIdAndSprintIDAndDateOfSubmission(ObjectId basicProjectConfigId, String sprintID, String dateOfSubmission);


}