package com.publicissapient.kpidashboard.apis.common.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Lists;
import com.publicissapient.kpidashboard.apis.appsetting.service.ConfigHelperService;
import com.publicissapient.kpidashboard.apis.config.CustomApiConfig;
import com.publicissapient.kpidashboard.apis.constant.Constant;
import com.publicissapient.kpidashboard.apis.enums.KPICode;
import com.publicissapient.kpidashboard.apis.model.KpiElement;
import com.publicissapient.kpidashboard.apis.model.KpiRequest;
import com.publicissapient.kpidashboard.apis.model.Node;
import com.publicissapient.kpidashboard.apis.util.AggregationUtils;
import com.publicissapient.kpidashboard.common.constant.CommonConstant;
import com.publicissapient.kpidashboard.common.model.application.AdditionalFilterCategory;
import com.publicissapient.kpidashboard.common.model.application.DataCount;
import com.publicissapient.kpidashboard.common.model.application.DataValue;
import com.publicissapient.kpidashboard.common.model.application.FieldMapping;
import com.publicissapient.kpidashboard.common.model.application.HierarchyLevel;
import com.publicissapient.kpidashboard.common.model.application.KpiMaster;

public abstract class ToolsKPIService<R, S> {

	private Set<String> cumulativeTrend = new HashSet<>(Arrays.asList(KPICode.UNIT_TEST_COVERAGE.name(),
			KPICode.UNIT_TEST_COVERAGE_KANBAN.name(), KPICode.SONAR_TECH_DEBT_KANBAN.name(),
			KPICode.SONAR_TECH_DEBT.name(), KPICode.NUMBER_OF_CHECK_INS.name(), KPICode.CODE_BUILD_TIME_KANBAN.name(),
			KPICode.TEST_EXECUTION_KANBAN.name()));

	private Set<String> reverseTrendList = new HashSet<>(Arrays.asList(KPICode.CODE_COMMIT.name(),
			KPICode.MEAN_TIME_TO_MERGE.name(), KPICode.PRODUCTION_ISSUES_BY_PRIORITY_AND_AGING.name(),
			KPICode.OPEN_TICKET_AGING_BY_PRIORITY.name(), KPICode.PI_PREDICTABILITY.name()));

	@Autowired
	private CustomApiConfig customApiConfig;

	@Autowired
	private CommonService commonService;

	@Autowired
	private CacheService cacheService;

	@Autowired
	private ConfigHelperService configHelperService;

	/**
	 * Calculates the aggregated value for the nodes in the bottom-up fashion.
	 * nodeWiseKPIValue is added explicitly to contain the values of each node to
	 * serve the excel data API's and other use case where all the node details
	 * needed.
	 *
	 * @param node
	 *            node
	 * @param nodeWiseKPIValue
	 *            nodeWiseKPIValue
	 * @param kpiCode
	 *            kpiCode
	 * @return value of node
	 */
	public Object calculateAggregatedValue(Node node, Map<Pair<String, String>, Node> nodeWiseKPIValue,
			KPICode kpiCode) {

		String kpiName = kpiCode.name();
		String kpiId = kpiCode.getKpiId();

		if (node == null || null == node.getValue()) {
			DataCount dataCount = new DataCount();
			dataCount.setData("0");
			dataCount.setValue(0);
			return dataCount;
		}

		List<Node> children = node.getChildren();
		if (CollectionUtils.isEmpty(node.getChildren())) {
			nodeWiseKPIValue.put(Pair.of(node.getGroupName().toUpperCase(), node.getId()), node);
			return node.getValue();
		}

		List<DataCount> aggregatedValueList = new ArrayList<>();

		for (Node child : children) {
			nodeWiseKPIValue.put(Pair.of(node.getGroupName().toUpperCase(), node.getId()), node);
			Object obj = calculateAggregatedValue(child, nodeWiseKPIValue, kpiCode);
			List<DataCount> value = obj instanceof List<?> ? ((List<DataCount>) obj) : null;
			if (value != null) {
				aggregatedValueList.addAll(value);
			}
		}
		if (CollectionUtils.isNotEmpty(aggregatedValueList)) {
			node.setValue(calculateAggregatedValue(kpiName, aggregatedValueList, node, kpiId));
			nodeWiseKPIValue.put(Pair.of(node.getGroupName().toUpperCase(), node.getId()), node);
		}
		return node.getValue();

	}

	/**
	 * Calculates the aggregated value for the nodes containing map or has filtering
	 * functionality in the bottom-up fashion. nodeWiseKPIValue is added explicitly
	 * to contain the values of each node to serve the excel data API's and other
	 * use case where all the node details needed.
	 *
	 * @param node
	 *            node
	 * @param nodeWiseKPIValue
	 *            nodeWiseKPIValue
	 * @param kpiCode
	 *            kpiCode
	 * @return value of node
	 */
	public Map<String, List<DataCount>> calculateAggregatedValueMap(Node node,
			Map<Pair<String, String>, Node> nodeWiseKPIValue, KPICode kpiCode) {
		String kpiName = kpiCode.name();
		String kpiId = kpiCode.getKpiId();

		if (node == null) {
			return new HashMap<>();
		}

		if (!(node.getValue() instanceof HashMap) && null != node.getValue() && (int) node.getValue() == 0) {
			Map<String, Double> defaultMap = new HashMap<>();
			defaultMap.put(Constant.DEFAULT, 0.0d);
			node.setValue(defaultMap);
		}

		List<Node> children = node.getChildren();
		if (CollectionUtils.isEmpty(children)) {
			nodeWiseKPIValue.put(Pair.of(node.getGroupName().toUpperCase(), node.getId()), node);
			return (Map<String, List<DataCount>>) node.getValue();
		}
		List<Map<String, List<DataCount>>> aggregatedValueList = new ArrayList<>();
		for (Node child : children) {
			Map<String, List<DataCount>> aggLeafValue = calculateAggregatedValueMap(child, nodeWiseKPIValue, kpiCode);
			if (MapUtils.isNotEmpty(aggLeafValue)) {
				aggregatedValueList.add(aggLeafValue);
			}
		}

		Map<String, List<DataCount>> aggMap = new HashMap<>();
		for (Map<String, List<DataCount>> capMap : aggregatedValueList) {
			for (Map.Entry<String, List<DataCount>> dataMap : capMap.entrySet()) {
				if (!(Constant.DEFAULT.equals(dataMap.getKey()))) {
					aggMap.computeIfAbsent(dataMap.getKey(), k -> new ArrayList<>()).addAll(dataMap.getValue());
				}
			}
		}
		Map<String, List<DataCount>> kpiFilterWiseDc = new HashMap<>();
		aggMap.forEach((key, value) -> {
			List<DataCount> aggData = calculateAggregatedValue(kpiName, value, node, kpiId);
			kpiFilterWiseDc.put(key, aggData);
		});
		kpiFilterWiseDc.remove(Constant.DEFAULT);
		if (MapUtils.isNotEmpty(kpiFilterWiseDc)) {
			node.setValue(kpiFilterWiseDc);
		}
		nodeWiseKPIValue.put(Pair.of(node.getGroupName().toUpperCase(), node.getId()), node);

		return (Map<String, List<DataCount>>) node.getValue();

	}

