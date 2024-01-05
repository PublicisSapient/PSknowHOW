package com.publicissapient.kpidashboard.apis.sonar.rest;

import java.util.List;
import java.util.Map;

import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.errors.ApplicationException;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.model.TreeAggregatorDetail;
import com.publicissapient.kpidashboard.apis.sonar.service.SonarKPIService;
import com.publicissapient.kpidashboard.common.model.sonar.SonarDetails;
import org.springframework.stereotype.Component;

@Component
public class TestService extends SonarKPIService<Long, List<Object>, List<SonarDetails>> {

    @Override
    public Long calculateKPIMetrics(List<SonarDetails> sonarDetails) {
		return null;
    }

    @Override
    public List<SonarDetails> fetchKPIDataFromDb(List<Node> leafNodeList, String startDate, String endDate, KpiRequest kpiRequest) {
        return null;
    }

    @Override
    public String getQualifierType() {
        return "TEST_SONAR";
    }

    @Override
    public KpiElement getKpiData(KpiRequest kpiRequest, KpiElement kpiElement, TreeAggregatorDetail treeAggregatorDetail) throws ApplicationException {
        return new KpiElement();
    }

    @Override
    public Map<String, Object> getSonarJobWiseKpiData(List<Node> projectList, Map<String, Node> tempMap, KpiElement kpiElement) {
        return null;
    }
}
