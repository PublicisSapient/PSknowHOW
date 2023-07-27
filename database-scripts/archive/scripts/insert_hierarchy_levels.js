if(db.hierarchy_levels.find().count() == 0 ) {
db.hierarchy_levels.insert([{
            "level": 1,
            "hierarchyLevelId": "hierarchyLevelOne",
            "hierarchyLevelName": "Orgnization"
        }, {
            "level": 2,
            "hierarchyLevelId": "hierarchyLevelTwo",
            "hierarchyLevelName": "Business Unit"
        }, {
            "level": 3,
            "hierarchyLevelId": "hierarchyLevelThree",
            "hierarchyLevelName": "Portfolio"
        }
    ]);
}
