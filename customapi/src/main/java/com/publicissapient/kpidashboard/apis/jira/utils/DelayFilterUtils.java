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

package com.publicissapient.kpidashboard.apis.jira.utils;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class DelayFilterUtils {

    private DelayFilterUtils() {} // Prevent instantiation

    /**
     * Generic method to filter items with positive delays.
     * @param items List of items to filter
     * @param delayExtractor Function to extract the delay string (e.g., IssueKpiModalValue::getDelayInDays)
     * @return Filtered list with only positive delays
     */
    public static <T> List<T> filterPositiveDelays(
            List<T> items,
            Function<T, String> delayExtractor) {

        return items.stream()
                .filter(item -> isValidPositiveDelay(delayExtractor.apply(item)))
                .collect(Collectors.toList());
    }

    /**
     * Checks if a delay string is a valid positive number.
     * Supports formats like "3d", "-4d", "0", "5 days", etc.
     */
    public static boolean isValidPositiveDelay(String delay) {
        if (delay == null || delay.trim().isEmpty() || delay.trim().equals("-")) {
            return false;
        }

        // Extract numeric part (handles "3d", "-4d", "5 days", etc.)
        String numericPart = delay.replaceAll("[^0-9-]", "");

        try {
            int days = Integer.parseInt(numericPart);
            return days > 0; // Only positive delays
        } catch (NumberFormatException e) {
            return false; // Invalid number format
        }
    }
}