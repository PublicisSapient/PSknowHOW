package com.publicissapient.kpidashboard.githubaction.util;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.Assert.assertEquals;

@ExtendWith(SpringExtension.class)
 class ProcessorUtilsTest {

    @SuppressWarnings("java:S2699")
    @Test
     void firstCulprit(){
        JSONObject d = new JSONObject();
        ProcessorUtils.firstCulprit(d);
    }

    @Test
     void getString() {
        JSONObject obj = new JSONObject();
        obj.put("id", 1);
        obj.put("html_url", "https://test.com/testUser/testProject/actions/runs/956576842");
        assertEquals("https://test.com/testUser/testProject/actions/runs/956576842",
                ProcessorUtils.getString(obj, "html_url"));
    }

    @Test
     void getJsonArray() {
        JSONObject obj = new JSONObject();
        JSONArray array = new JSONArray();
        array.add(1);
        array.add(2);
        obj.put("id", 1);
        obj.put("html_url", "https://test.com/testUser/testProject/actions/runs/956576842");
        obj.put("workflow_id", array);
        assertEquals(array, ProcessorUtils.getJsonArray(obj, "workflow_id"));
    }

}
