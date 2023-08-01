
// read data from restore_config collection - this contains date value to restore the data, collections to restore
print("Reading file restore_config.txt...");
var config_file = cat('/etc/init.d/restore_config.txt');
config_file = config_file.split('\n');

var config = {};	
for(i=0; i<config_file.length; i++) {
	data = config_file[i].split('=');
	config[data[0]] = data[1];
}
var endDate = null;
var startDate = null;
if(config.restoreOlderThanMonths != "") { 
	// restore data older than last n months
	endDate = new Date();
	endDate.setMonth(endDate.getMonth()-config.restoreOlderThanMonths);
	print("Restoring data before last " + config.restoreOlderThanMonths + " months i.e. before: " + endDate);
} else if(config.restoreOlderThanDate != "") { 
	// restore data older than the provided date
	endDate = new Date(config.restoreOlderThanDate);
	print("Restoring data before " + config.restoreOlderThanDate);
} else if(config.restoreStartDate != "" && config.restoreEndDate != "") {
	 // restore data between given dates
	endDate = new Date(config.restoreEndDate);
	startDate = new Date(config.restoreStartDate);
	startDate.setUTCHours(0,0,0,0);
	print("Restoring data from " + config.restoreStartDate + " till " + config.restoreEndDate);
} else { 
	// default date value - data older than 6 months is restored
	endDate = new Date();
	endDate.setMonth(endDate.getMonth()-6);
	print("No date value is provided. Restoring data before last 6 months.");
}
//set hours to 0 to apply check on only date 
endDate.setUTCHours(0,0,0,0); 

//var connectionString = config.host + ":" + config.port + "/" + config.dbname;
// Get the connection to db from which data to restore is fetched.
var restoredb = new Mongo().getDB(config.dbname);

print("--------Restore DB name is: " + restoredb);
print("--------Live DB name is: " + db);
 
if(restoredb.auth(config.user, config.password)) {
	print("Reading file collections_to_restore.csv...");
	var collectionsToRestore = cat('/etc/init.d/collections_to_restore.csv');
	collectionsToRestore = collectionsToRestore.split('\n');
	var collectionsList = [];
	for(i=1; i < collectionsToRestore.length; i++) {
		var collEntry = {};
		var restore_details1 = collectionsToRestore[i].split(','); 
		collEntry['collectionName'] = restore_details1[0];
		collEntry['field_to_check'] = restore_details1[1];
		collEntry['unique_identifier'] = restore_details1[2];
		collEntry['foreign_collection'] = restore_details1[3];
		collEntry['foreign_field'] = restore_details1[4];
		collEntry['foreign_field_to_check'] = restore_details1[5];
		collEntry['date_type'] = restore_details1[6];
		collEntry['order'] = restore_details1[7];
		collectionsList.push(collEntry);
	}
	collectionsList.sort((a,b) => (a.order > b.order) ? 1 : ((b.order > a.order) ? -1 : 0));
	// loop over all the collections
	for(i=0; i < collectionsList.length; i++) { 
		var restore_details = collectionsList[i]; 
		// name of the collection
		var collectionName = restore_details.collectionName; 
		print("Restore start for collection: " + collectionName);
		//this is a date field on basis of which date conditions are applied
		var field_to_check = restore_details.field_to_check;
		//Used to create list of IDs for restoring data
		var unique_identifier = restore_details.unique_identifier; 
		//if any collection does not have a date field then values of foreign collection will be used to fetch the data to restore
		//foreign collection is the collection which has a field with same values as in current collection
		var foreign_collection = restore_details.foreign_collection; 
		//field which has the same value as in unique_identifier field of current collection
		var foreign_field = restore_details.foreign_field; 
		// date field in foreign collection
		var foreign_field_to_check = restore_details.foreign_field_to_check; 
		// date field can be of any type like 1. timestamp 2. date string 3. ISO date
		var date_type = restore_details.date_type; 
		
		var idListName = collectionName + '_' + unique_identifier;
		var distinctValField = unique_identifier;
		var collectionToQuery = collectionName;
		var fieldToQuery = field_to_check;
		if(foreign_collection != "") {
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
		// create list of ids to compare while restoring the data. this list is created based on date conditions
		var restoreIds = [];
		var dataList = [];
		getRestoreIds = function(doc) {
			if(typeof doc == 'object') {
				restoreIds.push(doc.valueOf());
			} else {
				restoreIds.push(doc);
			}
		}
		// query on collection to restore data.
		if(  restoredb.system.js.find({_id:{$exists : true, $eq : idListName}}).count() == 0) {		
			if(fromDate != null) {
				restoredb[collectionToQuery].distinct(distinctValField, {[fieldToQuery]: {$exists : true, $ne : "", $lte: toDate, $gte: fromDate}}).forEach(getRestoreIds);
			} else {
				restoredb[collectionToQuery].distinct(distinctValField, {[fieldToQuery]: {$exists : true, $ne : "", $lte: toDate}}).forEach(getRestoreIds);
			}
			restoredb.system.js.save({_id: idListName, value: restoreIds});
			dataList = restoreIds;
		} else {
			dataList = restoredb.system.js.find({"_id": idListName}).toArray().map( function(u) { return u.value});
			dataList = dataList[0];
		}
		
		print("No. of entries that will be restored from collection " + collectionName + " are: " + dataList.length);
		//loop over all entries of the collection that need to be restored
		restoredb[collectionName].find().forEach( function(doc) {
			var fieldValue = (typeof doc[unique_identifier] == 'object') ? doc[unique_identifier].valueOf():doc[unique_identifier];
			//restore data for which unique field value is present in the list (which is created based on conditions)
			if (dataList.indexOf(fieldValue) != -1) {		
				//print("---------saving data-----"); 
                db[collectionName].update(doc, { "$set": { "_id": doc._id }}, { "upsert": true });		
				if (db.getLastError() != null) {
					print("Error: could not restore " + collectionName + " with " + unique_identifier + " : " + doc[unique_identifier]);
					print(db.getLastError());
				}
			}
		});
		restoredb.system.js.remove({_id: idListName});
	}
} else {
		throw "Not authorized on restore db. Please enter correct credentials";
	}
