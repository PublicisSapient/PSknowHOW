if(db.hierarchy_level_suggestions.find().count() == 0 ) {
db.hierarchy_level_suggestions.insert([{
            "hierarchyLevelId": "hierarchyLevelOne",
            "values": [
			"Sample One",
			"Sample Two"
			]
        }, {

            "hierarchyLevelId": "hierarchyLevelTwo",
            "values": [
			"Sample Three",
			"Sample Four"
			]
        }, {

            "hierarchyLevelId": "hierarchyLevelThree",
            "values": ["Sample Five", "Sample Six"]
        }
    ]);
}