	/**
	 * Calculates the aggregated value for the nodes in the bottom-up fashion.
	 * nodeWiseKPIValue is added explicitly to contain the values of each node to
	 * serve the excel data API's and other use case where all the node details
	 * needed.
	 *
	 * @param node
	 *            node
	 * @param nodeWiseKPIValue
	 *            nodeWiseKPIValue
	 * @param kpiCode
	 *            kpiCode
	 * @return value of node
	 */
	public Object calculateAggregatedMultipleValueGroup(Node node, Map<Pair<String, String>, Node> nodeWiseKPIValue,
			KPICode kpiCode) {

		String kpiName = kpiCode.name();
		String kpiId = kpiCode.getKpiId();

		if (node == null || null == node.getValue()) {
			DataCount dataCount = new DataCount();
			dataCount.setData("0");
			dataCount.setValue(0);
			return dataCount;
		}

		List<Node> children = node.getChildren();
		if (CollectionUtils.isEmpty(node.getChildren())) {
			nodeWiseKPIValue.put(Pair.of(node.getGroupName().toUpperCase(), node.getId()), node);
			return node.getValue();
		}

		List<DataCount> aggregatedValueList = new ArrayList<>();

		for (Node child : children) {
			nodeWiseKPIValue.put(Pair.of(node.getGroupName().toUpperCase(), node.getId()), node);
			Object obj = calculateAggregatedMultipleValueGroup(child, nodeWiseKPIValue, kpiCode);
			List<DataCount> value = obj instanceof List<?> ? ((List<DataCount>) obj) : null;
			if (value != null) {
				aggregatedValueList.addAll(value);
			}
		}
		if (CollectionUtils.isNotEmpty(aggregatedValueList)) {
			node.setValue(calculateAggregatedMultipleValueGroup(kpiName, aggregatedValueList, node, kpiId));
			nodeWiseKPIValue.put(Pair.of(node.getGroupName().toUpperCase(), node.getId()), node);
		}
		return node.getValue();

	}

	/**
	 * This method set Data count
	 *
	 * @param aggregatedValueList
	 *            aggregatedValueList
	 * @param node
	 *            node
	 * @param aggregatedDataCount
	 *            aggregatedDataCount
	 */
	private void setDataCountWithoutAggregation(List<DataCount> aggregatedValueList, Node node,
			List<DataCount> aggregatedDataCount, String kpiName) {
		if (Constant.PROJECT.equalsIgnoreCase(node.getGroupName())) {
			aggregatedDataCount.addAll(aggregatedValueList);
		} else {
			aggregatedValueList.forEach(dc -> {
				DataCount dataCount = new DataCount();
				dataCount.setSprintIds(dc.getSprintIds());
				dataCount.setSprintNames(dc.getSprintNames());
				dataCount.setProjectNames(dc.getProjectNames());
				dataCount.setSProjectName(node.getName());
				dataCount.setValue(dc.getValue());
				dataCount.setLineValue(dc.getLineValue());
				dataCount.setData(dc.getData());
				dataCount.setHoverValue(dc.getHoverValue());
				dataCount.setDate(dc.getDate() == null ? kpiName : dc.getDate());
				dataCount.setDataValue(dc.getDataValue());
				aggregatedDataCount.add(dataCount);
			});
		}
	}

	/**
	 * This method aggregate node values.
	 *
	 * @param kpiName
	 *            kpiName
	 * @param aggregatedValueList
	 *            aggregatedValueList
	 * @param node
	 *            node
	 */
	public List<DataCount> calculateAggregatedValue(String kpiName, List<DataCount> aggregatedValueList, Node node,
			String kpiId) {

		Map<String, List<DataCount>> projectWiseDataCount = aggregatedValueList.stream()
				.collect(Collectors.groupingBy(DataCount::getSProjectName, Collectors.toList()));
		String howerKpiName = prepareHowerValue(kpiName);

		List<DataCount> aggregatedDataCount = new ArrayList<>();
		if (projectWiseDataCount.size() <= 1) {
			setDataCountWithoutAggregation(aggregatedValueList, node, aggregatedDataCount, howerKpiName);
		} else {
			List<List<DataCount>> indexWiseValuesList = aggregateIndexedValues(projectWiseDataCount);
			for (int i = 0; i < indexWiseValuesList.size(); i++) {
				StringBuilder projectName = new StringBuilder();
				DataCount dataCount = new DataCount();
				List<String> sprintIds = new ArrayList<>();
				List<String> sprintNames = new ArrayList<>();
				List<String> projectNames = new ArrayList<>();
				String hoverIdentifier = null;
				List<R> values = new ArrayList<>();
				// if 2nd chart is line on same chart
				List<R> lineValues = new ArrayList<>();
				List<R> aggregatedMapValues = new ArrayList<>();
				Map<String, Object> hoverValue = new HashMap<>();
				for (DataCount dc : indexWiseValuesList.get(i)) {
					if (CollectionUtils.isNotEmpty(dc.getSprintIds())) {
						sprintIds.addAll(dc.getSprintIds());
						sprintNames.addAll(dc.getSprintNames());
					}
					projectNames.add(dc.getSProjectName());
					projectName.append(dc.getSProjectName());
					hoverIdentifier = dc.getDate();
					collectAggregatedData(values, lineValues, aggregatedMapValues, dc);
					collectHoverData(hoverValue, dc);
				}
				setDataCountValue(kpiId, dataCount, values, lineValues, aggregatedMapValues);
				dataCount.setSprintIds(sprintIds);
				dataCount.setSprintNames(sprintNames);
				dataCount.setProjectNames(projectNames);
				dataCount.setSProjectName(node.getName());
				dataCount.setHoverValue(hoverValue);
				dataCount.setDate(hoverIdentifier == null ? howerKpiName : hoverIdentifier);
				aggregatedDataCount.add(i, dataCount);
			}
		}
		return aggregatedDataCount;
	}

