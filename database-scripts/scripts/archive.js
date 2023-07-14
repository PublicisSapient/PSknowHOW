// read data from purge_config collection - this contains date value to purge the data, collections to purge
print("Reading file purge_config.txt...");
var config_file = cat('/etc/init.d/purge_config.txt');
config_file = config_file.split('\n');

var config = {};	
for(i=0; i<config_file.length; i++) {
	data = config_file[i].split('=');
	config[data[0]] = data[1];
}
var endDate = null;
var startDate = null;
if(config.purgeOlderThanMonths != "") {
	// purge data older than last n months
	endDate = new Date();
	endDate.setMonth(endDate.getMonth()-config.purgeOlderThanMonths);
	print("Purging data before last " + config.purgeOlderThanMonths + " months i.e. before: " + endDate);
} else if(config.purgeOlderThanDate != "") {
	// purge data older than the provided date
	endDate = new Date(config.purgeOlderThanDate);
	print("Purging data before " + config.purgeOlderThanDate);
} else if(config.purgeStartDate != "" && config.purgeEndDate != "") {
	// purge data between given dates
	endDate = new Date(config.purgeEndDate);
	startDate = new Date(config.purgeStartDate);
	startDate.setUTCHours(0,0,0,0);
	print("Purging data from " + config.purgeStartDate + " till " + config.purgeEndDate);
} else {
	// default date value - data older than 6 months is purged
	endDate = new Date();
	endDate.setMonth(endDate.getMonth()-7);
	print("No date value is provided. Purging data before last 6 months.");
}
//set hours to 0 to apply check on only date
endDate.setUTCHours(0,0,0,0);  

var connectionString = config.host + ":" + config.port + "/" + config.dbname;
// Get the connection to db on which purged data will be saved.
var archivedb = new Mongo().getDB(config.dbname); 

print("--------Archive DB name is: " + archivedb);
print("--------Live DB name is: " + db);

var projectConfigMap = {};
db.project_config.find().forEach( function(doc){
	projectConfigMap[doc._id.valueOf()] = doc.projectName;
});
var projectFieldMapping = {};
db.field_mapping.find().forEach( function(doc){
	project_name = projectConfigMap[doc.projectConfigId.valueOf()];
	projectFieldMapping[project_name] = doc;
});

