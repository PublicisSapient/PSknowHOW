//---------8.1.0 changes----------------------------------------------------------------------
db.field_mapping_structure.deleteMany({
    "fieldName": {
        $in: ["thresholdValueKPI14",
            "thresholdValueKPI82",
            "thresholdValueKPI111",
            "thresholdValueKPI35",
            "thresholdValueKPI34",
            "thresholdValueKPI37",
            "thresholdValueKPI28",
            "thresholdValueKPI36",
            "thresholdValueKPI16",
            "thresholdValueKPI17",
            "thresholdValueKPI38",
            "thresholdValueKPI27",
            "thresholdValueKPI72",
            "thresholdValueKPI84",
            "thresholdValueKPI11",
            "thresholdValueKPI62",
            "thresholdValueKPI64",
            "thresholdValueKPI67",
            "thresholdValueKPI65",
            "thresholdValueKPI157",
            "thresholdValueKPI158",
            "thresholdValueKPI159",
            "thresholdValueKPI160",
            "thresholdValueKPI164"]
    }
});

// delete Sonar Code Quality Kpi
db.getCollection('kpi_master').deleteOne(
  { "kpiId": "kpi168" }
);

// delete kpi_category_mapping for Sonar Code Quality
db.kpi_category_mapping.deleteOne({
    "kpiId": "kpi168"
});