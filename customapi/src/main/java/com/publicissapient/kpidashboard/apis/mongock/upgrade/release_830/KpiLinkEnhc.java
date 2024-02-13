/*
 * Copyright 2014 CapitalOne, LLC.
 * Further development Copyright 2022 Sapient Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.publicissapient.kpidashboard.apis.mongock.upgrade.release_830;

import com.publicissapient.kpidashboard.common.model.application.KpiMaster;
import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

/**
 * @author shunaray
 */
@SuppressWarnings("java:S1192")
@ChangeUnit(id = "kpi_link_enhc", order = "8333", author = "shunaray", systemVersion = "8.3.3")
public class KpiLinkEnhc {

    private final MongoTemplate mongoTemplate;

    public KpiLinkEnhc(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Execution
    public void execution() {
        updateOrInsertLinkDetail("kpi14", "NAME : Defect Injection Rate", "cat : S/Q/V", "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/83853321/Defect+Injection+Rate");
        updateOrInsertLinkDetail("kpi82", "NAME : First Time Pass Rate", "cat : S/Q/V", "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/84049922/First+time+pass+rate+FTPR");
        updateOrInsertLinkDetail("kpi111", "NAME : Defect Density", "cat : S/Q/V", "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/83886083/Defect+Density");
        updateOrInsertLinkDetail("kpi35", "NAME : Defect Seepage Rate", "cat : S/Q/V", "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/84049938/Defect+Seepage+Rate");
        updateOrInsertLinkDetail("kpi34", "NAME : Defect Removal Efficiency", "cat : S/Q/V", "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/83886099/Defect+Removal+Efficiency");
        updateOrInsertLinkDetail("kpi37", "NAME : Defect Rejection Rate", "cat : S/Q/V", "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/83886115/Defect+Rejection+Rate");
        updateOrInsertLinkDetail("kpi28", "NAME : Defect Count By Priority", "cat : S/Q/V", "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/83820546/Defect+Count+by+Priority+Quality");
        updateOrInsertLinkDetail("kpi36", "NAME : Defect Count By RCA", "cat : S/Q/V", "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/83820562/Defect+Count+By+RCA+Quality");
        updateOrInsertLinkDetail("kpi126", "NAME : Created vs Resolved defects", "cat : S/Q/V", "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/83886196/Created+vs+Resolved");
        updateOrInsertLinkDetail("kpi42", "NAME : Regression Automation Coverage", "cat : S/Q/V", "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/84049954/Regression+Automation+Coverage");
        updateOrInsertLinkDetail("kpi16", "NAME : In-Sprint Automation Coverage", "cat : S/Q/V", "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/83886131/In-Sprint+Automation+Coverage");
        updateOrInsertLinkDetail("kpi17", "NAME : Unit Test Coverage", "cat : S/Q/V", "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/83886147/Unit+Test+Coverage");
        updateOrInsertLinkDetail("kpi38", "NAME : Sonar Violations", "cat : S/Q/V", "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/84049987/Sonar+Violations");
        updateOrInsertLinkDetail("kpi27", "NAME : Sonar Tech Debt", "cat : S/Q/V", "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/83886163/Sonar+Tech+Debt");
        updateOrInsertLinkDetail("kpi116", "NAME : Change Failure Rate", "cat : Dora", "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/71958608/DORA+Change+Failure+Rate");
        updateOrInsertLinkDetail("kpi70", "NAME : Test Execution and pass percentage", "cat : S/Q/V", "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/84049970/Test+Execution+and+pass+percentage");
        updateOrInsertLinkDetail("kpi40", "NAME : Issue Count", "cat : S/Q/V", "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/84050003/Issue+Count");
        updateOrInsertLinkDetail("kpi72", "NAME : Commitment Reliability", "cat : S/Q/V", "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/84050019/Commitment+Reliability");
        updateOrInsertLinkDetail("kpi5", "NAME : Sprint Predictability", "cat : S/Q/V", "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/83886228/Sprint+Predictability");
        updateOrInsertLinkDetail("kpi39", "NAME : Sprint Velocity", "cat : S/Q/V", "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/83886212/Sprint+Velocity");
        updateOrInsertLinkDetail("kpi46", "NAME : Sprint Capacity Utilization", "cat : S/Q/V", "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/83820594/Sprint+Capacity+Utilization");
        updateOrInsertLinkDetail("kpi84", "NAME : Mean Time To Merge", "cat : Developer", "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/70713477/Developer+Mean+time+to+Merge");
        updateOrInsertLinkDetail("kpi11", "NAME : Check-Ins & Merge Requests", "cat : Developer", "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/70451310/Developer+No.+of+Check-ins+and+Merge+Requests");
        updateOrInsertLinkDetail("kpi8", "NAME : Code Build Time", "cat : S/Q/V", "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/83886260/Code+Build+Time");
        updateOrInsertLinkDetail("kpi118", "NAME : Deployment Frequency", "cat : Dora", "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/71827544/DORA+Deployment+Frequency");
        updateOrInsertLinkDetail("kpi73", "NAME : Release Frequency", "cat : S/Q/V", "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/84050035/Release+Frequency");
        updateOrInsertLinkDetail("kpi113", "NAME : Value delivered (Cost of Delay)", "cat : S/Q/V", "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/84050051/Value+delivered+Cost+of+Delay");
        updateOrInsertLinkDetail("kpi119", "NAME : Work Remaining", "cat : Iteration", "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/70680609/Work+Remaining");
        updateOrInsertLinkDetail("kpi128", "NAME : Planned Work Status", "cat : Iteration", "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/70713345/Planned+Work+Status");
        updateOrInsertLinkDetail("kpi75", "NAME : Estimate vs Actual", "cat : Iteration", "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/70680658/Estimate+vs+Actual");
        updateOrInsertLinkDetail("kpi123", "NAME : Issues likely to Spill", "cat : Iteration", "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/70713361/Issues+likely+to+spill");
        updateOrInsertLinkDetail("kpi122", "NAME : Closure Possible Today", "cat : Iteration", "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/70713377/Closures+possible+today");
        updateOrInsertLinkDetail("kpi120", "NAME : Iteration Commitment", "cat : Iteration", "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/70418594/Iteration+Commitment");
        updateOrInsertLinkDetail("kpi124", "NAME : Estimation Hygiene", "cat : Iteration", "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/70680674/Estimate+Hygiene");
        updateOrInsertLinkDetail("kpi132", "NAME : Defect Count by RCA", "cat : Iteration", "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/70713409/Defect+count+by+RCA");
        updateOrInsertLinkDetail("kpi133", "NAME : Quality Status", "cat : Iteration", "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/70680625/Quality+Status");
        updateOrInsertLinkDetail("kpi134", "NAME : Unplanned Work Status", "cat : Iteration", "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/70680593/Unplanned+Work+Status");
        updateOrInsertLinkDetail("kpi125", "NAME : Iteration Burnup", "cat : Iteration", "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/70680577/Iteration+Burnup");
        updateOrInsertLinkDetail("kpi131", "NAME : Wastage", "cat : Iteration", "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/70713393/Wastage");
        updateOrInsertLinkDetail("kpi135", "NAME : First Time Pass Rate", "cat : Iteration", "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/70680642/First+time+pass+rate");
        updateOrInsertLinkDetail("kpi129", "NAME : Issues Without Story Link", "cat : Backlog", "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/84050083/Issues+without+story+link");
        updateOrInsertLinkDetail("kpi127", "NAME : Production Defects Ageing", "cat : Backlog", "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/83886319/Production+defects+Ageing");
        updateOrInsertLinkDetail("kpi139", "NAME : Refinement Rejection Rate", "cat : Backlog", "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/83886335/Refinement+Rejection+Rate");
        updateOrInsertLinkDetail("kpi136", "NAME : Defect Count by Status", "cat : Iteration", "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/70713425/Defect+count+by+Status");
        updateOrInsertLinkDetail("kpi137", "NAME : Defect Reopen Rate", "cat : Backlog", "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/83820630/Defects+Reopen+rate");
        updateOrInsertLinkDetail("kpi141", "NAME : Defect Count by Status", "cat : Release", "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/79986689/Release+Defect+count+by+Status");
        updateOrInsertLinkDetail("kpi142", "NAME : Defect Count by RCA", "cat : Release", "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/79953937/Release+Defect+count+by+RCA");
        updateOrInsertLinkDetail("kpi143", "NAME : Defect Count by Assignee", "cat : Release", "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/79691782/Release+Defect+count+by+Assignee");
        updateOrInsertLinkDetail("kpi144", "NAME : Defect Count by Priority", "cat : Release", "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/79953921/Release+Defect+count+by+Priority");
        updateOrInsertLinkDetail("kpi140", "NAME : Defect Count by Priority", "cat : Iteration", "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/70713441/Defect+count+by+Priority");
        updateOrInsertLinkDetail("kpi147", "NAME : Release Progress", "cat : Release", "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/79757314/Release+Release+Progress");
        updateOrInsertLinkDetail("kpi145", "NAME : Dev Completion Status", "cat : Iteration", "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/99483649/Dev+Completion+Status");
        updateOrInsertLinkDetail("kpi138", "NAME : Backlog Readiness", "cat : Backlog", "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/83886286/Backlog+Readiness");
        updateOrInsertLinkDetail("kpi3", "NAME : Lead Time", "cat : Backlog", "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/70811702/Lead+time");
        updateOrInsertLinkDetail("kpi148", "NAME : Flow Load", "cat : Backlog", "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/83820646/Flow+Load");
        updateOrInsertLinkDetail("kpi146", "NAME : Flow Distribution", "cat : Backlog", "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/84050099/Flow+Distribution");
        updateOrInsertLinkDetail("kpi149", "NAME : Happiness Index", "cat : S/Q/V", "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/41582623/People");
        updateOrInsertLinkDetail("kpi150", "NAME : Release Burnup", "cat : Release", "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/70484023/Release+Release+Burnup");
        updateOrInsertLinkDetail("kpi151", "NAME : Backlog Count By Status", "cat : Backlog", "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/83820678/Backlog+Count+by+Status");
        updateOrInsertLinkDetail("kpi152", "NAME : Backlog Count By Issue Type", "cat : Backlog", "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/84050115/Backlog+Count+by+Issue+type");
        updateOrInsertLinkDetail("kpi153", "NAME : PI Predictability", "cat : S/Q/V", "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/84050067/PI+Predictability");
        updateOrInsertLinkDetail("kpi155", "NAME : Defect Count By Type", "cat : Backlog", "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/98140489/Defect+count+by+Type");
        updateOrInsertLinkDetail("kpi164", "NAME : Scope Churn", "cat : S/Q/V", "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/83886244/Scope+Churn");
        updateOrInsertLinkDetail("kpi161", "NAME : Iteration Readiness", "cat : Backlog", "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/83886303/Iteration+Readiness");
        updateOrInsertLinkDetail("kpi163", "NAME : Defect by Testing Phase", "cat : Release", "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/98140473/Release+Defect+count+by+Testing+phase");
        updateOrInsertLinkDetail("kpi156", "NAME : Lead Time For Change", "cat : Dora", "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/71663772/DORA+Lead+time+for+changes");
        updateOrInsertLinkDetail("kpi157", "NAME : Check-Ins & Merge Requests", "cat : Developer", "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/70451310/Developer+No.+of+Check-ins+and+Merge+Requests");
        updateOrInsertLinkDetail("kpi158", "NAME : Mean Time To Merge", "cat : Developer", "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/70713477/Developer+Mean+time+to+Merge");
        updateOrInsertLinkDetail("kpi160", "NAME : Pickup Time", "cat : Developer", "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/70680716/Developer+Pickup+time");
        updateOrInsertLinkDetail("kpi162", "NAME : PR Size", "cat : Developer", "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/70713493/Developer+PR+Size");
        updateOrInsertLinkDetail("kpi165", "NAME : Epic Progress", "cat : Release", "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/79986705/Release+Epic+Progress");
        updateOrInsertLinkDetail("kpi169", "NAME : Epic Progress", "cat : Backlog", "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/83820662/Epic+Progress");
        updateOrInsertLinkDetail("kpi168", "NAME : Sonar Code Quality", "cat : S/Q/V", "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/83886180/Sonar+Code+Quality");
        updateOrInsertLinkDetail("kpi166", "NAME : Mean Time to Recover", "cat : Dora", "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/71663785/DORA+Mean+time+to+Restore");
        updateOrInsertLinkDetail("kpi170", "NAME : Flow Efficiency", "cat : Backlog", "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/71827496/Flow+Efficiency");
        updateOrInsertLinkDetail("kpi171", "NAME : Cycle Time", "cat : Backlog", "https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/70418714/Cycle+time");
    }


    public void updateOrInsertLinkDetail(String kpiId, String kpiName, String category, String newLink) {
        Query kpiQuery = new Query(Criteria.where("kpiId").is(kpiId));
        Update kpiUpdate = new Update();

        Query linkExistQuery = new Query(Criteria.where("kpiId").is(kpiId)
                .and("kpiInfo.details.kpiLinkDetail.link").exists(true)); // Include additional criteria

        // Check if any document has a link
        boolean linkExists = mongoTemplate.exists(linkExistQuery, KpiMaster.class);

        if (linkExists) {
            // Update the existing link to the new one
            kpiUpdate.set("kpiInfo.details.$[elem].kpiLinkDetail.link", newLink); // Use positional operator directly
            kpiUpdate.filterArray(Criteria.where("elem.kpiLinkDetail.link").exists(true));
        } else {
            // Add a new link
            kpiUpdate.addToSet("kpiInfo.details", new Document("type", "link")
                    .append("kpiLinkDetail", new Document("text", "Detailed Information at")
                            .append("link", newLink)));
        }

        mongoTemplate.updateFirst(kpiQuery, kpiUpdate, KpiMaster.class);
    }

    @RollbackExecution
    public void rollback() {
        //NOT required
    }

}
