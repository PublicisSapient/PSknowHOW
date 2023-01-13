# Open source PsKnowHOW MongoDB

Kpidashboard application using MongoDB-4.4.1 to save the processor's data. Each processor accessing data from source and transforming and pushing it in mongodb. 

### Build MongoDB Docker image
```
cd mongodb
docker build -t mongodb .
```

### Custom mongo image

We use custom base image psknowhow/mongo-base:4.4.1-bionic which is made from official mongo:4.4.1-bionic, which is lighter compared to parent image i.e. mongo:4.4.1-bionic which is made by ignoring unrequired mongo libraries like mongostat, mongoimport, mongoexport etc .
### How to build Custom Mongo image 
```
docker pull  mongo:4.4.1-bionic
docker run -d --name mongodb-bionic mongo:4.4.1-bionic
docker exec -it mongodb-bionic /bin/bash
rm -rf  rm -rf /usr/bin/mongos && rm -rf /usr/bin/mongoimport && rm -rf /usr/bin/mongoexport && rm -rf /usr/bin/mongofiles && rm -rf /usr/bin/mongotop && rm -rf /usr/bin/mongostat
exit
docker export mongodb-bionic > mongodb-baseimage.tar
docker import mongodb-baseimage.tar
sha256:c11d39f80f9111fc5eb31fa71c7c32c91644d86afa47961ffe365638ae99a01f
docker tag c11d39f80f9111fc5eb31fa71c7c32c91644d86afa47961ffe365638ae99a01f psknowhow/mongodb-baseimage:latest
docker push psknowhow/mongodb-base:4.4.1-bionic
```
refference : https://medium.com/@samhavens/how-to-make-a-docker-container-smaller-by-deleting-files-7354b5c6c8f1
### Run Mongodb
```
docker run -d -p 27017:27017 -v <volumes>:/data/db mongodb
OR
docker-compose up -d
```
