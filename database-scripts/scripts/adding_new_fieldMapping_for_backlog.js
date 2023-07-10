const fms = db.field_mapping.find({});

fms.forEach((fm)=> {
      let id = fm._id;
      let defectDropStatus = fm.jiraDefectDroppedStatus;
      let defectType = fm.jiradefecttype;
      db.field_mapping.updateOne({
           "_id":id
        }, {
            $set: {
                'excludeStatusKpi129': defectDropStatus,
                'issueTypeKpi127': defectType
            }
        });
});