	/**
	 * calculate Aggregated MultipleValue based on projects and sprints wise data
	 * 
	 * @param kpiName
	 * @param aggregatedValueList
	 * @param node
	 * @param kpiId
	 * @return
	 */
	public List<DataCount> calculateAggregatedMultipleValueGroup(String kpiName, List<DataCount> aggregatedValueList,
			Node node, String kpiId) {

		Map<String, List<DataCount>> projectWiseDataCount = aggregatedValueList.stream()
				.collect(Collectors.groupingBy(DataCount::getSProjectName, Collectors.toList()));
		String howerKpiName = prepareHowerValue(kpiName);

		List<DataCount> aggregatedDataCount = new ArrayList<>();
		if (projectWiseDataCount.size() <= 1) {
			setDataCountWithoutAggregation(aggregatedValueList, node, aggregatedDataCount, howerKpiName);
		} else {
			List<List<DataCount>> indexWiseValuesList = aggregateIndexedValues(projectWiseDataCount);
			for (int i = 0; i < indexWiseValuesList.size(); i++) {
				StringBuilder projectName = new StringBuilder();
				DataCount dataCount = new DataCount();
				List<String> sprintIds = new ArrayList<>();
				List<String> sprintNames = new ArrayList<>();
				List<String> projectNames = new ArrayList<>();
				String hoverIdentifier = null;
				Map<String, List<DataValue>> valueMultiLine = new HashMap<>();
				for (DataCount dc : indexWiseValuesList.get(i)) {
					if (CollectionUtils.isNotEmpty(dc.getSprintIds())) {
						sprintIds.addAll(dc.getSprintIds());
						sprintNames.addAll(dc.getSprintNames());
					}
					projectNames.add(dc.getSProjectName());
					projectName.append(dc.getSProjectName());
					hoverIdentifier = dc.getDate();
					collectAggregatedDataBasedOnLineType(valueMultiLine, dc);
				}
				setDataCountValueBasedOnLineType(kpiId, dataCount, valueMultiLine);
				dataCount.setSprintIds(sprintIds);
				dataCount.setSprintNames(sprintNames);
				dataCount.setProjectNames(projectNames);
				dataCount.setSProjectName(node.getName());
				dataCount.setDate(hoverIdentifier == null ? howerKpiName : hoverIdentifier);
				aggregatedDataCount.add(i, dataCount);
			}
		}
		return aggregatedDataCount;
	}

	/**
	 * aggregated Data Values based on multi line chart
	 * 
	 * @param kpiId
	 * @param dataCount
	 * @param valueMultiLine
	 */
	private void setDataCountValueBasedOnLineType(String kpiId, DataCount dataCount,
			Map<String, List<DataValue>> valueMultiLine) {
		if (MapUtils.isNotEmpty(valueMultiLine)) {
			List<DataValue> aggregatedDataValueList = new ArrayList<>();
			for (Map.Entry<String, List<DataValue>> entry : valueMultiLine.entrySet()) {
				DataValue aggregatedDataValue = new DataValue();
				Map<String, Object> aggregatedHoverValue = new HashMap<>();
				List<R> aggregatedValues = new ArrayList<>();
				String lineType = entry.getKey();

				List<DataValue> dataValueList = entry.getValue();
				dataValueList.stream().forEach(dataValue -> {
					aggregatedValues.add((R) dataValue.getValue());
					aggregatedDataValue.setName(dataValue.getName());
					collectHoverDataBaseOnLineType(dataValue.getHoverValue(), aggregatedHoverValue);
				});
				R aggregatedValue = calculateKpiValue(aggregatedValues, kpiId);
				aggregatedDataValue.setData(aggregatedValue.toString());
				aggregatedDataValue.setHoverValue(aggregatedHoverValue);
				aggregatedDataValue.setLineType(lineType);
				aggregatedDataValue.setValue(aggregatedValue);
				aggregatedDataValueList.add(aggregatedDataValue);
			}
			dataCount.setDataValue(aggregatedDataValueList);
		}
	}

	/**
	 * Collect Hover data based On chart Type
	 *
	 * @param dataValue
	 * @param aggregatedHoverValue
	 */
	private void collectHoverDataBaseOnLineType(Map<String, Object> dataValue,
			Map<String, Object> aggregatedHoverValue) {
		if (MapUtils.isNotEmpty(dataValue)) {
			Map<String, Object> hoverValuee = new LinkedHashMap<>(dataValue);
			if (MapUtils.isNotEmpty(hoverValuee)) {
				hoverValuee.forEach((key, value) -> {
					if (value instanceof Integer) {
						aggregatedHoverValue.computeIfPresent(key, (k, v) -> (Integer) v + (Integer) value);
					} else if (value instanceof Double) {
						aggregatedHoverValue.computeIfPresent(key, (k, v) -> (Double) v + (Double) value);
					} else if (value instanceof Long) {
						aggregatedHoverValue.computeIfPresent(key, (k, v) -> (Long) v + (Long) value);
					}
					aggregatedHoverValue.putIfAbsent(key, value);
				});
			}
		}
	}

	/**
	 * Collect Aggregated Data based on multi Line chart type
	 * 
	 * @param valueMultiLine
	 * @param dc
	 */
	private void collectAggregatedDataBasedOnLineType(Map<String, List<DataValue>> valueMultiLine, DataCount dc) {
		dc.getDataValue().stream().forEach(dataValue -> {
			valueMultiLine.computeIfPresent(dataValue.getLineType(), (k, v) -> {
				v.add(dataValue);
				return v;
			});
			List<DataValue> values = new ArrayList<>();
			values.add(dataValue);
			valueMultiLine.putIfAbsent(dataValue.getLineType(), values);
		});
	}

	private void collectHoverData(Map<String, Object> hoverValue, DataCount dc) {
		collectHoverDataBaseOnLineType(dc.getHoverValue(), hoverValue);
	}

	private String prepareHowerValue(String kpiName) {
		String newKpiName = kpiName.toUpperCase().replace("_", " ");
		if (newKpiName.contains("KANBAN")) {
			newKpiName = newKpiName.substring(7);
		}
		return newKpiName;
	}

