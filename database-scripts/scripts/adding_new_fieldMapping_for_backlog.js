const fms = db.field_mapping.find({});

fms.forEach((fm)=> {
      let id = fm._id;
      let defectDropStatus = fm.jiraDefectDroppedStatus;
      db.field_mapping.updateOne({
           "_id":id,
           "excludeStatusKpi129": { $exists: false }
        }, {
            $set: {
                'excludeStatusKpi129': defectDropStatus
            }
        });
});
