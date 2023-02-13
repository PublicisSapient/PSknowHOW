db.project_tool_configs.find({"toolName": { $in : [ "Jenkins" , "Bamboo" , "AzurePipeline" , "Teamcity"]}}).forEach(function(toolConfig) {
    var dbToolConfigId = toolConfig._id;
    var dbToolName = toolConfig.toolName;
	var dbBasicProjectConfigId = toolConfig.basicProjectConfigId;
	var dbJobName = toolConfig.jobName;
	print("processes started for toolConfigId ->", dbToolConfigId , ", ToolName ->" , dbToolName , "JobName ->" , dbJobName);
	print("basicProjectConfigId :", dbBasicProjectConfigId)
	db.processor_items.find({"toolConfigId": dbToolConfigId}).forEach(function(processorsItem) {
		var dbProcessorsItemId = processorsItem._id;
		var dbProcessorId = processorsItem.processorId;
		var desc = processorsItem.desc;
		print("found records for processorItemId ->", dbProcessorsItemId, ", JobName ->", desc)
		var result = db.build_details.updateMany({"processorItemId": dbProcessorsItemId},
				[{
					$set: {
						"basicProjectConfigId": dbBasicProjectConfigId,
						"projectToolConfigId": dbToolConfigId,
						"processorId": dbProcessorId
					}
				}, {
					$unset: ["processorItemId"]
				}]);
		print(result);
		db.processor_items.deleteOne({"_id": dbProcessorsItemId});
		print("remove processors items id ->" , dbProcessorsItemId);
	});
	print("process ended for toolConfigId ->", dbToolConfigId);
	print("--------------------------------------------")
})