db.getCollection('action_policy_rule').remove({});
db.getCollection('action_policy_rule').insert([
		{
            "name": "Super Admin",
            "roleAllowed": "",
            "description": "Super Admin can do all.",
            "roleActionCheck": "subject.authorities.contains('ROLE_SUPERADMIN')",
            "condition": "true",
            "createdDate": new Date(),
            "lastModifiedDate": new Date(),
            "isDeleted": false
        }, 
        {
            "name": "Add projects",
            "roleAllowed": "",
            "description": "Any user can add a project except guest user",
            "roleActionCheck": "!subject.authorities.contains('ROLE_GUEST') && action == 'ADD_PROJECT'",
            "condition": "true",
            "createdDate": new Date(),
            "lastModifiedDate": new Date(),
            "isDeleted": false
        }, 
        {
            "name": "Update projects",
            "roleAllowed": "",
            "description": "User with ROLE_PROJECT_ADMIN can update the project if has access of it",
            "roleActionCheck": "subject.authorities.contains('ROLE_PROJECT_ADMIN') && action == 'UPDATE_PROJECT'",
            "condition": "projectAccessManager.hasProjectEditPermission(resource.id, subject.getUsername())",
            "createdDate": new Date(),
            "lastModifiedDate": new Date(),
            "isDeleted": false
        }, 
        {
            "name": "Delete tool",
            "roleAllowed": "",
            "description": "User with ROLE_PROJECT_ADMIN can delete the tool associated with a project if has access of that project",
            "roleActionCheck": "subject.authorities.contains('ROLE_PROJECT_ADMIN') && action == 'DELETE_TOOL'",
            "condition": "projectAccessManager.hasProjectEditPermission(resource, subject.getUsername())",
            "createdDate": new Date(),
            "lastModifiedDate": new Date(),
            "isDeleted": false
        }, 
        {
            "name": "Cache project config",
            "roleAllowed": "",
            "description": "User can get cached/saved projects by him/her self",
            "roleActionCheck": "{'GET_SAVED_PROJECTS', 'SAVE_CACHE_PROJECT', 'UPDATE_CACHE_PROJECT'}.contains(action)",
            "condition": "subject.username == resource",
            "createdDate": new Date(),
            "lastModifiedDate": new Date(),
            "isDeleted": false
        }, 
        {
            "name": "Global config field",
            "roleAllowed": "",
            "description": "Only ROLE_SUPERADMIN can access this resource",
            "roleActionCheck": "action == 'GET_GLOBAL_CONFIG_FIELD' && resource != 'centralConfig'",
            "condition": "true",
            "createdDate": new Date(),
            "lastModifiedDate": new Date(),
            "isDeleted": false
        }, 
        {
            "name": "Central Config",
            "roleAllowed": "",
            "description": "Only ROLE_SUPERADMIN can access this resource",
            "roleActionCheck": "action == 'GET_GLOBAL_CONFIG_FIELD' && resource == 'centralConfig'",
            "condition": "true",
            "createdDate": new Date(),
            "lastModifiedDate": new Date(),
            "isDeleted": false
        }, 
        {
            "name": "Upload/Delete logo",
            "roleAllowed": "",
            "description": "Only superadmin can upload or delete logo",
            "roleActionCheck": "resource == 'LOGO' && {'FILE_UPLOAD', 'DELETE_LOGO'}.contains(action)",
            "condition": "subject.authorities.contains('ROLE_SUPERADMIN')",
            "createdDate": new Date(),
            "lastModifiedDate": new Date(),
            "isDeleted": false
        },
        {
            "name": "File Upload eng maturity master",
            "roleAllowed": "",
            "description": "ENG_MATURITY_MASTER can be uploaded by ROLE_SUPERADMIN only. Rest uploads can be done by any user",
            "roleActionCheck": "resource == 'ENG_MATURITY_MASTER' && action == 'FILE_UPLOAD'",
            "condition": "subject.authorities.contains('ROLE_SUPERADMIN')",
            "createdDate": new Date(),
            "lastModifiedDate": new Date(),
            "isDeleted": false
        },
        {
            "name": "File Upload engg maturity",
            "roleAllowed": "",
            "description": "project admin and superadmin can upload",
            "roleActionCheck": "resource == 'ENG_MATURITY' && action == 'FILE_UPLOAD'",
            "condition": "subject.authorities.contains('ROLE_SUPERADMIN') || subject.authorities.contains('ROLE_PROJECT_ADMIN')",
            "createdDate": new Date(),
            "lastModifiedDate": new Date(),
            "isDeleted": false
        },
        {
            "name": "Processor List",
            "roleAllowed": "",
            "description": "super admin and project admin can access list of processor",
            "roleActionCheck": "action == 'GET_PROCESSORS'",
            "condition": "subject.authorities.contains('ROLE_SUPERADMIN') || subject.authorities.contains('ROLE_PROJECT_ADMIN')",
            "createdDate": new Date(),
            "lastModifiedDate": new Date(),
            "isDeleted": false
        }, 
        {
            "name": "Run Processor",
            "description": "super admin and project admin can run processor",
            "roleActionCheck": "action == 'TRIGGER_PROCESSOR'",
            "condition": "projectAccessManager.canTriggerProcessorFor(resource, subject.getUsername())",
            "createdDate": new Date(),
            "lastModifiedDate": new Date(),
            "isDeleted": false
        }, 
        {
            "name": "Access request of the user",
            "roleAllowed": "",
            "description": "gets all access requests of the provided user",
            "roleActionCheck": "action == 'GET_ACCESS_REQUESTS_OF_USER'",
            "condition": "subject.authorities.contains('ROLE_SUPERADMIN') || subject.username == resource",
            "createdDate": new Date(),
            "lastModifiedDate": new Date(),
            "isDeleted": false
        }, 
        {
            "name": "SAVE_PROJECT_TOOL",
            "roleAllowed": "",
            "description": "User with ROLE_PROJECT_ADMIN save the project tool if has access of it",
            "roleActionCheck": "subject.authorities.contains('ROLE_PROJECT_ADMIN') && action == 'SAVE_PROJECT_TOOL'",
            "condition": "projectAccessManager.hasProjectEditPermission(resource, subject.getUsername())",
            "createdDate": new Date(),
            "lastModifiedDate": new Date(),
            "isDeleted": false
        }, 
        {
            "name": "UPDATE_PROJECT_TOOL",
            "roleAllowed": "",
            "description": "User with ROLE_PROJECT_ADMIN update the projecttool if has access of it",
            "roleActionCheck": "subject.authorities.contains('ROLE_PROJECT_ADMIN') && {'UPDATE_PROJECT_TOOL','DELETE_PROJECT_TOOL'}.contains(action)",
            "condition": "projectAccessManager.hasProjectEditPermission(resource, subject.getUsername())",
            "createdDate": new Date(),
            "lastModifiedDate": new Date(),
            "isDeleted": false
        },
        {
            "name": "CLEAN_PROJECT_TOOL_DATA",
            "roleAllowed": "",
            "description": "User with ROLE_PROJECT_ADMIN update the projectTool if has access of it",
            "roleActionCheck": "subject.authorities.contains('ROLE_PROJECT_ADMIN') && {'CLEAN_PROJECT_TOOL_DATA'}.contains(action)",
            "condition": "projectAccessManager.hasProjectEditPermission(resource, subject.getUsername())",
            "createdDate": new Date(),
            "lastModifiedDate": new Date(),
            "isDeleted": false
        },
        {
            "name": "capacity tab",
            "roleAllowed": "",
            "description": "User with ROLE_PROJECT_ADMIN and ROLE_SUPERADMIN can update or save the capacity data",
            "roleActionCheck": "subject.authorities.contains('ROLE_PROJECT_ADMIN') && action == 'SAVE_UPDATE_CAPACITY'",
            "condition": "projectAccessManager.hasProjectEditPermission(resource.basicProjectConfigId, subject.getUsername())",
            "createdDate": new Date(),
            "lastModifiedDate": new Date(),
            "isDeleted": false
        }, 
        {
            "name": "DELETE_PROJECT",
            "roleAllowed": "",
            "description": "User with ROLE_PROJECT_ADMIN can delete the project if granted access",
            "roleActionCheck": "subject.authorities.contains('ROLE_PROJECT_ADMIN') && {'DELETE_PROJECT'}.contains(action)",
            "condition": "projectAccessManager.hasProjectEditPermission(resource, subject.getUsername())",
            "createdDate": new Date(),
            "lastModifiedDate": new Date(),
            "isDeleted": false
        }, 
        {
            "name": "test Execution",
            "roleAllowed": "",
            "description": "User with ROLE_PROJECT_ADMIN and ROLE_SUPERADMIN can update or save the test Execution data",
            "roleActionCheck": "subject.authorities.contains('ROLE_PROJECT_ADMIN') && action == 'SAVE_UPDATE_TEST_EXECUTION'",
            "condition": "projectAccessManager.hasProjectEditPermission(resource.basicProjectConfigId, subject.getUsername())",
            "createdDate": new Date(),
            "lastModifiedDate": new Date(),
            "isDeleted": false
        },
        {
            "name": "Raise access request by the user",
            "roleAllowed": "",
            "description": "Restrict guest user to raise access request",
            "roleActionCheck": "!subject.authorities.contains('ROLE_GUEST') && action == 'RAISE_ACCESS_REQUEST'",
            "condition": "true",
            "createdDate": new Date(),
            "lastModifiedDate": new Date(),
            "isDeleted": false
        },
        {
            "name": "Connection access",
            "roleAllowed": "",
            "description": "Restrict guest user to create,update,get and delete connection",
            "roleActionCheck": "!subject.authorities.contains('ROLE_GUEST') && action == 'CONNECTION_ACCESS'",
            "condition": "true",
            "createdDate": new Date(),
            "lastModifiedDate": new Date(),
            "isDeleted": false
         },
         {
             "name": "AD Settings",
             "roleAllowed": "",
             "description": "Only superadmin can see or update the AD settings",
             "roleActionCheck": "action == 'SAVE_AD_SETTING' || action == 'GET_AD_SETTING'",
             "condition": "subject.authorities.contains('ROLE_SUPERADMIN')",
             "createdDate": new Date(),
             "lastModifiedDate": new Date(),
             "isDeleted": false
         },
         {
             "name": "Show Emm history and Statistics",
             "roleAllowed": "",
             "description": "Project viewer, Projet admin and superadmin can view emm upload history and statistics",
             "roleActionCheck": "{'GET_EMM_HISTORY', 'GET_EMM_STATISTICS'}.contains(action)",
             "condition": "subject.authorities.contains('ROLE_SUPERADMIN') || subject.authorities.contains('ROLE_PROJECT_VIEWER'), || subject.authorities.contains('ROLE_PROJECT_ADMIN')",
             "createdDate": new Date(),
             "lastModifiedDate": new Date(),
             "isDeleted": false
        },
        {
             "name": "Approve User",
             "roleAllowed": "",
             "description": "get, update and reject new user",
             "roleActionCheck": "action == 'APPROVE_USER'",
             "condition": "subject.authorities.contains('ROLE_SUPERADMIN')",
             "createdDate": new Date(),
             "lastModifiedDate": new Date(),
             "isDeleted": false
        },
        {
            "name" : "Access Request status",
            "roleAllowed" : "",
            "description" : "User with ROLE_PROJECT_ADMIN and ROLE_SUPERADMIN can see access request",
            "roleActionCheck" : "action == 'ACCESS_REQUEST_STATUS'",
            "condition" : "subject.authorities.contains('ROLE_PROJECT_ADMIN')",
            "createdDate" : new Date(),
            "lastModifiedDate" : new Date(),
            "isDeleted" : false
        },
        {
            "name" : "Grant Access",
            "roleAllowed" : "",
            "description" : "User with ROLE_PROJECT_ADMIN and ROLE_SUPERADMIN can grant access",
            "roleActionCheck" : "action == 'GRANT_ACCESS'",
            "condition" : "subject.authorities.contains('ROLE_PROJECT_ADMIN')",
            "createdDate" : ISODate("2022-01-03T20:39:43.139+05:30"),
            "lastModifiedDate" : ISODate("2022-01-03T20:39:43.139+05:30"),
            "isDeleted" : false
        },
        {
            "name": "DELETE_USER",
            "roleAllowed": "",
            "description": "User with role ROLE_SUPERADMIN can delete the users if granted access",
            "roleActionCheck" : "action == 'DELETE_USER'",
            "condition": "subject.authorities.contains('ROLE_SUPERADMIN')",
            "createdDate": new Date(),
            "lastModifiedDate": new Date(),
            "isDeleted": false
        },
        {
            "name": "Login type Configuration",
            "roleAllowed": "",
            "description": "SUPERADMIN can enable/disable a login type",
            "roleActionCheck" : "action == 'CONFIGURE_LOGIN_TYPE' || action == 'GET_LOGIN_TYPES_CONFIG' ",
            "condition": "subject.authorities.contains('ROLE_SUPERADMIN')",
            "createdDate": new Date(),
            "lastModifiedDate": new Date(),
            "isDeleted": false
        }
    ]);