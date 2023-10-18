package com.publicissapient.kpidashboard.apis.mongock;

import com.publicissapient.kpidashboard.apis.util.MongockUtils;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import org.bson.Document;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

@ChangeUnit(id = "Release_7.5.0", order = "004", author = "hargupta15", runAlways = true, systemVersion = "8.0.0")

public class Release_7_5_0 {

	private final MongoTemplate mongoTemplate;
	private static String FILENAME = "fieldName";

	public Release_7_5_0(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Execution
	public void changeSet() {
		updateFieldMappingStructure("jiraStoryIdentification",
				new Update().set("fieldLabel", "Issue Count KPI Issue type"));
		updateFieldMappingStructure("jiraIssueTypeKPI3", new Update().set("fieldLabel", "Issue type to be included")
				.set("tooltip.definition", "All issue types that should be included in Lead time calculation"));
		updateFieldMappingStructure("jiraDorKPI3", new Update().set("fieldLabel", "DOR status")
				.set("tooltip.definition", "Status/es that identify that an issue is ready to be taken in the sprint"));
		updateFieldMappingStructure("jiraLiveStatusKPI3",
				new Update().set("tooltip.definition", "Status/es that identify that an issue is LIVE in Production."));
		insertActionPolicyRule();
		insertFieldMappingStructureDocuments();
		updateFieldMappings();

	}

	@RollbackExecution
	public void rollback() {

	}

	private void updateFieldMappingStructure(String fieldNameToUpdate, Update update) {

		Query query = Query.query(Criteria.where(FILENAME).is(fieldNameToUpdate));
		mongoTemplate.updateFirst(query, update, MongockUtils.FIELD_MAPPING_STRUCTURE_COLLECTION);
	}

	private void insertActionPolicyRule() {
		Document document = new Document();
		document.append("name", "Fetch Sprint").append("roleAllowed", "")
				.append("description", "super admin and project admin can run active sprint fetch")
				.append("roleActionCheck", "action == 'TRIGGER_SPRINT_FETCH'")
				.append("condition",
						"subject.authorities.contains('ROLE_SUPERADMIN') || subject.authorities.contains('ROLE_PROJECT_ADMIN')")
				.append("createdDate", new Date()).append("lastModifiedDate", new Date()).append("isDeleted", false);

		mongoTemplate.insert(document, MongockUtils.ACTION_POLICY_RULE_COLLECTION);
	}

	private void insertFieldMappingStructureDocuments() {
		Document document1 = new Document();
		document1.append("fieldName", "jiraDodKPI37").append("fieldLabel", "Status to identify completed issues")
				.append("fieldType", "chips").append("fieldCategory", "workflow")
				.append("section", "WorkFlow Status Mapping").append("tooltip", new Document("definition",
						"Status/es that identify that an issue is completed based on Definition of Done (DoD)"));
		mongoTemplate.insert(document1, MongockUtils.FIELD_MAPPING_STRUCTURE_COLLECTION);

		Document document2 = new Document();
		document2.append("fieldName", "sprintName").append("fieldLabel", "Sprint Name").append("fieldType", "text")
				.append("fieldCategory", "fields").append("section", "Custom Fields Mapping")
				.append("tooltip", new Document("definition",
						"JIRA applications let you add custom fields in addition to the built-in fields. Sprint name is a custom field in JIRA. So User need to provide that custom field which is associated with Sprint in Users JIRA Installation."));

		mongoTemplate.insert(document2, MongockUtils.FIELD_MAPPING_STRUCTURE_COLLECTION);
	}

	void updateFieldMappings() {
		Query query = new Query(Criteria.where("jiraIssueTypeKPI37").exists(true));
		List<Document> fieldMappings = mongoTemplate.find(query, Document.class, "field_mapping");

		for (Document fieldMapping : fieldMappings) {
			Update update = new Update();
			update.set("jiraDodKPI37", fieldMapping.get("jiraDod"));
			update.unset("jiraIssueTypeKPI37");

			Query updateQuery = new Query(Criteria.where("_id").is(fieldMapping.get("_id")));
			mongoTemplate.updateFirst(updateQuery, update, Document.class, MongockUtils.FIELD_MAPPING);
		}
	}

	private void updateDRRKpiFormula() {
		Query query = new Query(Criteria
				.where("kpiId").is("kpi37")
				.and("kpiInfo.formula.operands").is("Total no. of defects reported in a sprint")
		);

		Update update = new Update().set("kpiInfo.formula.$[formulaElem].operands.$[operandElem]", "Total no. of defects Closed in a sprint");
		FindAndModifyOptions options = FindAndModifyOptions.options().returnNew(true);
		options.arrayFilters(
				Arrays.asList(
						Criteria.where("formulaElem.operands").exists(true).getCriteriaObject(),
						Criteria.where("operandElem").is("Total no. of defects reported in a sprint").getCriteriaObject()
				)
		);

		mongoTemplate.updateFirst(query, update, Document.class);
	}
}
