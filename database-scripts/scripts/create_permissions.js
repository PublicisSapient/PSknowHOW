db.getCollection('permissions').remove({});

db.permissions.insertMany([
    {
     
      permissionName: "RoleEdit",
      operationName: "Edit",
      resourceName:"resource1",
      createdDate:  new Date(Date.now()),
      lastModifiedDate: new Date(Date.now()),
      isDeleted: "False"
     
    },
    {
         
      permissionName: "RoleCreate",
      operationName: "Create",
      resourceName:"resource2",
      createdDate:  new Date(Date.now()),
      lastModifiedDate: new Date(Date.now()),
      isDeleted: "False"
     
    },
    {
         
      permissionName: "RoleCreate",
      operationName: "Create",
      resourceName:"resource3",
      createdDate:  new Date(Date.now()),
      lastModifiedDate: new Date(Date.now()),
      isDeleted: "False"
     
    },
    {
        
      permissionName: "RoleDelete",
      operationName: "Delete",
      resourceName:"resource3",
      createdDate:  new Date(Date.now()),
      lastModifiedDate: new Date(Date.now()),
      isDeleted: "False"
     
    },
    {
      
      permissionName: "View",
      operationName: "Read",
      resourceName:"resource4",
      createdDate:  new Date(Date.now()),
      lastModifiedDate: new Date(Date.now()),
      isDeleted: "False"
     
    },
    {

      permissionName: "ViewAll",
      operationName: "Read",
      resourceName:"resource5",
      createdDate:  new Date(Date.now()),
      lastModifiedDate: new Date(Date.now()),
      isDeleted: "False"
     
    }

])


db.permissions.find({}).forEach(function(permissionDoc){

    if(permissionDoc['resourceName'] != ""){
        permissionDoc['resourceId'] = db.resource.findOne({resourceName: permissionDoc.resourceName})._id;
    }

    
    db.permissions.save(permissionDoc);
});