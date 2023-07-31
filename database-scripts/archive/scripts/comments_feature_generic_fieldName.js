// comment feature collections generic field name for all board (iteration , release)
db.kpi_comments.updateMany(
  {"sprintId": { $exists: true }},
  [
    { $set: { "nodeChildId": "$sprintId" } },
    { $unset: "sprintId" },
  ]
)
db.kpi_comments_history.updateMany(
  { "sprintId": { $exists: true }},
  [
    { $set: { "nodeChildId": "$sprintId" } },
    { $unset: "sprintId" },
  ]
)