	/**
	 * This method collect aggregated data
	 *
	 * @param values
	 *            values
	 * @param lineValues
	 *            lineValues
	 * @param aggregatedMapValues
	 *            aggregatedMapValues
	 * @param dc
	 *            data count
	 */
	private void collectAggregatedData(List<R> values, List<R> lineValues, List<R> aggregatedMapValues, DataCount dc) {
		Object obj = dc.getValue();
		if (obj instanceof HashMap<?, ?>) {
			aggregatedMapValues.add((R) obj);

		} else {
			R value = (R) obj;
			values.add(value);
			// if 2nd chart is line on same chart
			Object lineObj = dc.getLineValue();
			if (null != lineObj) {
				R lineValue = (R) lineObj;
				lineValues.add(lineValue);
			}
		}
	}

	/**
	 * This method group values to be aggregated
	 *
	 * @param projectWiseDataCount
	 *            projectWiseDataCount
	 * @return grouped value
	 */
	private List<List<DataCount>> aggregateIndexedValues(Map<String, List<DataCount>> projectWiseDataCount) {
		List<List<DataCount>> indexWiseValuesList = new ArrayList<>();
		for (Map.Entry<String, List<DataCount>> entry : projectWiseDataCount.entrySet()) {
			for (int i = 0; i < entry.getValue().size(); i++) {
				DataCount dataCount = entry.getValue().get(i);
				if (indexWiseValuesList.size() < (i + 1)) {
					indexWiseValuesList.add(i, new ArrayList<>(Arrays.asList(dataCount)));
				} else {
					indexWiseValuesList.get(i).add(dataCount);
				}
			}
		}
		return indexWiseValuesList;
	}

	/**
	 * This method aggregate values and set in data count object
	 *
	 * @param kpiId
	 *            kpiId
	 * @param dataCount
	 *            dataCount
	 * @param values
	 *            values
	 * @param lineValues
	 *            lineValues
	 * @param aggregatedMapValues
	 *            aggregatedMapValues
	 */
	private void setDataCountValue(String kpiId, DataCount dataCount, List<R> values, List<R> lineValues,
			List<R> aggregatedMapValues) {
		if (CollectionUtils.isNotEmpty(aggregatedMapValues)) {
			R aggregatedValue = calculateKpiValue(aggregatedMapValues, kpiId);
			dataCount.setValue(aggregatedValue);
		}
		if (CollectionUtils.isNotEmpty(values)) {
			R aggregatedValue = calculateKpiValue(values, kpiId);
			dataCount.setValue(aggregatedValue);
			dataCount.setData(String.valueOf(aggregatedValue));
		}
		if (CollectionUtils.isNotEmpty(lineValues)) {
			R aggregatedLineValue = calculateKpiValue(lineValues, kpiId);
			dataCount.setLineValue(aggregatedLineValue);
		}
	}

	/**
	 * This method return trend value for simple non filter KPIs
	 *
	 * @param kpiRequest
	 *            kpiRequest
	 * @param kpiElement
	 * @param nodeWiseKPIValue
	 *            nodeWiseKPIValue
	 * @return trend values
	 */
	public List<DataCount> getTrendValues(KpiRequest kpiRequest, KpiElement kpiElement,
			Map<Pair<String, String>, Node> nodeWiseKPIValue, KPICode kpiCode) {
		String kpiName = kpiCode.name();
		String kpiId = kpiCode.getKpiId();
		List<DataCount> trendValues = new ArrayList<>();
		Set<String> selectedIds = getSelectedIds(kpiRequest);
		calculateThresholdValue(selectedIds, kpiElement, kpiRequest.getLabel());

		for (String selectedId : selectedIds) {
			Node node = nodeWiseKPIValue.get(Pair.of(kpiRequest.getSelecedHierarchyLabel(), selectedId));
			if (null != node) {
				Object obj = node.getValue();

				List<DataCount> dataCounts = obj instanceof List<?> ? (List<DataCount>) obj : null;
				if (CollectionUtils.isNotEmpty(dataCounts)) {

					Pair<String, String> maturityValue = getMaturityValuePair(kpiName, kpiId, dataCounts);
					String aggregateValue = null;
					String maturity = null;
					if (maturityValue != null) {
						aggregateValue = maturityValue.getValue();
						maturity = maturityValue.getKey();
					}
					trendValues
							.add(new DataCount(node.getName(), maturity, aggregateValue, getList(dataCounts, kpiName)));

				}
			}
		}
		return trendValues;
	}

	/**
	 * This method return trend value for simple non filter and circle KPI (like
	 * DORA KPI)
	 *
	 * @param kpiRequest
	 *            kpiRequest
	 * @param kpiElement
	 *            kpiElement
	 * @param nodeWiseKPIValue
	 *            nodeWiseKPIValue
	 * @return trend values
	 */
	public List<DataCount> getAggregateTrendValues(KpiRequest kpiRequest,KpiElement kpiElement,
			Map<Pair<String, String>, Node> nodeWiseKPIValue, KPICode kpiCode) {
		String kpiName = kpiCode.name();
		String kpiId = kpiCode.getKpiId();
		List<DataCount> trendValues = new ArrayList<>();

		Set<String> selectedIds = getSelectedIds(kpiRequest);
		calculateThresholdValue(selectedIds, kpiElement, kpiRequest.getLabel());

		for (String selectedId : selectedIds) {
			Node node = nodeWiseKPIValue.get(Pair.of(kpiRequest.getSelecedHierarchyLabel(), selectedId));
			if (null != node) {
				Object obj = node.getValue();

				List<DataCount> dataCounts = obj instanceof List<?> ? (List<DataCount>) obj : null;
				if (CollectionUtils.isNotEmpty(dataCounts)) {
					List<R> aggValues = dataCounts.stream().filter(val -> val.getValue() != null)
							.map(val -> (R) val.getValue()).collect(Collectors.toList());

					R calculatedAggValue = getCalculatedAggValue(aggValues, kpiId);
					String maturity = calculateMaturity(configHelperService.calculateMaturity().get(kpiId), kpiId,
							String.valueOf(calculatedAggValue));

					String aggregateValue = null;
					if (StringUtils.isNotEmpty(maturity)) {
						aggregateValue = String.valueOf(calculatedAggValue);
					}
					trendValues.add(new DataCount(node.getName(), maturity, aggregateValue,
							getList(dataCounts, kpiName), calculatedAggValue));

				}
			}
		}
		return trendValues;
	}

