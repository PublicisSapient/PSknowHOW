/* --- author: Priyanka Jain
* --- Changes ------
* --- 1. add field estimationCriteria & storyPointToHourMapping with default values
*/

db.getCollection('field_mapping').update({"estimationCriteria": {"$exists": false}}, {$set: {"estimationCriteria": "Story Point", "storyPointToHourMapping": 8.0}}, {multi: true});