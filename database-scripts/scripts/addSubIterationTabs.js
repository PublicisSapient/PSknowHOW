const iterationReviewKpi = ["kpi121", "kpi119", "kpi128", "kpi75", "kpi123", "kpi122", "kpi120", "kpi124", "kpi132",
 "kpi133", "kpi134", "kpi131", "kpi135", "kpi136", "kpi140", "kpi145"];

const iterationProgressKpi = ["kpi125"];

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


db.user_board_config.find({}).forEach(function(use_board) {
 var boardScrum = use_board.scrum;
 var id = use_board._id;
 
 boardScrum.forEach(function(scrum){
     if(scrum.boardName == "Iteration"){
           scrum.kpis.forEach(function(kpi){
                if (isIterationReviewKpi(kpi.kpiId) && kpi.subCategoryBoard == undefined){
                        print("kpi_id Iteration Review" + kpi.kpiId);
                        kpi.subCategoryBoard = "Iteration Review";
                        }

                if (isIterationProgressKpi(kpi.kpiId) && kpi.subCategoryBoard == undefined){
                    print("kpi_id Iteration Progress" + kpi.kpiId);
                    kpi.subCategoryBoard = "Iteration Progress";
                    }
            });
     }
 });

 const result = db.user_board_config.updateOne({ "_id": id}, {$set: {'scrum': boardScrum}});

});