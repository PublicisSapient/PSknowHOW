let userBoardData = db.user_board_config.find({});

const iterationReviewKpi = ["kpi119", "kpi138"];

const iterationProgressKpi = ["kpi121"];

function isIterationReviewKpi(kpiId) {

    for (let kpi of iterationReviewKpi)

       if(kpi===kpiId) {
        return true;
       }
    return false;
}

function isIterationProgressKpi(kpiId) {

    for (let kpi of iterationProgressKpi)

       if(kpi==kpiId) {
       return true;
       }
    return false;
}



for (let i = 0; i < userBoardData.size(); i++) {

    let scrum = userBoardData[i].scrum;

    let _id = userBoardData[i]._id;

    for (let j = 0; j < scrum.length; j++) {

        if (scrum[j].boardName == "Iteration") {

            let kpis = scrum[j].kpis;

            kpis.forEach(kpi => {

                if (isIterationReviewKpi(kpi.kpiId))

                    kpi.subCategoryBoard = "Iteration Review";

                if (isIterationProgressKpi(kpi.kpiId))

                    kpi.subCategoryBoard = "Iteration Progress";

            })
        }
    }


    const result = db.user_board_config.updateOne({

        "_id": _id

    }, {

        $set: {

            'scrum': scrum

        }

    });

    print(result);

}