package com.publicissapient.kpidashboard.rally.util;

import com.rallydev.rest.util.QueryFilter;

public class RqlParser {

    public static QueryFilter parseRql(String rqlQuery) {
        // Split the RQL into individual conditions
        String[] conditions = rqlQuery.split(" AND | OR ");

        QueryFilter queryFilter = null;

        for (String condition : conditions) {
            condition = condition.trim();

            // Remove parentheses
            if (condition.startsWith("(") && condition.endsWith(")")) {
                condition = condition.substring(1, condition.length() - 1);
            }

            // Split into key, operator, and value
            String[] parts = condition.split(" ");
            if (parts.length == 3) {
                String key = parts[0];
                String operator = parts[1];
                String value = parts[2];

                // Remove quotes from value if present
                if (value.startsWith("\"") && value.endsWith("\"")) {
                    value = value.substring(1, value.length() - 1);
                }

                // Create a QueryFilter for the condition
                QueryFilter conditionFilter = new QueryFilter(key, operator, value);

                // Combine conditions using AND/OR
                if (queryFilter == null) {
                    queryFilter = conditionFilter;
                } else {
                    if (rqlQuery.contains(" AND ")) {
                        queryFilter = queryFilter.and(conditionFilter);
                    } else if (rqlQuery.contains(" OR ")) {
                        queryFilter = queryFilter.or(conditionFilter);
                    }
                }
            }
        }

        return queryFilter;
    }
}