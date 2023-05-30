
let projects = db.user_board_config.find({});

function isIterationReviewKpi(kpiId) {

    return kpiId == "kpi148" || kpiId == "kpi140";

}

function isIterationProgressKpi(kpiId) {

    return kpiId == "kpi146";

}

for (let i = 0; i < projects.size(); i++) {

    let scrum = projects[i].scrum;

    let _id = projects[i]._id;

    for (let j = 0; j < scrum.length; j++) {

        if (scrum[j].boardId == 5) {

            let kpis = scrum[j].kpis;

            kpis.forEach(kpi => {

                // if(isIterationReviewKpi(kpi.kpiId))

                kpi.subCategoryBoard = "Iteration Review";

                // if(isIterationProgressKpi(kpi.kpiId))

                //kpi.subCategoryBoard = "Iteration Progress" ;

            })

        }

    }

    var result = db.user_board_config.updateOne({

        "_id": _id

    }, {

        $set: {

            'scrum': scrum

        }

    });

    print(result);

}