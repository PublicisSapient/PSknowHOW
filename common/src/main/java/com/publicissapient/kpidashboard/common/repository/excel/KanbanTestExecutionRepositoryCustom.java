package com.publicissapient.kpidashboard.common.repository.excel;

import java.util.List;
import java.util.Map;

import com.publicissapient.kpidashboard.common.model.testexecution.KanbanTestExecution;

/**
 * The interface Test execution detail custom repository.
 *
 */
public interface KanbanTestExecutionRepositoryCustom {

	/**
	 * Returns TestExecutionDetail documents for selected filters and date range.
	 * filters applied
	 * 
	 * @param uniqueProjectMap
	 *            project specific filters
	 * @param dateFrom
	 *            start date
	 * @param dateTo
	 *            end date
	 * @return result list
	 */
	List<KanbanTestExecution> findTestExecutionDetailByFilters(Map<String, List<String>> mapOfFilters,
			Map<String, Map<String, Object>> uniqueProjectMap, String dateFrom, String dateTo);
}