	/**
	 * Method to calculate the Aggregated value with Cycle kpi dora
	 * 
	 * @param aggValues
	 *            AggValue
	 * @param kpiId
	 *            kpiId
	 * @return calculated Agg value
	 */
	private R getCalculatedAggValue(List<R> aggValues, String kpiId) {
		R calculatedAggValue = null;
		if (!aggValues.isEmpty()) {
			if (aggValues.get(0) instanceof Double) {
				calculatedAggValue = (R) calculateCycleAggregateValueForDouble((List<Double>) aggValues, kpiId);
			} else if (aggValues.get(0) instanceof Long) {
				calculatedAggValue = (R) calculateCycleAggregateValueForLong((List<Long>) aggValues, kpiId);
			}
		}
		return calculatedAggValue;
	}

	private Pair<String, String> getMaturityValuePair(String kpiName, String kpiId, List<DataCount> dataCounts) {
		Pair<String, String> maturityValue = null;
		if (null != configHelperService.calculateMaturity().get(kpiId)) {
			maturityValue = collectValuesForMaturity(dataCounts, kpiName, kpiId);
		}
		return maturityValue;
	}

	/**
	 * This method return trend value for KPIs containing filter or map as value
	 *
	 * @param kpiRequest
	 *            kpiRequest
	 * @param kpiElement
	 * @param nodeWiseKPIValue
	 *            nodeWiseKPIValue
	 * @return map of string and list of trendvalue
	 */
	public Map<String, List<DataCount>> getTrendValuesMap(KpiRequest kpiRequest, KpiElement kpiElement,
			Map<Pair<String, String>, Node> nodeWiseKPIValue, KPICode kpiCode) {
		String kpiName = kpiCode.name();
		String kpiId = kpiCode.getKpiId();
		Map<String, List<DataCount>> trendMap = new HashMap<>();
		Set<String> selectedIds = getSelectedIds(kpiRequest);
		calculateThresholdValue(selectedIds, kpiElement, kpiRequest.getLabel());

		for (String selectedId : selectedIds) {
			Node node = nodeWiseKPIValue.get(Pair.of(kpiRequest.getSelecedHierarchyLabel().toUpperCase(), selectedId));
			if (null != node) {
				Object obj = node.getValue();
				Map<String, List<DataCount>> valueMap = obj instanceof Map<?, ?> ? (Map<String, List<DataCount>>) obj
						: new HashMap<>();
				if (MapUtils.isNotEmpty(valueMap)) {
					valueMap.remove(Constant.DEFAULT);
					valueMap.forEach((key, value) -> {
						List<DataCount> trendValues = new ArrayList<>();

						Pair<String, String> maturityValue = getMaturityValuePair(kpiName, kpiId, value);
						String aggregateValue = null;
						String maturity = null;
						if (maturityValue != null) {
							aggregateValue = maturityValue.getValue();
							maturity = maturityValue.getKey();
						}
						trendValues
								.add(new DataCount(node.getName(), maturity, aggregateValue, getList(value, kpiName)));
						trendMap.computeIfAbsent(key, k -> new ArrayList<>()).addAll(trendValues);

					});
				}
			}
		}
		return commonService.sortTrendValueMap(trendMap);
	}

	/**
	 * This method return trend value for KPIs containing filter or map as value and
	 * circle KPI (like DORA KPI)
	 *
	 * @param kpiRequest
	 *            kpiRequest
	 * @param kpiElement
	 *            kpiElement
	 * @param nodeWiseKPIValue
	 *            nodeWiseKPIValue
	 * @return map of string and list of trendvalue
	 */
	public Map<String, List<DataCount>> getAggregateTrendValuesMap(KpiRequest kpiRequest,KpiElement kpiElement,
			Map<Pair<String, String>, Node> nodeWiseKPIValue, KPICode kpiCode) {
		String kpiName = kpiCode.name();
		String kpiId = kpiCode.getKpiId();
		Map<String, List<DataCount>> trendMap = new HashMap<>();

		Set<String> selectedIds = getSelectedIds(kpiRequest);
		calculateThresholdValue(selectedIds, kpiElement, kpiRequest.getLabel());

		for (String selectedId : selectedIds) {
			Node node = nodeWiseKPIValue.get(Pair.of(kpiRequest.getSelecedHierarchyLabel().toUpperCase(), selectedId));
			if (null != node) {
				Object obj = node.getValue();
				Map<String, List<DataCount>> valueMap = obj instanceof Map<?, ?> ? (Map<String, List<DataCount>>) obj
						: new HashMap<>();
				if (MapUtils.isNotEmpty(valueMap)) {
					valueMap.remove(Constant.DEFAULT);
					valueMap.forEach((key, value) -> {
						List<DataCount> trendValues = new ArrayList<>();

						List<R> aggValues = value.stream().filter(val -> val.getValue() != null)
								.map(val -> (R) val.getValue()).collect(Collectors.toList());
						R calculatedAggValue = getCalculatedAggValue(aggValues, kpiId);
						String aggregateValue = null;
						String maturity = calculateMaturity(configHelperService.calculateMaturity().get(kpiId), kpiId,
								String.valueOf(calculatedAggValue));
						if (StringUtils.isNotEmpty(maturity)) {
							aggregateValue = String.valueOf(calculatedAggValue);
						}
						trendValues.add(new DataCount(node.getName(), maturity, aggregateValue, getList(value, kpiName),
								calculatedAggValue));
						trendMap.computeIfAbsent(key, k -> new ArrayList<>()).addAll(trendValues);

					});
				}
			}
		}
		return commonService.sortTrendValueMap(trendMap);
	}

	/**
	 * Based on data prepared return list in order
	 *
	 * @param value
	 *            value
	 * @param kpiName
	 *
	 *            kpiName
	 * @return list
	 */
	private List<DataCount> getList(List<DataCount> value, String kpiName) {
		if (reverseTrendList.contains(kpiName)) {
			return value.stream().limit(Constant.TREND_LIMIT).collect(Collectors.toList());
		} else {
			return Lists.reverse(value).stream().limit(Constant.TREND_LIMIT).collect(Collectors.toList());
		}
	}

