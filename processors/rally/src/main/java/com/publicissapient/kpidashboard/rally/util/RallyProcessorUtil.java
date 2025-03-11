package com.publicissapient.kpidashboard.rally.util;

import java.util.List;
import org.apache.commons.lang3.StringUtils;

import com.publicissapient.kpidashboard.rally.model.RallyChangelogGroup;
import com.publicissapient.kpidashboard.rally.model.RallyIssueField;
import com.publicissapient.kpidashboard.rally.constant.RallyConstants;

public final class RallyProcessorUtil {
    private RallyProcessorUtil() {
    }

    public static String deodeUTF8String(String inputString) {
        return inputString == null ? null : StringUtils.normalizeSpace(inputString);
    }

    public static String getChangeLogValue(List<RallyChangelogGroup> histories, String field) {
        String value = null;
        if (histories != null && !histories.isEmpty()) {
            for (RallyChangelogGroup history : histories) {
                if (history.getItems() != null) {
                    for (RallyIssueField issueField : history.getItems()) {
                        if (field.equals(issueField.getField())) {
                            value = issueField.getFromString();
                            break;
                        }
                    }
                }
                if (value != null) {
                    break;
                }
            }
        }
        return value;
    }

    public static String getFirstChangeLog(List<RallyChangelogGroup> histories, String field) {
        String value = null;
        if (histories != null && !histories.isEmpty()) {
            for (RallyChangelogGroup history : histories) {
                if (history.getItems() != null) {
                    for (RallyIssueField issueField : history.getItems()) {
                        if (field.equals(issueField.getField())) {
                            value = issueField.getFromString();
                            return value;
                        }
                    }
                }
            }
        }
        return value;
    }

    public static String getLastChangeLog(List<RallyChangelogGroup> histories, String field) {
        String value = null;
        if (histories != null && !histories.isEmpty()) {
            for (int i = histories.size() - 1; i >= 0; i--) {
                RallyChangelogGroup history = histories.get(i);
                if (history.getItems() != null) {
                    for (RallyIssueField issueField : history.getItems()) {
                        if (field.equals(issueField.getField())) {
                            value = issueField.getFromString();
                            return value;
                        }
                    }
                }
            }
        }
        return value;
    }

    public static String handleNullValue(String value) {
        return StringUtils.isBlank(value) ? RallyConstants.EMPTY_STR : value;
    }
}
