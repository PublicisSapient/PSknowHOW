package com.publicissapient.kpidashboard.apis.mongock;

import com.publicissapient.kpidashboard.apis.util.MongockUtils;
import io.mongock.api.annotations.BeforeExecution;
import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@ChangeUnit(id = "Release_7.3.0", order = "002", author = "hargupta15", runAlways = true, systemVersion = "7.3.0")
public class Release_7_3_0 {

	private final MongoTemplate mongoTemplate;
	List<String> kpiIdsToCheck = Arrays.asList("kpi151", "kpi152");

	public Release_7_3_0(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@BeforeExecution
	private void setUp() {
		MongockUtils.checkCollectionExists(mongoTemplate, MongockUtils.KPI_COLUMN_CONFIG_COLLECTION);
		MongockUtils.checkCollectionExists(mongoTemplate, MongockUtils.KPI_MASTER_COLLECTION);
	}

	@Execution
	public void changeSet() {

		List<Document> kpiData = getKpiData(kpiIdsToCheck);
		List<Document> kpiColumnData = getKpiColumnData(kpiIdsToCheck);

		if (kpiColumnData.isEmpty()) {
			List<Document> kpiColumnConfigs = Arrays.asList(MongockUtils.createKpiColumnConfig("kpi151"),
					MongockUtils.createKpiColumnConfig("kpi152"));
			mongoTemplate.insert(kpiColumnConfigs, MongockUtils.KPI_COLUMN_CONFIG_COLLECTION);
		} else {
			System.out.println("KPI Column Config data is already present");
		}

		if (kpiData.isEmpty()) {
			List<Document> kpiDocuments = new ArrayList<>();
			kpiDocuments.add(MongockUtils.createKpiDocument("kpi151", "Backlog Count By Status", "Count", 9, "Backlog",
					"Jira", 10, "Total count of issues in the Backlog with a breakup by Status.", true, false,
					"dropdown", "chart", false));
			kpiDocuments.add(MongockUtils.createKpiDocument("kpi152", "Backlog Count By Issue Type", "Count", 10,
					"Backlog", "Jira", 10, "Total count of issues in the backlog with a breakup by issue type.", true,
					false, "dropdown", "chart", false));

			mongoTemplate.insert(kpiDocuments, "kpi_master");
		} else {
			System.out.println("KPI are already present in Kpi master");
		}

	}

	@RollbackExecution
	public void rollback() {
		rollbackKpiData(kpiIdsToCheck);
		rollbackKpiColumnData(kpiIdsToCheck);
	}

	private List<Document> getKpiData(List<String> kpiIdsToCheck) {
		Query query = Query.query(Criteria.where(MongockUtils.kpiId).in(kpiIdsToCheck));
		return mongoTemplate.find(query, Document.class, MongockUtils.KPI_MASTER_COLLECTION);
	}

	private List<Document> getKpiColumnData(List<String> kpiIdsToCheck) {
		Query query = Query.query(Criteria.where(MongockUtils.kpiId).in(kpiIdsToCheck));
		return mongoTemplate.find(query, Document.class, MongockUtils.KPI_COLUMN_CONFIG_COLLECTION);
	}

	private void rollbackKpiData(List<String> kpiIdsToCheck) {
		Criteria criteria = Criteria.where(MongockUtils.kpiId).in(kpiIdsToCheck);
		mongoTemplate.remove(Query.query(criteria), Document.class, MongockUtils.KPI_MASTER_COLLECTION);
	}

	private void rollbackKpiColumnData(List<String> kpiIdsToCheck) {
		Criteria criteria = Criteria.where(MongockUtils.kpiId).in(kpiIdsToCheck);
		mongoTemplate.remove(Query.query(criteria), Document.class, MongockUtils.KPI_COLUMN_CONFIG_COLLECTION);
	}
}
