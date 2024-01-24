/*******************************************************************************
 * Copyright 2014 CapitalOne, LLC.
 * Further development Copyright 2022 Sapient Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package com.publicissapient.kpidashboard.apis.util;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.publicissapient.kpidashboard.common.model.sonar.SonarMetric;

import lombok.extern.slf4j.Slf4j;

/**
 * The type Aggregation utils.
 */
@Slf4j
@SuppressWarnings("PMD.GodClass")
public final class AggregationUtils {

	private AggregationUtils() {
	}

	/**
	 * Calculates median for input list
	 *
	 * @param numbers
	 *            the numbers
	 * @return median of input.
	 */
	public static Double median(List<Double> numbers) {
		Double median = null;
		if (CollectionUtils.isNotEmpty(numbers)) {
			int size = numbers.size();
			Collections.sort(numbers);
			int middle = size / 2;
			if (size % 2 == 1) {
				median = numbers.get(middle);
			} else {
				median = (numbers.get(middle - 1) + numbers.get(middle)) / 2.0;
			}
		}
		return median;
	}

	/**
	 * Calculates median for input list
	 *
	 * @param numbers
	 *            the numbers
	 * @return median of input.
	 */
	public static Long getMedianForLong(List<Long> numbers) {
		Long median = null;
		if (CollectionUtils.isNotEmpty(numbers)) {
			int size = numbers.size();
			Collections.sort(numbers);
			int middle = size / 2;
			if (size % 2 == 1) {
				median = numbers.get(middle);
			} else {
				median = Math.round((double) (numbers.get(middle - 1) + numbers.get(middle)) / 2);
			}
		}
		return median;
	}

	/**
	 * Calculates median for input list
	 *
	 * @param numbers
	 *            the numbers
	 * @return median of input.
	 */
	public static Integer getMedianForInteger(List<Integer> numbers) {
		Integer median = null;
		if (CollectionUtils.isNotEmpty(numbers)) {
			int size = numbers.size();
			Collections.sort(numbers);
			int middle = size / 2;
			if (size % 2 == 1) {
				median = numbers.get(middle);
			} else {
				median = (int) Math.round((double) (numbers.get(middle - 1) + numbers.get(middle)) / 2);
			}
		}
		return median;
	}

	/**
	 * Calculates average for input list
	 *
	 * @param numbers
	 *            the numbers
	 * @return average of input.
	 */
	public static Double average(List<Double> numbers) {
		Double sum = 0.0d;
		Double average = null;
		DecimalFormat decformat = new DecimalFormat("#0.00");
		if (CollectionUtils.isNotEmpty(numbers)) {
			Iterator<Double> itr = numbers.iterator();
			while (itr.hasNext()) {
				Double number = Double.parseDouble(itr.next() + StringUtils.EMPTY);
				sum += number;
			}
			String formattedVal = decformat.format(sum / numbers.size());
			average = Double.parseDouble(formattedVal);
		}
		return average;
	}

	/**
	 * Calculates average for input list
	 *
	 * @param numbers
	 *            the numbers
	 * @return average of input.
	 */
	public static Long averageLong(List<Long> numbers) {
		Long sum = 0l;
		Double average = null;
		if (CollectionUtils.isNotEmpty(numbers)) {
			for (Long element : numbers) {
				sum += element;
			}
			average = (Double.valueOf(sum) / numbers.size());
		}
		return average == null ? null : Math.round(average);
	}

	/**
	 * Calculates average for input list
	 *
	 * @param numbers
	 *            the numbers
	 * @return average of input.
	 */
	public static Integer averageInteger(List<Integer> numbers) {
		Integer sum = 0;
		Double average = null;
		if (CollectionUtils.isNotEmpty(numbers)) {
			for (Integer element : numbers) {
				sum += element;
			}
			average = (Double.valueOf(sum) / numbers.size());
		}
		return average == null ? null : (int) Math.round(average);
	}

	/**
	 * Calculates weighted average for inputs
	 *
	 * @param numbers
	 *            the numbers
	 * @param wgt
	 *            the wgt
	 * @return weighted average.
	 */
	public static Double weightedAverage(List<Double> numbers, List<Double> wgt) {
		Double wtAverage = null;
		Double weightSum = 0.0d;
		if (CollectionUtils.isNotEmpty(numbers) && CollectionUtils.isNotEmpty(wgt)) {
			double weighted = 0d;
			for (int i = 0; i < numbers.size(); i++) {
				weighted += numbers.get(i) * wgt.get(i);

				if (i < wgt.size()) {
					weightSum = weightSum + wgt.get(i);
				}
			}
			if (weightSum == 0) {
				wtAverage = null;
			} else {
				wtAverage = weighted / weightSum;
			}

		}
		return wtAverage;
	}

	/**
	 * Calculates percentiles for inputs
	 *
	 * @param numbers
	 *            the numbers
	 * @param percentiles
	 *            the percentiles
	 * @return percentiles. double
	 */
	public static Double percentiles(List<Double> numbers, Double percentiles) {
		Double percentileValue = null;

		if (CollectionUtils.isNotEmpty(numbers) && (null != percentiles)) {
			Collections.sort(numbers);
			int index = (int) Math.round((percentiles / 100) * numbers.size());
			if (index == 0) {
				percentileValue = numbers.get(index);
			} else {
				percentileValue = numbers.get(index - 1);
			}
		}
		return percentileValue;
	}

	/**
	 * Calculates percentiles for inputs
	 *
	 * @param numbers
	 *            the numbers
	 * @param percentiles
	 *            the percentiles
	 * @return percentiles. long
	 */
	public static Long percentilesLong(List<Long> numbers, Double percentiles) {
		Long values = null;

		if (CollectionUtils.isNotEmpty(numbers) && (null != percentiles)) {
			Collections.sort(numbers);
			int index = (int) Math.round((percentiles / 100) * numbers.size());
			if (index == 0) {
				values = numbers.get(index);
			} else {
				values = numbers.get(index - 1);
			}

		}
		return values;
	}

