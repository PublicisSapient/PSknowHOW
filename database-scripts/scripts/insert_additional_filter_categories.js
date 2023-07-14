if(db.additional_filter_categories.find().count() == 0 ) {
db.additional_filter_categories.insert([{
            "level": 1,
            "filterCategoryId": "afOne",
            "filterCategoryName": "Additional Filter One"
        }
    ]);
}
