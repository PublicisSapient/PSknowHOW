package com.publicissapient.kpidashboard.apis.sonar.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.common.model.sonar.SonarDetails;

@Component
public class TestService extends SonarKPIService<Long, List<Object>, List<SonarDetails>> {

	@Override
	public Long calculateKPIMetrics(List<SonarDetails> sonarDetails) {
		return null;
	}

	@Override
	public List<SonarDetails> fetchKPIDataFromDb(List<Node> leafNodeList, String startDate, String endDate,
			KpiRequest kpiRequest) {
		return null;
	}

	@Override
	public String getQualifierType() {
		return "TEST_SONAR";
	}

	@Override
	public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement,
			TreeAggregatorDetail treeAggregatorDetail) throws ApplicationException {
		return new KpiElement();
	}
}