	/**
	 * This method fetch hierarchy id based on selections
	 *
	 * @param kpiRequest
	 *            kpiRequest
	 * @return set of string
	 */
	private Set<String> getSelectedIds(KpiRequest kpiRequest) {
		Set<String> selectedIds = new HashSet<>();

		populateKanbanData(kpiRequest);
		Map<String, AdditionalFilterCategory> addFilterCategory = cacheService.getAdditionalFilterHierarchyLevel();
		Map<String, AdditionalFilterCategory> addFilterCat = addFilterCategory.entrySet().stream()
				.collect(Collectors.toMap(entry -> entry.getKey().toUpperCase(), Map.Entry::getValue));

		if (Constant.SPRINT.equalsIgnoreCase(kpiRequest.getLabel())) {
			populateSelectedIdInSprintSelection(kpiRequest, selectedIds);
			kpiRequest.setSelecedHierarchyLabel(Constant.PROJECT.toUpperCase());
		} else if (MapUtils.isNotEmpty(addFilterCat) && null != addFilterCat.get(kpiRequest.getLabel().toUpperCase())) {
			Map<String, List<String>> kpiRequestSelectedMap = new HashMap<>();
			if (MapUtils.isNotEmpty(kpiRequest.getSelectedMap())) {
				kpiRequestSelectedMap = kpiRequest.getSelectedMap().entrySet().stream()
						.collect(Collectors.toMap(entry -> entry.getKey().toUpperCase(), Map.Entry::getValue));
			}
			if (CollectionUtils.isNotEmpty(kpiRequestSelectedMap.get(Constant.SPRINT.toUpperCase()))) {
				populateSelectedIdInSprintSelection(kpiRequest, selectedIds);
			} else {
				selectedIds.addAll(kpiRequestSelectedMap.get(Constant.PROJECT.toUpperCase()));
			}
			kpiRequest.setSelecedHierarchyLabel(Constant.PROJECT.toUpperCase());
		} else {
			selectedIds = new HashSet<>(Arrays.asList(kpiRequest.getIds()));
			kpiRequest.setSelecedHierarchyLabel(kpiRequest.getLabel());
		}
		return selectedIds;
	}

	/**
	 * This method populate id in kanban scenario
	 *
	 * @param kpiRequest
	 *            kpiRequest
	 */
	private void populateKanbanData(KpiRequest kpiRequest) {
		String id = kpiRequest.getIds()[0];
		if (NumberUtils.isCreatable(id)) {
			Map<String, List<String>> selectedMap = kpiRequest.getSelectedMap();
			List<HierarchyLevel> hiearachyLevel = cacheService.getFullKanbanHierarchyLevel();
			List<String> kanbanHierarchyOrder = Lists.reverse(hiearachyLevel).stream()
					.map(HierarchyLevel::getHierarchyLevelId).collect(Collectors.toList());

			for (String hierarchyLevel : kanbanHierarchyOrder) {
				if (CollectionUtils.isNotEmpty(selectedMap.get(hierarchyLevel))) {
					kpiRequest.setIds(selectedMap.get(hierarchyLevel).toArray(new String[0]));
					break;
				}
			}
		}
	}

	/**
	 * This method populate id on sprint selection
	 *
	 * @param kpiRequest
	 *            kpiRequest
	 * @param selectedIds
	 *            selectedIds
	 */
	private void populateSelectedIdInSprintSelection(KpiRequest kpiRequest, Set<String> selectedIds) {
		for (String selectedId : kpiRequest.getSelectedMap().get(Constant.SPRINT.toLowerCase())) {
			String[] sprintNameWithProject = selectedId.split("_", 2);
			if (sprintNameWithProject.length > 1) {
				selectedIds.add(sprintNameWithProject[1]);
			}
		}
	}

	/**
	 * This method return maturity of KPI
	 *
	 * @param dataCounts
	 *            dataCounts
	 * @param kpiName
	 *            kpiName
	 * @param kpiId
	 *            kpiId
	 * @return maturity value
	 */
	private Pair<String, String> collectValuesForMaturity(List<DataCount> dataCounts, String kpiName, String kpiId) {
		List<R> values = null;
		String maturityValue = null;
		String aggregateValue = null;
		List<R> valueMap = dataCounts.stream().filter(val -> val.getValue() instanceof HashMap<?, ?>)
				.map(val -> (R) val.getValue()).collect(Collectors.toList());
		if (CollectionUtils.isNotEmpty(valueMap)) {
			S aggValue = calculateMapKpiMaturity(valueMap, kpiName);
			maturityValue = calculateMaturity(configHelperService.calculateMaturity().get(kpiId), kpiId,
					String.valueOf(aggValue));
			aggregateValue = String.valueOf(aggValue);
		}
		if (CollectionUtils.isEmpty(valueMap)) {
			values = dataCounts.stream().filter(val -> null != val.getLineValue()).map(val -> (R) val.getLineValue())
					.collect(Collectors.toList());
			if (CollectionUtils.isEmpty(values)) {
				values = dataCounts.stream().map(val -> (R) val.getValue()).collect(Collectors.toList());
			}
			R aggValue = calculateAggValue(kpiName, dataCounts, values, kpiId);
			maturityValue = calculateMaturity(configHelperService.calculateMaturity().get(kpiId), kpiId,
					String.valueOf(aggValue));
			aggregateValue = String.valueOf(aggValue);
		}
		return Pair.of(maturityValue, aggregateValue);
	}

	/**
	 * Generic method to override for aggregation
	 *
	 * @param values
	 *            values
	 * @param kpiName
	 *            kpiName
	 * @return aggregated value
	 */
	public R calculateKpiValue(List<R> values, String kpiName) { // NOSONAR
		return null;
	}

	/**
	 * Generic method to override for aggregation in map scenario
	 *
	 * @param values
	 *            values
	 * @param kpiName
	 *            kpiName
	 * @return aggregated value
	 */
	public S calculateMapKpiMaturity(List<R> values, String kpiName) { // NOSONAR
		return null;
	}

