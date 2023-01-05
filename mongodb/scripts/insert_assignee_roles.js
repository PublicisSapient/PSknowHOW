db.getCollection('assignee_roles').remove({});
db.assignee_roles.insertMany([
{
    roleId :"backend_developer",
    roleDisplayName: "Backend Developer",
    createdDate: new Date(Date.now())
}, 
{   
    roleId :"frontend_developer",
    roleDisplayName: "Frontend Developer",
    createdDate: new Date(Date.now())
}, 
{   
    roleId :"tester",
    roleDisplayName: "Tester",
    createdDate: new Date(Date.now())
}
]);