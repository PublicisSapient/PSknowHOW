# Install Kpidashboard - Offline Mode - Set up KpiDashboard Instance.
# Steps to follow.

* Download the required KnowHOW version from below location https://lion.app.box.com/s/tofw1y04a336s7tkfw7rg2cxqz9b89x3 , To /KnowHOWX.Y.Z-Offline folder.
* Create a directory /KnowHOWX.Y.Z, where X is major, Y is minor and Z is the patch version.
* cd to /KnowHOWX.Y.Z-Offline and then
* Unzip downloaded file.
```
$ unzip Speedy-X.Y.Z.zip -d /KnowHOWX.Y.Z
Archive:  SpeedyX.Y.Z.zip
 extracting: dockers.zip             
  inflating: readme.md               
  inflating: docker-compose.yaml     
   creating: scripts/
  inflating: scripts/backup.sh  
  inflating: scripts/dir.sh          
  inflating: scripts/docker-images-up.sh  
  inflating: scripts/ipwhitelist.sh  
  inflating: scripts/restore.sh      
```
* You could refer the extract file with the below file hierarchy.
```
$ tree /KnowHOWX.Y.Z
.
├── readme.md
├── docker-compose.yaml
├── dockers.zip
└── scripts
    ├── backup.sh
    ├── dir.sh
    ├── docker-images-up.sh
    ├── ipwhitelist.sh
    └── restore.sh
```
* And then cd to /KnowHOWX.Y.Z/ and run the following scripts
* chmod +x -R scripts/

******For Fresh installation please follow below steps****
* ./scripts/dir.sh
* if you want PS DB use setup-speedy.tools.publicis.sapient.com/speedy/mongodb-ps image in docker-compose.yaml file else use setup-speedy.tools.publicis.sapient.com/speedy/mongodb
* ./scripts/docker-images-up.sh


******For KnowHOW upgrade please follow below steps*****
* ./scripts/backup.sh 
* ./scripts/dir.sh
* ./scripts/docker-images-up.sh
* ./scripts/restore.sh
