if(db.hierarchy_levels.find().count() == 0 ) {
db.hierarchy_levels.insert([{
            "level": 1,
            "hierarchyLevelId": "hierarchyLevelOne",
            "hierarchyLevelName": "Level One"
        }, {
            "level": 2,
            "hierarchyLevelId": "hierarchyLevelTwo",
            "hierarchyLevelName": "Level Two"
        }, {
            "level": 3,
            "hierarchyLevelId": "hierarchyLevelThree",
            "hierarchyLevelName": "Level Three"
        }
    ]);
}