	/**
	 * Calculates percentiles for inputs
	 *
	 * @param numbers
	 *            the numbers
	 * @param percentiles
	 *            the percentiles
	 * @return percentiles. integer
	 */
	public static Integer percentilesInteger(List<Integer> numbers, Double percentiles) {
		Integer values = null;

		if (CollectionUtils.isNotEmpty(numbers) && (null != percentiles)) {
			Collections.sort(numbers);
			int index = (int) Math.round((percentiles / 100) * numbers.size());
			values = numbers.get(index - 1);
		}
		return values;
	}

	/**
	 * Calculates percentiles for inputs
	 *
	 * @param numbers
	 *            the numbers
	 * @param percentiles
	 *            the percentiles
	 * @return percentiles. long
	 */
	public static Long percentilesForLongValues(List<Long> numbers, Double percentiles) {
		Long values = null;

		Iterator<Long> itr = numbers.iterator();
		while (itr.hasNext()) {
			if (itr.next() == null) {
				itr.remove();
			}
		}

		if (CollectionUtils.isNotEmpty(numbers) && (null != percentiles)) {
			Collections.sort(numbers);
			int index = (int) Math.round((percentiles / 100) * numbers.size());
			values = numbers.get(index - 1);
		}
		return values;
	}

	/**
	 * Calculates Sonar percentiles in double.
	 *
	 * @param numbers
	 *            the numbers
	 * @param percentiles
	 *            the percentiles
	 * @return the double
	 */
	public static Double sonarPercentiles(List<Double> numbers, Double percentiles) {
		Double values = null;

		if (CollectionUtils.isNotEmpty(numbers) && (null != percentiles)) {
			Collections.reverse(numbers);
			int index = (int) Math.round((percentiles / 100) * numbers.size());
			values = numbers.get(index - 1);
		}
		return values;
	}

	/**
	 * Calculates weighted percentiles for inputs
	 *
	 * @param numbers
	 *            the numbers
	 * @param wgt
	 *            the wgt
	 * @param percentiles
	 *            the percentiles
	 * @return weighted percentiles.
	 */
	public static Double weightedPercentiles(List<Double> numbers, List<Double> wgt, Double percentiles) {

		Double values = null;
		if (CollectionUtils.isNotEmpty(numbers) && CollectionUtils.isNotEmpty(wgt) && null != percentiles) {
			List<Double> wgtNumber = new ArrayList<>();
			HashMap<Double, Double> map = new HashMap<>();
			for (int i = 0; i < numbers.size(); i++) {
				wgtNumber.add(numbers.get(i) * wgt.get(i));
				map.put(wgtNumber.get(i), numbers.get(i));
			}

			Collections.sort(wgtNumber);
			int index = (int) Math.round((percentiles / 100) * wgtNumber.size());
			values = map.get(wgtNumber.get(index - 1));
		}
		return values;
	}

	/**
	 * Gets weighted sum.
	 *
	 * @param weightList
	 *            the weight list
	 * @return the weighted sum
	 */
	public static Double getWeightedSum(List<Double> weightList) {

		Double weightSum = 0d;

		for (Double weight : weightList) {
			weightSum = weightSum + weight;
		}

		return weightSum;
	}

	/**
	 * Aggregates list of object for specific KPI's. The underlying aggregation used
	 * is 90 percentile. The tight coupling of aggregation calculation has to be
	 * freed if needed.
	 *
	 * @param aggregatedValueList
	 *            List of objects
	 * @return returns object
	 */
	@SuppressWarnings("unchecked")
	public static List<SonarMetric> aggregateForCodeMetrics(List<List<SonarMetric>> aggregatedValueList) {
		log.info("[AGGREGATE-FOR-OBJECT].Aggregating objects with 90 percentile: {}", aggregatedValueList);
		List<SonarMetric> list = aggregatedValueList.stream().flatMap(Collection::stream).filter(
				value -> (value.getMetricValue() != null && !(value.getMetricName().equals("quality_gate_details")
						|| value.getMetricName().equals("alert_status"))))
				.collect(Collectors.toList());

		list.forEach(value -> {
			if (value.getMetricValue() instanceof String) {
				value.setMetricValue(Double.parseDouble((String) value.getMetricValue()));
			}
		});

		List<SonarMetric> list3 = list.stream()
				.collect(Collectors.groupingBy(SonarMetric::getMetricName,
						Collectors.mapping(SonarMetric::getMetricValue, Collectors.toList())))
				.entrySet().stream().map(m -> new SonarMetric(m.getKey(), m.getValue())).collect(Collectors.toList());
		list3.forEach(l -> l.setMetricValue(AggregationUtils.percentiles((List<Double>) l.getMetricValue(), 90.0d)));
		return list3;
	}

	/**
	 * Calculates sum for input list
	 *
	 * @param numbers
	 *            the numbers
	 * @return sum of input.
	 */
	public static Double sum(List<Double> numbers) {
		Double sum = null;
		if (CollectionUtils.isNotEmpty(numbers)) {
			sum = numbers.stream().mapToDouble(Double::doubleValue).sum();
		}
		return sum;
	}

	/**
	 * Sum list of numbers as long.
	 *
	 * @param numbers
	 *            the numbers
	 * @return the long
	 */
	public static Long sumLong(List<Long> numbers) {
		Long sum = null;
		if (CollectionUtils.isNotEmpty(numbers)) {
			sum = numbers.stream().mapToLong(Long::longValue).sum();
		}
		return sum;
	}

}