if(archivedb.auth(config.user, config.password)) {	
	print("Reading file collections_to_purge.csv...");
	var collectionsToPurge = cat('/etc/init.d/collections_to_purge.csv');
	var collectionsToPurge = collectionsToPurge.split('\n');
	var collectionsList = [];
	for(i=1; i < collectionsToPurge.length; i++) {
		var collEntry = {};
		var purge_details1 = collectionsToPurge[i].split(','); 
		collEntry['collectionName'] = purge_details1[0];
		collEntry['field_to_check'] = purge_details1[1];
		collEntry['unique_identifier'] = purge_details1[2];
		collEntry['foreign_collection'] = purge_details1[3];
		collEntry['foreign_field'] = purge_details1[4];
		collEntry['foreign_field_to_check'] = purge_details1[5];
		collEntry['date_type'] = purge_details1[6];
		collEntry['order'] = purge_details1[7];
		collectionsList.push(collEntry);
	}
	collectionsList.sort((a,b) => (a.order > b.order) ? 1 : ((b.order > a.order) ? -1 : 0));
	var collectionIdMap = {};
	// loop over all the collections
	for(i=0; i < collectionsList.length; i++) {  
		var purge_details = collectionsList[i]; 
		// name of the collection
		var collectionName = purge_details.collectionName; 
		print("Purge start for collection: " + collectionName);		
		
		//this is a date field on basis of which date conditions are applied
		var field_to_check = purge_details.field_to_check; 
		//Used to create list of IDs for purging data
		var unique_identifier = purge_details.unique_identifier; 
		//if any collection does not have a date field then values of foreign collection will be used to fetch the data to purge
		//foreign collection is the collection which has a field with same values as in current collection
		var foreign_collection = purge_details.foreign_collection; 
		//field which has the same value as in unique_identifier field of current collection
		var foreign_field = purge_details.foreign_field; 
		// date field in foreign collection	
		var foreign_field_to_check = purge_details.foreign_field_to_check; 	
		// date field can be of any type like 1. timestamp 2. date string 3. ISO date
		var date_type = purge_details.date_type; 
		
		var idListName = collectionName + '_' + unique_identifier;
		var distinctValField = unique_identifier;
		var collectionToQuery = collectionName;
		var fieldToQuery = field_to_check;
		if(foreign_collection != "") {
			//use the fields of parent collection to query date conditions
			idListName = foreign_collection + '_' + foreign_field;
			distinctValField = foreign_field;
			collectionToQuery = foreign_collection;
			fieldToQuery = foreign_field_to_check;
		}
		
		var fromDate = startDate;
		var toDate = endDate;
		if(date_type == "timestamp") {
			toDate = new Date(endDate).getTime();
			fromDate = startDate != null ? new Date(startDate).getTime() : null;
		} else if(date_type == "date") {
			toDate = new Date(endDate).toISOString();
			fromDate = startDate != null ? new Date(startDate).toISOString() : null;
		}
		print("Date value which is compared - endDate: " + toDate + ", startDate: " + fromDate);
		// create list of ids to compare while purging the data. this list is created based on date conditions
		var expiredIds = [];
		var dataList = [];
		getExpiredIds = function(doc) {
			if(typeof doc == 'object') {
				expiredIds.push(doc.valueOf());
			} else {
				expiredIds.push(doc);
			}
		}
		// query on collection to purge data.
		if( collectionIdMap[idListName] != undefined && collectionIdMap[idListName].length > 0) { 
			dataList = collectionIdMap[idListName];		
		} else {
			if(fromDate != null) {
				db[collectionToQuery].distinct(distinctValField, {[fieldToQuery]: {$exists : true, $ne : "", $lte: toDate, $gte: fromDate}}).forEach(getExpiredIds);
			} else {
				db[collectionToQuery].distinct(distinctValField, {[fieldToQuery]: {$exists : true, $ne : "", $lte: toDate}}).forEach(getExpiredIds);
			}
			
			collectionIdMap[idListName] = expiredIds;
			dataList = expiredIds;
		}
		
		print("No. of entries that will be purged from collection " + collectionName + " are: " + dataList.length);
		var openDefectsList = [];
		//loop over all entries of the collection that need to be purged
		db[collectionName].find().forEach( function(doc) {
			fieldValue = (typeof doc[unique_identifier] == 'object') ? doc[unique_identifier].valueOf():doc[unique_identifier];
			if (dataList.indexOf(fieldValue) != -1 && openDefectsList.indexOf(fieldValue) == -1) {
				// for collection 'feature' we need to retain defects that are not closed
				if(collectionName == 'feature' && doc.sTypeName == 'Bug' && doc.sProjectName!== undefined) {
					var jiraDefectRemovalStatus = projectFieldMapping[doc.sProjectName].jiraDefectRemovalStatus;
					var jiraDefectRejectionStatus = projectFieldMapping[doc.sProjectName].jiraDefectRejectionStatus;
					jiraDefectRemovalStatus.push(jiraDefectRejectionStatus);
					
					if(jiraDefectRemovalStatus.indexOf(doc.sJiraStatus) == -1) {
						openDefectsList.push(fieldValue);
						print("Defect with id: " + fieldValue + " is in open state and cannot be purged.....");
					} else {
						print("-------------------purging feature --------- " + fieldValue);
						archivedb[collectionName].save(doc);
						if (archivedb.getLastError() == null) {
							db[collectionName].remove({[unique_identifier]: doc[unique_identifier]});
						} else {
							print("Error: could not purge " + collectionName + " with " + unique_identifier + " : " + doc[unique_identifier]);
							print(archivedb.getLastError());
						}
					}
				} else {
					archivedb[collectionName].save(doc);
					if (archivedb.getLastError() == null) {
						db[collectionName].remove({[unique_identifier]: doc[unique_identifier]});
					} else {
						print("Error: could not purge " + collectionName + " with " + unique_identifier + " : " + doc[unique_identifier]);
						print(archivedb.getLastError());
					}
				}
			} 
		});	
		if(collectionName == 'feature') {
			var updatedIdList = [];
			for(k=0; k<dataList.length; k++) {
				if (openDefectsList.indexOf(dataList[k]) == -1) {
					updatedIdList.push(dataList[k]);
				}
			}
			collectionIdMap[idListName] = updatedIdList;
		}
		
	} 
} else {
		throw "Not authorized on archive db. Please enter correct credentials";
	}
