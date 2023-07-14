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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.publicissapient.kpidashboard.common.model.sonar.SonarMetric;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { AggregationUtils.class })
public class AggregationUtilsTests {

	@Test
	public void testMedian() {
		List<Double> measure = Arrays.asList(20d, 2d, 3d, 3d, 4d, 2d, 8d, 3d);
		assertNotNull(AggregationUtils.median(measure));
	}

	@Test
	public void testMediancondt() {
		List<Double> measure = Arrays.asList(2d);
		assertEquals(2d, AggregationUtils.median(measure), 0d);
	}

	@Test
	public void testMedianNull() {
		assertNull(AggregationUtils.median(null));
	}

	@Test
	public void testAverage() {
		List<Double> measure = Arrays.asList(20d, 2d, 3d, 3d, 4d, 2d, 8d, 3d);
		assertNotNull(AggregationUtils.average(measure));
	}

	@Test
	public void testAverageNull() {
		assertNull(AggregationUtils.average(null));

	}

	@Test
	public void testWeightedAverage() {
		List<Double> measure = Arrays.asList(20d, 2d, 3d, 3d, 4d, 2d, 8d, 3d);
		List<Double> wgt = Arrays.asList(0.01, 0.19, 0.50, 0.05, 0.05, 0.10, 0.05, 0.05);
		assertNotNull(AggregationUtils.weightedAverage(measure, wgt));
	}

	@Test
	public void testWeightedAverageNull() {
		assertNull(AggregationUtils.weightedAverage(null, null));

	}

	@Test
	public void testWeightedAveragenumEmpty() {
		List<Double> wgt = new ArrayList<>();
		List<Double> measure = Arrays.asList(0.01, 0.19, 0.50, 0.05, 0.05, 0.10, 0.05, 0.05);
		AggregationUtils.weightedAverage(measure, wgt);

	}

	@Test
	public void testPercentiles() {
		List<Double> measure = Arrays.asList(20d, 2d, 3d, 3d, 4d, 2d, 8d, 3d, 7d, 5d);
		assertEquals(8d, AggregationUtils.percentiles(measure, 90d), 0d);
	}

	@Test
	public void testPercentilesNull() {
		assertNull(AggregationUtils.percentiles(null, 90d));

	}

	@Test
	public void testPercentileEmptyList() {
		List<Double> measure = new ArrayList<>();
		assertNull("Null returned as no input is passed", AggregationUtils.percentiles(measure, 90d));
	}

	@Test
	public void testPercentileSingleElement() {
		List<Double> measure = Arrays.asList(20d);
		assertEquals(20d, AggregationUtils.percentiles(measure, 90d), 0d);
	}

	@Test
	public void testPercentileSingleindex() {
		List<Double> measure = Arrays.asList(2d);
		AggregationUtils.percentiles(measure, 10d);
	}

	@Test
	public void testPercentilenull() {
		List<Double> measure = Arrays.asList(2d);
		AggregationUtils.percentiles(measure, null);
	}

	@Test
	public void testWeightedPercentiles() {
		List<Double> measure = Arrays.asList(20d, 2d, 3d, 3d, 4d, 2d, 8d, 3d);
		List<Double> wgt = Arrays.asList(0.01, 0.19, 0.50, 0.05, 0.05, 0.10, 0.05, 0.05);
		assertEquals(8d, AggregationUtils.weightedPercentiles(measure, wgt, 90d), 0d);
	}

	@Test
	public void testWeightedPercentilesNull() {
		assertNull(AggregationUtils.weightedPercentiles(null, null, 90d));
	}

	@Test
	public void weightedPercentilesnull() {
		List<Double> measure = Arrays.asList(2d);
		List<Double> wgt = Arrays.asList(2d);
		AggregationUtils.weightedPercentiles(measure, wgt, null);
	}

	@Test
	public void weightedPercentilesEmpty() {
		List<Double> measure = new ArrayList<>();
		List<Double> wgt = Arrays.asList(2d);
		AggregationUtils.weightedPercentiles(measure, wgt, 90d);
	}

	@Test
	public void weightedPercentilesEmpty1() {
		List<Double> measure = Arrays.asList(2d);
		List<Double> wgt = new ArrayList<>();
		AggregationUtils.weightedPercentiles(measure, wgt, 90d);
	}

	@Test
	public void testGetMedianForLongForEvenLength() {
		List<Long> measure = Arrays.asList(20L, 2L, 3L, 3L, 4L, 2L, 8L, 3L);
		assertEquals(3, AggregationUtils.getMedianForLong(measure), 0L);
	}

	@Test
	public void testGetMedianForLongForOddLength() {
		List<Long> measure = Arrays.asList(20L, 2L, 3L, 4L, 2L, 5L, 8L);
		assertEquals(4, AggregationUtils.getMedianForLong(measure), 0L);
	}

	@Test
	public void testGetMedianForLongForEmptyList() {
		List<Long> measure = new ArrayList<>();
		assertNull(AggregationUtils.getMedianForLong(measure));
	}

	@Test
	public void averageLong() {
		List<Long> measure = new ArrayList<>();
		measure.add(1234L);
		AggregationUtils.averageLong(measure);
	}

	@Test
	public void averageLongNull() {
		List<Long> measure = new ArrayList<>();
		assertNull(AggregationUtils.averageLong(measure));
	}

	@Test
	public void percentilesLong() {
		List<Long> measure = new ArrayList<>();
		Double percentiles = 20d;
		measure.add(1234L);
		Collections.sort(measure);
		AggregationUtils.percentilesLong(measure, percentiles);
	}

