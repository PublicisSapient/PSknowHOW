/*
 *   Copyright 2014 CapitalOne, LLC.
 *   Further development Copyright 2022 Sapient Corporation.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.publicissapient.kpidashboard.apis.sonar.utiils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class SonarQualityMetricTest {
    /**
     * Method under test:
     * {@link SonarQualityMetric#addQualityMetrics(long, long, long, long, long)}
     */
    @Test
    public void testAddQualityMetrics() {
        // Arrange
        SonarQualityMetric sonarQualityMetric = new SonarQualityMetric();

        // Act
        sonarQualityMetric.addQualityMetrics(1L, 1L, 1L, 1L, 1L);

        // Assert
        List<Long> denominatorLeft = sonarQualityMetric.getDenominatorLeft();
        assertEquals(1, denominatorLeft.size());
        List<Long> denominatorRight = sonarQualityMetric.getDenominatorRight();
        assertEquals(1, denominatorRight.size());
        List<Long> numeratorLeft = sonarQualityMetric.getNumeratorLeft();
        assertEquals(1, numeratorLeft.size());
        List<Long> numeratorRight = sonarQualityMetric.getNumeratorRight();
        assertEquals(1, numeratorRight.size());
        assertEquals(1L, sonarQualityMetric.getCostPerLine());
        assertEquals(1L, denominatorLeft.get(0).longValue());
        assertEquals(1L, denominatorRight.get(0).longValue());
        assertEquals(1L, numeratorLeft.get(0).longValue());
        assertEquals(1L, numeratorRight.get(0).longValue());
    }

    /**
     * Method under test: {@link SonarQualityMetric#getTEngineData()}
     */
    @Test
    public void testGetTEngineData() {
        // Arrange, Act and Assert
        assertEquals(0.0d, (new SonarQualityMetric()).getTEngineData(), 0.0);
    }

    /**
     * Method under test: {@link SonarQualityMetric#getTEngineData()}
     */
    @Test
    public void testGetTEngineData2() {
        // Arrange
        SonarQualityMetric sonarQualityMetric = new SonarQualityMetric();
        sonarQualityMetric.addQualityMetrics(1L, 1L, 1L, 1L, 1L);

        // Act and Assert
        assertEquals(100.0d, sonarQualityMetric.getTEngineData(), 0.0);
    }

    /**
     * Method under test:
     * {@link SonarQualityMetric#calculateRatio(List, List, long)}
     */
    @Test
    public void testCalculateRatio() {
        // Arrange
        SonarQualityMetric sonarQualityMetric = new SonarQualityMetric();
        ArrayList<Long> numerator = new ArrayList<>();

        // Act and Assert
        assertEquals(0.0d, sonarQualityMetric.calculateRatio(numerator, new ArrayList<>(), 1L), 0.0);
    }

    /**
     * Method under test:
     * {@link SonarQualityMetric#calculateRatio(List, List, long)}
     */
    @Test
    public void testCalculateRatio2() {
        // Arrange
        SonarQualityMetric sonarQualityMetric = new SonarQualityMetric();

        ArrayList<Long> numerator = new ArrayList<>();
        numerator.add(1L);

        // Act and Assert
        assertEquals(0.0d, sonarQualityMetric.calculateRatio(numerator, new ArrayList<>(), 1L), 0.0);
    }

    /**
     * Method under test:
     * {@link SonarQualityMetric#calculateRatio(List, List, long)}
     */
    @Test
    public void testCalculateRatio3() {
        // Arrange
        SonarQualityMetric sonarQualityMetric = new SonarQualityMetric();

        ArrayList<Long> numerator = new ArrayList<>();
        numerator.add(0L);
        numerator.add(1L);

        // Act and Assert
        assertEquals(0.0d, sonarQualityMetric.calculateRatio(numerator, new ArrayList<>(), 1L), 0.0);
    }

    /**
     * Method under test:
     * {@link SonarQualityMetric#calculateRatio(List, List, long)}
     */
    @Test
    public void testCalculateRatio4() {
        // Arrange
        SonarQualityMetric sonarQualityMetric = new SonarQualityMetric();
        ArrayList<Long> numerator = new ArrayList<>();

        ArrayList<Long> denominator = new ArrayList<>();
        denominator.add(1L);

        // Act and Assert
        assertEquals(0.0d, sonarQualityMetric.calculateRatio(numerator, denominator, 1L), 0.0);
    }

    /**
     * Method under test:
     * {@link SonarQualityMetric#calculateRatio(List, List, long)}
     */
    @Test
    public void testCalculateRatio5() {
        // Arrange
        SonarQualityMetric sonarQualityMetric = new SonarQualityMetric();
        ArrayList<Long> numerator = new ArrayList<>();

        ArrayList<Long> denominator = new ArrayList<>();
        denominator.add(0L);
        denominator.add(1L);

        // Act and Assert
        assertEquals(0.0d, sonarQualityMetric.calculateRatio(numerator, denominator, 1L), 0.0);
    }

    /**
     * Method under test: default or parameterless constructor of
     * {@link SonarQualityMetric}
     */
    @Test
    public void testNewSonarQualityMetric() {
        // Arrange and Act
        SonarQualityMetric actualSonarQualityMetric = new SonarQualityMetric();

        // Assert
        assertEquals(0.0d, actualSonarQualityMetric.getTEngineData(), 0.0);
        assertEquals(0L, actualSonarQualityMetric.getCostPerLine());
        assertTrue(actualSonarQualityMetric.getDenominatorLeft().isEmpty());
        assertTrue(actualSonarQualityMetric.getDenominatorRight().isEmpty());
        assertTrue(actualSonarQualityMetric.getNumeratorLeft().isEmpty());
        assertTrue(actualSonarQualityMetric.getNumeratorRight().isEmpty());
    }
}
