const duplicateSprints = new Set();
const uniqueSprints = new Set();
db.sprint_details.find().forEach(sprint => {
    if(!uniqueSprints.has(sprint.sprintID)){
        uniqueSprints.add(sprint.sprintID);
    }else{
        console.log("duplicate sprint detail with id :"+sprint.sprintID);
        duplicateSprints.add(sprint._id);
    }
});

console.log(duplicateSprints);

console.log("deleting duplicate sprint....");
duplicateSprints.forEach(sprint => {
    db.sprint_details.deleteOne({"_id": new ObjectId(sprint)});
});