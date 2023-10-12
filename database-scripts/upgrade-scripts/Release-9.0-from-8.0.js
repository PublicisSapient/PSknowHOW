//------------------------- 8.1.0 changes----------------------------------------------------------------------------------
//Sprint Capacity Changes
db.kpi_master.updateOne(
  {
    "kpiId": "kpi46"
  },
  {
    $set: {
      "kpiFilter": "radioButton"
    }
  }
  )

db.getCollection('field_mapping_structure').insertMany([
{
        "fieldName": "excludeSpilledKpi46",
        "fieldLabel": "Exclude Spilled Issues",
        "fieldType": "radiobutton",
        "section": "Custom Fields Mapping",
        "tooltip": {
             "definition": "By enabling this, any issues spilled from any previous sprint will not be considered for estimates hours or logged hours calculation."
        },
        "options": [{
             "label": "On",
             "value": "On"
        },
        {
             "label": "Off",
             "value": "Off"
        }
        ]
   }
])