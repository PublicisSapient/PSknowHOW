db.getCollection('roles').remove({});
db.roles.insertMany([{ 
    roleName :"ROLE_PROJECT_VIEWER",
    displayName: "Project Viewer",
    description: "read kpi data at project level",
    createdDate: new Date(Date.now()),
    lastModifiedDate: new Date(Date.now()),
    isDeleted: "False",
    permissionNames: ['View']
}, 
{   
    roleName :"ROLE_PROJECT_ADMIN",
    displayName: "Project Admin",
    description: "manage user-roles at project level",
    createdDate: new Date(Date.now()),
    lastModifiedDate: new Date(Date.now()),
    isDeleted: "False",
    permissionNames: ['View']     
}, 
{   
    roleName :"ROLE_SUPERADMIN",
    displayName: "Super Admin",
    description: "access to every resource in the instance",
    createdDate: new Date(Date.now()),
    lastModifiedDate: new Date(Date.now()),
	isDeleted: "False",
    permissionNames: ["ViewAll"]
}, 
{   
    roleName :"ROLE_GUEST",
    displayName: "Guest",
    description: "read access for the instance",
    createdDate: new Date(Date.now()),
    lastModifiedDate: new Date(Date.now()),
	isDeleted: "False",
    permissionNames: ["View"]
}
]);
db.roles.find({}).forEach(function(roleDoc){
    var permissionNames = [];
    roleDoc['permissionNames'].forEach(function(permissionName){
         permissionNames.push(db.permissions.findOne({permissionName: permissionName}));
    });
    
    roleDoc['permissions'] = permissionNames;
    
    delete(roleDoc['permissionNames']);
    db.roles.save(roleDoc);
});   
