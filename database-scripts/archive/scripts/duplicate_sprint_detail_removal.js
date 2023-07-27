const duplicateSprints = new Set();
const uniqueSprints = new Set();
db.sprint_details.find().sort({_id:-1}).forEach(sprint =>{
    if(!uniqueSprints.has(sprint.sprintID)){
        console.log("unique with id :"+sprint.sprintID);
        uniqueSprints.add(sprint.sprintID);
    }else{
        console.log("duplicate with id :"+sprint.sprintID);
        duplicateSprints.add(sprint._id);
    }
});

console.log(duplicateSprints);

console.log("deleting duplicate sprint....");
duplicateSprints.forEach(sprint=>{
    db.sprint_details.deleteOne({"_id": new ObjectId(sprint)});
});