//------------------------- 8.1.0 changes----------------------------------------------------------------------------------
//Sprint Capacity Changes
//dts-29093
db.getCollection("kpi_master").updateMany(
{ kpiId: { $in: ["kpi46"] } },
{ $unset: {
          "kpiFilter": null,
          } }
);
db.field_mapping_structure.deleteMany({
    "fieldName": {
        $in: ["excludeSpilledKpi46"]
    }
});