	/**
	 * This method implement aggreagation based on kpi
	 *
	 * @param kpiName
	 *            kpiName
	 * @param value
	 *            value
	 * @param values
	 *            values
	 * @return aggregated value
	 */
	private R calculateAggValue(String kpiName, List<DataCount> value, List<R> values, String kpiId) {
		R aggValue;
		if (kpiName.equals(KPICode.REGRESSION_AUTOMATION_COVERAGE.name())) {
			aggValue = (R) value.get(value.size() - 1).getValue();
		} else if (cumulativeTrend.contains(kpiName)) {
			aggValue = (R) value.get(0).getValue();
		} else if (kpiName.equals(KPICode.LEAD_TIME.name())) {
			aggValue = (R) value.stream().filter(dataCount -> dataCount.getsSprintID().equalsIgnoreCase("< 3 Months"))
					.findFirst().get().getValue();
		} else {
			aggValue = calculateKpiValue(values, kpiId);
			if (kpiName.equals(KPICode.DEPLOYMENT_FREQUENCY.name()) && CollectionUtils.isNotEmpty(values)) {
				aggValue = (R) String.valueOf(Integer.parseInt(String.valueOf(aggValue)) / values.size());
			}
		}
		return aggValue;
	}

	/**
	 * This method aggregate double.
	 *
	 * @param valueList
	 *            valueList
	 * @param kpiId
	 *            kpiId
	 * @return result
	 */
	public Double calculateKpiValueForDouble(List<Double> valueList, String kpiId) {
		Double calculatedValue = 0.0;
		if (Constant.PERCENTILE.equalsIgnoreCase(configHelperService.calculateCriteria().get(kpiId))) {
			if (null == customApiConfig.getPercentileValue()) {
				calculatedValue = AggregationUtils.percentiles(valueList, 90.0D);
			} else {
				calculatedValue = AggregationUtils.percentiles(valueList, customApiConfig.getPercentileValue());
			}
		} else if (Constant.MEDIAN.equalsIgnoreCase(configHelperService.calculateCriteria().get(kpiId))) {
			calculatedValue = AggregationUtils.median(valueList);
		} else if (Constant.AVERAGE.equalsIgnoreCase(configHelperService.calculateCriteria().get(kpiId))) {
			calculatedValue = AggregationUtils.average(valueList);
		} else if (Constant.SUM.equalsIgnoreCase(configHelperService.calculateCriteria().get(kpiId))) {
			calculatedValue = valueList.stream().mapToDouble(i -> i).sum();
		}
		return round(calculatedValue);
	}

	/**
	 * This method aggregate long.
	 *
	 * @param valueList
	 *            valueList
	 * @param kpiId
	 *            kpiId
	 * @return result
	 */
	public Long calculateKpiValueForLong(List<Long> valueList, String kpiId) {
		if (Constant.PERCENTILE.equalsIgnoreCase(configHelperService.calculateCriteria().get(kpiId))) {
			if (null == customApiConfig.getPercentileValue()) {
				return AggregationUtils.percentilesLong(valueList, 90d);
			} else {
				Double percentile = customApiConfig.getPercentileValue();
				return AggregationUtils.percentilesLong(valueList, percentile);
			}
		} else if (Constant.MEDIAN.equalsIgnoreCase(configHelperService.calculateCriteria().get(kpiId))) {
			return AggregationUtils.getMedianForLong(valueList);
		} else if (Constant.SUM.equalsIgnoreCase(configHelperService.calculateCriteria().get(kpiId))) {
			return AggregationUtils.sumLong(valueList);
		} else if (Constant.AVERAGE.equalsIgnoreCase(configHelperService.calculateCriteria().get(kpiId))) {
			return AggregationUtils.averageLong(valueList);
		}
		return AggregationUtils.percentilesLong(valueList, 90d);
	}

	/**
	 * This method aggregate double.
	 *
	 * @param values
	 *            values
	 * @param kpiName
	 *            kpiName
	 * @return result
	 */
	public Map<String, Long> calculateKpiValueForMap(List<Map<String, Long>> values, String kpiName) {
		String aggregationCriteria = configHelperService.calculateCriteria().get(kpiName);
		Map<String, Long> resultMap = new HashMap<>();
		Map<String, List<Long>> aggMap = values.stream().flatMap(m -> m.entrySet().stream()).collect(
				Collectors.groupingBy(Map.Entry::getKey, Collectors.mapping(Map.Entry::getValue, Collectors.toList())));

		aggMap.forEach((key, value) -> {
			if (Constant.PERCENTILE.equalsIgnoreCase(aggregationCriteria)) {
				if (null == customApiConfig.getPercentileValue()) {
					resultMap.put(key, AggregationUtils.percentilesForLongValues(value, 90.0D));
				} else {
					resultMap.put(key,
							AggregationUtils.percentilesForLongValues(value, customApiConfig.getPercentileValue()));
				}
			} else if (Constant.MEDIAN.equalsIgnoreCase(aggregationCriteria)) {
				resultMap.put(key, AggregationUtils.getMedianForLong(value));
			} else if (Constant.AVERAGE.equalsIgnoreCase(aggregationCriteria)) {
				resultMap.put(key, AggregationUtils.averageLong(value));
			} else if (Constant.SUM.equalsIgnoreCase(aggregationCriteria)) {
				resultMap.put(key, value.stream().mapToLong(i -> i).sum());
			}
		});

		resultMap.remove(Constant.DEFAULT);
		return resultMap.entrySet().stream().sorted((i1, i2) -> i2.getValue().compareTo(i1.getValue()))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
	}

	/**
	 * This method aggregate double.
	 *
	 * @param values
	 *            values
	 * @param kpiId
	 *            kpiId
	 * @return result
	 */
	public Map<String, Object> calculateKpiValueForIntMap(List<Map<String, Object>> values, String kpiId) {
		String aggregationCriteria = configHelperService.calculateCriteria().get(kpiId);
		Map<String, Object> resultMap = new HashMap<>();
		Map<String, List<Object>> aggMap = values.stream().flatMap(m -> m.entrySet().stream()).collect(
				Collectors.groupingBy(Map.Entry::getKey, Collectors.mapping(Map.Entry::getValue, Collectors.toList())));

		aggMap.forEach((key, objectList) -> {
			List<Integer> value = objectList.stream().map(Integer.class::cast).collect(Collectors.toList());
			if (Constant.PERCENTILE.equalsIgnoreCase(aggregationCriteria)) {
				if (null == customApiConfig.getPercentileValue()) {
					resultMap.put(key, AggregationUtils.percentilesInteger(value, 90.0D));
				} else {
					resultMap.put(key,
							AggregationUtils.percentilesInteger(value, customApiConfig.getPercentileValue()));
				}
			} else if (Constant.MEDIAN.equalsIgnoreCase(aggregationCriteria)) {
				resultMap.put(key, AggregationUtils.getMedianForInteger(value));
			} else if (Constant.AVERAGE.equalsIgnoreCase(aggregationCriteria)) {
				resultMap.put(key, AggregationUtils.averageInteger(value));
			} else if (Constant.SUM.equalsIgnoreCase(aggregationCriteria)) {
				resultMap.put(key, value.stream().mapToInt(i -> i).sum());
			}
		});

		resultMap.remove(Constant.DEFAULT);
		return resultMap.entrySet().stream()
				.sorted((i1, i2) -> ((Integer) i2.getValue()).compareTo((Integer) i1.getValue()))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
	}

