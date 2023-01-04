db.getCollection('assignee_roles').remove({});
db.assignee_roles.insertMany([
{
    roleName :"BackendDeveloper",
    roleDisplayName: "Backend Developer",
    createdDate: new Date(Date.now())
}, 
{   
    roleName :"FrontendDeveloper",
    roleDisplayName: "Frontend Developer",
    createdDate: new Date(Date.now())
}, 
{   
    roleName :"Tester",
    roleDisplayName: "Tester",
    createdDate: new Date(Date.now())
}
]);