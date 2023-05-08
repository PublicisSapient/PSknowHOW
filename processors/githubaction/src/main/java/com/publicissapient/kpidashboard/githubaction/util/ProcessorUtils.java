package com.publicissapient.kpidashboard.githubaction.util;


import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ProcessorUtils {

    private ProcessorUtils() {

    }


    public static String getString(JSONObject json, String key) {
        return (String) json.get(key);
    }

    public static JSONArray getJsonArray(JSONObject json, String key) {
        Object array = json.get(key);
        return array == null ? new JSONArray() : (JSONArray) array;
    }

    public static String firstCulprit(JSONObject buildJson) {
        JSONArray culprits = getJsonArray(buildJson, "author");
        if (CollectionUtils.isEmpty(culprits)) {
            return null;
        }
        JSONObject culprit = (JSONObject) culprits.get(0);
        return getFullName(culprit);
    }

    public static String getFullName(JSONObject jsonObject) {
        return getString(jsonObject, "name");
    }

}
