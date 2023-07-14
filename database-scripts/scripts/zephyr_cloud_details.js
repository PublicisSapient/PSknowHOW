
db.global_config.update({"zephyrCloudBaseUrl": {"$exists": false}},{$set : {"zephyrCloudBaseUrl":"https://api.zephyrscale.smartbear.com/v2/"}},{upsert:false,multi:true})

