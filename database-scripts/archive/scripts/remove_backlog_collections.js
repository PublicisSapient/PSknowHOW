print("Start: Script to Removing Backlog Collection")
db.issue_backlog.drop();
db.issue_backlog_custom_history.drop();
print("End: Script to Removing Backlog Collection")