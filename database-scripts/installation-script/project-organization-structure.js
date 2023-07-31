//grouping of project for data aggregation. you can add upto six levels
if(db.hierarchy_levels.find().count() == 0 ) {
db.hierarchy_levels.insert([{
            "level": 1,
            "hierarchyLevelId": "hierarchyLevelOne",
            "hierarchyLevelName": "Organization"
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
//the value of which can be selected for each of above group.

if(db.hierarchy_level_suggestions.find().count() == 0 ) {
db.hierarchy_level_suggestions.insert([{
            "hierarchyLevelId": "hierarchyLevelOne",
            "values": [
			"Organization"			
			]
        }, {

            "hierarchyLevelId": "hierarchyLevelTwo",
            "values": [
			"Business Unit 1",
			"Business Unit 2"
			]
        }, {

            "hierarchyLevelId": "hierarchyLevelThree",
            "values": ["Portfolio 1", "Portfolio 2"]
        }
    ]);
}



//creating metadata for aditional filter next to scrum filter
db.additional_filter_categories.insert([{
            "level": 1,
            "filterCategoryId": "afOne",
            "filterCategoryName": "Teams"
        }
    ]);

