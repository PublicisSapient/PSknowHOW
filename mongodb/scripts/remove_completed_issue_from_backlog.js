print("Start of script to remove complete issue from backlog")
let issues  = db.issue_backlog.find({"status": {$in:["Closed","Live","Done","Dropped"]}});
print(issues.size())
issues.forEach(function(issue){
   {
      let res1 = db.jira_issue.save(issue);
      let res2 = db.issue_backlog.deleteOne({"_id":issue._id});
      let historyObj = db.issue_backlog_custom_history.findOne({"storyID" : issue.number, "basicProjectConfigId" : issue.basicProjectConfigId});
      let res3 = db.jira_issue_custom_history.save(historyObj);
      let res4 = db.issue_backlog_custom_history.deleteOne({"_id":historyObj._id});
   }
})
print(db.issue_backlog.find({"status": {$in:["Closed"," Live","Done"]}}).count())
print("End of script to remove complete issue from backlog")