    var oldSuperadminUser = "SUPERADMIN"; // old superadmin username if already present in db for clean up
    var newSuperadminUser = "YOUR_NEW_SUPERADMIN_USERNAME"; // new superadmin username to insert into db

    db.getCollection("authentication").deleteMany({"username": oldSuperadminUser});
    db.getCollection("user_info").deleteMany({"username": oldSuperadminUser});

    // insert new superadmin user
	db.getCollection("user_info").insert({
		"_class": "com.publicissapient.speedy.common.model.rbac",
		"username": newSuperadminUser,
		"authorities": [
			"ROLE_SUPERADMIN"
		],
		"authType": "SSO",
		"projectsAccess": []
	});



