//------------------------- 8.1.0 changes----------------------------------------------------------------------------------
db.getCollection('field_mapping_structure').insertMany([{
    "fieldName": "startDateCountKPI150",
    "fieldLabel": "Count of days from the release start date to calculate closure rate for prediction",
    "fieldType": "number",
    "section": "Issue Types Mapping",
    "tooltip": {
        "definition": "If this field is kept blank, then daily closure rate of issues is calculated based on the number of working days between today and the release start date or date when first issue was added. This configuration allows you to decide from which date the closure rate should be calculated."
    }
}]);

db.kpi_master.bulkWrite([
    {
        updateMany: {
            filter: { "kpiId": { $in: ["kpi150"] } },
            update: { $set: { "defaultOrder": 1 } }
        },
    }
]);