	/**
	 * This method return maturity level
	 *
	 * @param maturityRangeList
	 *            maturityRangeList
	 * @param kpiId
	 *            kpiId
	 * @param kpiCeilValue
	 *            kpiCeilValue
	 * @return maturity level
	 */
	public String calculateMaturity(List<String> maturityRangeList, String kpiId, String kpiCeilValue) {
		return commonService.getMaturityLevel(maturityRangeList, kpiId, String.valueOf(kpiCeilValue));
	}

	/**
	 * Method to round value
	 *
	 * @param value
	 *            value
	 * @return rounded value
	 */
	public Double round(Double value) {
		Double val = (Double) ObjectUtils.defaultIfNull(value, 0.0D);
		BigDecimal bd = BigDecimal.valueOf(val).setScale(2, RoundingMode.HALF_UP);
		return bd.doubleValue();
	}

	/**
	 * Method to calculate the Agg value of List of double for cycle kpi(dora)
	 * 
	 * @param valueList
	 *            List of values
	 * @param kpiId
	 *            kpiId
	 * @return Agg value
	 */
	public Double calculateCycleAggregateValueForDouble(List<Double> valueList, String kpiId) {
		Double calculatedValue = 0.0;
		if (Constant.PERCENTILE.equalsIgnoreCase(configHelperService.calculateCriteriaForCircleKPI().get(kpiId))) {
			if (null == customApiConfig.getPercentileValue()) {
				calculatedValue = AggregationUtils.percentiles(valueList, 90.0D);
			} else {
				calculatedValue = AggregationUtils.percentiles(valueList, customApiConfig.getPercentileValue());
			}
		} else if (Constant.MEDIAN.equalsIgnoreCase(configHelperService.calculateCriteriaForCircleKPI().get(kpiId))) {
			calculatedValue = AggregationUtils.median(valueList);
		} else if (Constant.AVERAGE.equalsIgnoreCase(configHelperService.calculateCriteriaForCircleKPI().get(kpiId))) {
			calculatedValue = AggregationUtils.average(valueList);
		} else if (Constant.SUM.equalsIgnoreCase(configHelperService.calculateCriteriaForCircleKPI().get(kpiId))) {
			calculatedValue = valueList.stream().mapToDouble(i -> i).sum();
		}
		return round(calculatedValue);
	}

	/**
	 * Method to calculate the Agg value of List of Long for cycle kpi(dora)
	 * 
	 * @param valueList
	 *            List of values
	 * @param kpiId
	 *            kpiId
	 * @return Agg value
	 */
	public Long calculateCycleAggregateValueForLong(List<Long> valueList, String kpiId) {
		if (Constant.PERCENTILE.equalsIgnoreCase(configHelperService.calculateCriteriaForCircleKPI().get(kpiId))) {
			if (null == customApiConfig.getPercentileValue()) {
				return AggregationUtils.percentilesLong(valueList, 90d);
			} else {
				Double percentile = customApiConfig.getPercentileValue();
				return AggregationUtils.percentilesLong(valueList, percentile);
			}
		} else if (Constant.MEDIAN.equalsIgnoreCase(configHelperService.calculateCriteriaForCircleKPI().get(kpiId))) {
			return AggregationUtils.getMedianForLong(valueList);
		} else if (Constant.AVERAGE.equalsIgnoreCase(configHelperService.calculateCriteriaForCircleKPI().get(kpiId))) {
			return AggregationUtils.averageLong(valueList);
		} else if (Constant.SUM.equalsIgnoreCase(configHelperService.calculateCriteriaForCircleKPI().get(kpiId))) {
			return AggregationUtils.sumLong(valueList);
		}
		return AggregationUtils.percentilesLong(valueList, 90d);
	}

	/**
	 * on selection of single project the fieldmapping threshold value will be
	 * selected
	 *
	 * @param selectIds
	 *            projectIds
	 * @param kpiElement
	 *            kpiElement
	 * @param labelName
	 *            labelName
	 */
	public void calculateThresholdValue(Set<String> selectIds, KpiElement kpiElement, String labelName) {
		if (selectIds.size() == 1 && (labelName.equalsIgnoreCase(CommonConstant.HIERARCHY_LEVEL_ID_PROJECT)
				|| labelName.equalsIgnoreCase(CommonConstant.HIERARCHY_LEVEL_ID_SPRINT))) {
			String basicProjectConfigId = selectIds.iterator().next().split(Constant.UNDERSCORE)[1];
			FieldMapping fieldMapping = configHelperService.getFieldMappingMap()
					.get(new ObjectId(basicProjectConfigId));
			if (fieldMapping != null) {
				kpiElement.setThresholdValue(calculateThresholdValue(fieldMapping));
			}
		}
	}

	/**
	 *
	 * @param fieldMapping
	 *            fieldMapping
	 * @return
	 */
	public Double calculateThresholdValue(FieldMapping fieldMapping) {
		return null;
	}

	/**
	 * 
	 * @param fieldValue
	 *            fieldmapping thresholdvalue
	 * @param kpiId
	 *            KPICODE kpiId
	 * @return
	 */
	public Double calculateThresholdValue(String fieldValue, String kpiId) { // NOSONAR
		Double thresholdValue;
		if (StringUtils.isEmpty(fieldValue)) {
			List<KpiMaster> masterList = (List<KpiMaster>) configHelperService.loadKpiMaster();
			thresholdValue = masterList.stream().filter(kpi -> kpi.getKpiId().equalsIgnoreCase(kpiId))
					.mapToDouble(kpi -> kpi.getThresholdValue() != null ? kpi.getThresholdValue() : 0.0).sum();
		} else {
			thresholdValue = Double.valueOf(fieldValue);
		}
		return thresholdValue;
	}

}
