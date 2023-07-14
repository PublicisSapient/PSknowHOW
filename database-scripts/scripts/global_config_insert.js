if (db.global_config.countDocuments({}) == 0) {
    db.global_config.insert([{
                "env": "dojo",
                "authTypeStatus": {
                    "standardLogin": true,
                    "adLogin": false
                }
            }
        ]);
} else {
    var globalConfig = db.global_config.findOne();
    if (!('authTypeStatus' in globalConfig)) {
        var authTypeStatus = {};
        var adServerDetails = globalConfig["adServerDetail"];
        if (adServerDetails) {
            authTypeStatus = {
                "standardLogin": true,
                "adLogin": true
            }
        } else {
            authTypeStatus = {
                "standardLogin": true,
                "adLogin": false
            }
        }
        globalConfig["authTypeStatus"] = authTypeStatus;
        db.global_config.save(globalConfig);
    }
}
