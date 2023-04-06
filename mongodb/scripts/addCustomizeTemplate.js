print("Start : add customize template code in project tool config for existing project");


db.getCollection('project_basic_configs').find({"kanban" : true}).forEach(
        basicProjectConfig => {
            const basicProjectConfigId = basicProjectConfig._id;
            const basicConfigId = basicProjectConfigId.str;
            print("Project basic configId", basicConfigId);

           db.getCollection('project_tool_configs').find({"basicProjectConfigId": ObjectId(basicConfigId),
            "toolName" : "Jira", "metadataTemplateCode" : {$exists : false }}).forEach(
                        projectToolConfig => {
                            const projectToolId = projectToolConfig._id;
                            print("Project Tool config id :", projectToolId);
                            const toolConfigId = projectToolId.str;
                             db.getCollection('project_tool_configs').update({ "_id" : ObjectId(toolConfigId)},
                                        { $set : {metadataTemplateCode : "9" }})

                        })
        });

db.getCollection('project_basic_configs').find({"kanban" : false}).forEach(
        basicProjectConfig => {
            const basicProjectConfigId = basicProjectConfig._id;
            const basicConfigId = basicProjectConfigId.str;
            print("Project basic configId", basicConfigId);

           db.getCollection('project_tool_configs').find({"basicProjectConfigId": ObjectId(basicConfigId),
            "toolName" : "Jira", "metadataTemplateCode" : {$exists : false }}).forEach(
                        projectToolConfig => {
                            const projectToolId = projectToolConfig._id;
                            print("Project Tool config id :", projectToolId);
                            const toolConfigId = projectToolId.str;
                             print(db.getCollection('project_tool_configs').update({ "_id" : ObjectId(toolConfigId)},
                                        { $set : {metadataTemplateCode : "10" }}));

                        })
        });