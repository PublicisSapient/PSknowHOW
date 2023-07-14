

    
if (db.authentication.find({username: "SUPERADMIN"}).count() == 0 ) {

	db.getCollection("authentication").insert({
		"_class": "com.publicissapient.speedy.common.model.auth",
		"username": "SUPERADMIN",
		"password": "sha512:fe08d67f12ab21191d3e665b9b360f5946068a3763be460ef0cbbbaeb2d951660d0c1568f1a74479f8c2ce83132f88693b477b316c00bc62a853d155614e4adb",
		"email": "",
		"approved" : true
	})

	db.getCollection("user_info").insert({
		"_class": "com.publicissapient.speedy.common.model.rbac",
		"username": "SUPERADMIN",
		"authorities": [
			"ROLE_SUPERADMIN"
		],
		"authType": "STANDARD",
		"projectsAccess": []
	})

} else {
	db.getCollection("user_info").update({username: "SUPERADMIN"}, 
    {$set:{authorities:["ROLE_SUPERADMIN"],
    "projectsAccess": []
    }}, 
    {}
  )

}


