// comment feature collections generic field name for all board (iteration , release)
db.kpi_comments.updateMany(
  {},
  [
    { $set: { "nodeChildId": "$sprintId" } },
    { $unset: "sprintId" },
  ]
)
db.kpi_comments_history.updateMany(
  {},
  [
    { $set: { "nodeChildId": "$sprintId" } },
    { $unset: "sprintId" },
  ]
)