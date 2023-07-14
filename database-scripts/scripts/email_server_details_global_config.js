if (db.getCollection("global_config").find({
        env: "dojo"
    }).count() > 0) {
    if (db.getCollection("global_config").find({
            "emailServerDetail": {
                $exists: true
            }
        }).count() == 0) {
        db.getCollection("global_config").update({
            "env": "dojo"
        }, {
            $set: {
                "emailServerDetail": {
                    "emailHost": "mail.example.com",
                    "emailPort": 25,
                    "fromEmail": "no-reply@example.com",
                    "feedbackEmailIds": [
                        "sampleemail@example.com"
                    ]
                }
            }
        }, {})
    }
} else {
    db.getCollection("global_config").insert({
        "env": "email",
        "emailServerDetail": {
            "emailHost": "mail.example.com",
            "emailPort": 25,
            "fromEmail": "no-reply@example.com",
            "feedbackEmailIds": [
                "sampleemail@example.com"
            ]
        },
        "_class": "com.publicissapient.kpidashboard.common.model.application.GlobalConfig"
    })
}