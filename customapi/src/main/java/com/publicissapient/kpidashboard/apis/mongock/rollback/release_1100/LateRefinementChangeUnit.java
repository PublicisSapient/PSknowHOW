package com.publicissapient.kpidashboard.apis.mongock.rollback.release_1100;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ChangeUnit(id = "r_late_refinement_update", order = "011205", author = "aksshriv1", systemVersion = "11.0.0")
public class LateRefinementChangeUnit {

	public static final String KPI_ID = "kpiId";
	public static final String KPI_NAME = "kpiName";
	public static final String KPI_MASTER = "kpi_master";
	public static final String KPI_INFO_DETAILS_0_KPI_LINK_DETAIL_LINK = "kpiInfo.details.0.kpiLinkDetail.link";
	public static final String URL_DOC = "https://knowhow.suite.publicissapient.com/wiki/spaces/PS/pages/159154188/Sprint+Readiness";
	public static final String URL_DOC_R_C = "https://knowhow.tools.publicis.sapient.com/wiki/kpi187-Late+Refinemnt+Current+Sprint";
	public static final String URL_DOC_R_F = "https://knowhow.tools.publicis.sapient.com/wiki/kpi188-Late+Refinemnt+Future+Sprint";
	private final MongoTemplate mongoTemplate;

	public LateRefinementChangeUnit(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Execution
	public void changeSet() {

		Query query187 = new Query(Criteria.where(KPI_ID).is("kpi187"));
		Update rollback187 = new Update().set(KPI_NAME, "Late Refinement (Current Sprint)")
				.set(KPI_INFO_DETAILS_0_KPI_LINK_DETAIL_LINK, URL_DOC_R_C);

		mongoTemplate.updateFirst(query187, rollback187, KPI_MASTER);

		Query query188 = new Query(Criteria.where(KPI_ID).is("kpi188"));
		Update rollback188 = new Update().set(KPI_NAME, "Late Refinement (Next Sprint)")
				.set(KPI_INFO_DETAILS_0_KPI_LINK_DETAIL_LINK, URL_DOC_R_F);

		mongoTemplate.updateFirst(query188, rollback188, KPI_MASTER);

		log.info("Rollback completed for KPI 187 and 188");
	}

	private void updateKpi187And188ToMaster(MongoTemplate mongoTemplate) {

		// Update for KPI 187
		Query query187 = new Query(Criteria.where(KPI_ID).is("kpi187"));
		Update update187 = new Update().set(KPI_NAME, "Sprint Readiness (Current Sprint)")
				.set(KPI_INFO_DETAILS_0_KPI_LINK_DETAIL_LINK, URL_DOC);

		mongoTemplate.updateFirst(query187, update187, KPI_MASTER);
		log.info("Updated kpiName for kpiId=kpi187");

		// Update for KPI 188
		Query query188 = new Query(Criteria.where(KPI_ID).is("kpi188"));
		Update update188 = new Update().set(KPI_NAME, "Sprint Readiness (Next Sprint)")
				.set(KPI_INFO_DETAILS_0_KPI_LINK_DETAIL_LINK, URL_DOC);
		mongoTemplate.updateFirst(query188, update188, KPI_MASTER);
		log.info("Updated kpiName for kpiId=kpi188");
	}

	@RollbackExecution
	public void rollback() {
		updateKpi187And188ToMaster(mongoTemplate);
	}
}
