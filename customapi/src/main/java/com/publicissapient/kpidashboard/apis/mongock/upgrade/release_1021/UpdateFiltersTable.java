package com.publicissapient.kpidashboard.apis.mongock.upgrade.release_1021;

import com.publicissapient.kpidashboard.common.model.application.Filters;
import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.Arrays;
import java.util.List;

@Slf4j
@ChangeUnit(id = "update_filters", order = "10208", author = "girpatha", systemVersion = "10.2.1")
public class UpdateFiltersTable {

    private final MongoTemplate mongoTemplate;

    public UpdateFiltersTable(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Execution
    public void changeSet() {
        // Remove all documents from the filters collection
        mongoTemplate.dropCollection("filters");

        // Insert new documents into the filters collection
        List<Filters> filtersList = Arrays.asList(
                new Filters(1, new Filters.ProjectTypeSwitch(true, true), new Filters.BasicFilter("multiSelect", new Filters.DefaultLevel("project", null)), new Filters.ParentFilter("Organization Level", null), Arrays.asList(new Filters.BasicFilter("multiSelect", new Filters.DefaultLevel("sprint", null)), new Filters.BasicFilter("multiSelect", new Filters.DefaultLevel("sqd", null)))),
                new Filters(2, new Filters.ProjectTypeSwitch(true, true), new Filters.BasicFilter("multiSelect", new Filters.DefaultLevel("project", null)), new Filters.ParentFilter("Organization Level", null), Arrays.asList(new Filters.BasicFilter("multiSelect", new Filters.DefaultLevel("sprint", null)), new Filters.BasicFilter("multiSelect", new Filters.DefaultLevel("sqd", null)))),
                new Filters(3, new Filters.ProjectTypeSwitch(true, true), new Filters.BasicFilter("multiSelect", new Filters.DefaultLevel("project", null)), new Filters.ParentFilter("Organization Level", null), Arrays.asList(new Filters.BasicFilter("multiSelect", new Filters.DefaultLevel("sprint", null)), new Filters.BasicFilter("multiSelect", new Filters.DefaultLevel("sqd", null)))),
                new Filters(4, new Filters.ProjectTypeSwitch(true, true), new Filters.BasicFilter("multiSelect", new Filters.DefaultLevel("project", null)), new Filters.ParentFilter("Organization Level", null), null),
                new Filters(5, new Filters.ProjectTypeSwitch(false, true), new Filters.BasicFilter("singleSelect", new Filters.DefaultLevel("sprint", "sprintState")), new Filters.ParentFilter("Project", "sprint"), Arrays.asList(new Filters.BasicFilter("multiSelect", new Filters.DefaultLevel("sqd", null)))),
                new Filters(6, new Filters.ProjectTypeSwitch(true, true), new Filters.BasicFilter("singleSelect", new Filters.DefaultLevel("project", null)), null, Arrays.asList(new Filters.BasicFilter("singleSelect", new Filters.DefaultLevel("branch", null)), new Filters.BasicFilter("singleSelect", new Filters.DefaultLevel("developer", null)))),
                new Filters(15, new Filters.ProjectTypeSwitch(false, true), new Filters.BasicFilter("singleSelect", new Filters.DefaultLevel("release", "releaseState")), new Filters.ParentFilter("Project", "release"), null),
                new Filters(7, new Filters.ProjectTypeSwitch(true, true), new Filters.BasicFilter("singleSelect", new Filters.DefaultLevel("project", null)), new Filters.ParentFilter("Organization Level", null), null),
                new Filters(16, new Filters.ProjectTypeSwitch(false, true), new Filters.BasicFilter("singleSelect", new Filters.DefaultLevel("project", null)), null, null),
                new Filters(17, new Filters.ProjectTypeSwitch(true, true), new Filters.BasicFilter("singleSelect", new Filters.DefaultLevel("project", null)), new Filters.ParentFilter("Organization Level", null), null)
        );

        mongoTemplate.insertAll(filtersList);
    }

    @RollbackExecution
    public void rollback() {
        // Remove all documents from the filters collection
        mongoTemplate.dropCollection("filters");
    }

}