	@Test
	public void percentilesLongEmpty() {
		List<Long> measure = new ArrayList<>();
		Double percentiles = 0d;
		assertNull(AggregationUtils.percentilesLong(measure, percentiles));
	}

	@Test
	public void testPercentilesLongNull() {
		assertNull(AggregationUtils.percentilesLong(null, 90d));

	}

	@Test
	public void testPercentileLongEmptyList() {
		List<Long> measure = new ArrayList<>();
		assertNull("Null returned as no input is passed", AggregationUtils.percentilesLong(measure, 90d));
	}

	@Test
	public void testPercentileLongSingleElement() {
		List<Long> measure = Arrays.asList(20l);
		assertEquals(20d, AggregationUtils.percentilesLong(measure, 90d), 0d);
	}

	@Test
	public void testPercentileLongSingleindex() {
		List<Long> measure = Arrays.asList(2l);
		AggregationUtils.percentilesLong(measure, 10d);
	}

	@Test
	public void testPercentileLongnull() {
		List<Long> measure = Arrays.asList(2l);
		AggregationUtils.percentilesLong(measure, null);
	}

	@Test
	public void percentilesInteger() {
		List<Integer> measure = new ArrayList<>();
		measure.add(12);
		measure.add(12);
		measure.add(12);
		measure.add(12);
		measure.add(12);
		Double percentiles = 20d;
		Collections.sort(measure);
		AggregationUtils.percentilesInteger(measure, percentiles);
	}

	@Test
	public void testPercentilesIntegerNull() {
		assertNull(AggregationUtils.percentilesInteger(null, 90d));

	}

	@Test
	public void percentilesIntegerNull() {
		List<Integer> measure = new ArrayList<>();
		Double percentiles = 0d;
		assertNull(AggregationUtils.percentilesInteger(measure, percentiles));
	}

	@Test
	public void testPercentileIntegerEmptyList() {
		List<Integer> measure = new ArrayList<>();
		AggregationUtils.percentilesInteger(measure, 90d);
	}

	@Test
	public void percentilesIntegerEmptyList() {
		List<Integer> measure = new ArrayList<>();
		AggregationUtils.percentilesInteger(measure, null);
	}

	@Test
	public void percentilesIntegerEmptyList1() {
		List<Integer> measure = Arrays.asList(2);
		AggregationUtils.percentilesInteger(measure, null);
	}

	@Test
	public void percentilesIntegerListNull() {
		List<Integer> measure = new ArrayList<>(2);
		AggregationUtils.percentilesInteger(measure, null);
	}

	@Test
	public void percentilesForLongValues() {
		List<Long> measure = new ArrayList<>();
		measure.add(12l);
		measure.add(12l);
		measure.add(12l);
		measure.add(12l);
		measure.add(12l);
		Double percentiles = 20d;
		Collections.sort(measure);
		AggregationUtils.percentilesForLongValues(measure, percentiles);
	}

	@Test
	public void percentilesForLongValuesNull() {
		List<Long> measure = new ArrayList<>();
		Double percentiles = 0d;
		assertNull(AggregationUtils.percentilesForLongValues(measure, percentiles));
	}

	@Test
	public void percentilesIntegerEmptyLongList1() {
		List<Long> measure = Arrays.asList(2l);
		AggregationUtils.percentilesForLongValues(measure, null);

	}

	@Test
	public void sonarPercentiles() {
		List<Double> measure = new ArrayList<>();
		measure.add(12d);
		measure.add(12d);
		measure.add(12d);
		measure.add(12d);
		measure.add(12d);
		Double percentiles = 20d;
		Collections.sort(measure);
		AggregationUtils.sonarPercentiles(measure, percentiles);
	}

	@Test
	public void sonarPercentilesNull() {
		List<Double> measure = new ArrayList<>();
		Double percentiles = null;
		assertNull(AggregationUtils.sonarPercentiles(measure, percentiles));
	}

	@Test
	public void sonarPercentilesNull2() {
		List<Double> measure = Arrays.asList(2d);
		AggregationUtils.sonarPercentiles(measure, null);
	}

	@Test
	public void sum() {
		List<Double> measure = new ArrayList<>();
		measure.add(12d);
		Collections.sort(measure);
		AggregationUtils.sum(measure);
	}

	@Test
	public void sumNull() {
		List<Double> measure = new ArrayList<>();
		assertNull(AggregationUtils.sum(measure));
	}

	@Test
	public void sumLong() {
		List<Long> measure = new ArrayList<>();
		measure.add(12l);
		Collections.sort(measure);
		AggregationUtils.sumLong(measure);
	}

	@Test
	public void sumLongNull() {
		List<Long> measure = new ArrayList<>();
		assertNull(AggregationUtils.sumLong(measure));
	}

	@Test
	public void getWeightedSum() {
		List<Double> measure = new ArrayList<>();
		measure.add(12d);
		Collections.sort(measure);
		AggregationUtils.getWeightedSum(measure);
	}

	@Test
	public void aggregateForCodeMetrics() {
		List<List<SonarMetric>> aggregatedValueList = new ArrayList<>();
		List<SonarMetric> sonar = new ArrayList<>();
		SonarMetric metric = new SonarMetric();
		metric.setMetricName("quality");
		metric.setMessage("msg");
		sonar.add(metric);
		aggregatedValueList.add(sonar);

		AggregationUtils.aggregateForCodeMetrics(aggregatedValueList);
	}

}
