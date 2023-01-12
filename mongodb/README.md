# Open source speedy MongoDB

Kpidashboard application using MongoDB-3.4 to save the processor's data. Each processor accessing data from source and transforming and pushing it in mongodb. 

### Build MongoDB Docker image
```
cd mongodb
docker build -t mongodb .
```

### Run Mongodb
```
docker run -d -p 27017:27017 -v <volumes>:/data/db mongodb
OR
docker-compose up -d
```

### Let's understand Vertical concept  
We consider each instance to be a producer which produce kpi's result for particular vertical. So when user set up kpidashboard instance via installer or Offline 
we ask user to select vertical. As you are running it setting it up manually so you also have to provide vertical Id 
```
  Consumer Products -> consumer_products
  Energy & Commodities -> enc
  Financial Services -> fs
  Health -> health
  Public Sector -> public_sector
  Retail -> retail
  State & Local -> state_n_local
  Telco Media & High Tech -> telco_media_high_tech
  Transportation & Mobility -> tnm
  Travel & Hospitality -> tnh

```

### Update Vertical Id, default value - consumer_products 
```
version: "2"
services:
  MongoDB:
    image: mongodb
    tty: true
    hostname: mongodb
    restart: on-failure
    ports:
      - 27017:27017
    volumes:
      - /app/apps/db_data:/data/db
      - /app/apps/logs/mongodb:/data/logs
      - /var/run/docker.sock:/var/run/docker.sock
    mem_limit: 4096M
    mem_reservation: 2048M
    environment:
      - MONGODB_ADMIN_USER=admin
      - MONGODB_ADMIN_PASS=reset@123
      - MONGODB_APPLICATION_DATABASE=kpidashboard
      - MONGODB_APPLICATION_USER=devadmin
      - MONGODB_APPLICATION_PASS=admin@123
      - ROLE=PRODUCER
      - VERTICALS=<VerticalId>
    networks:
      - Network_app

networks:
  Network_app:
```
####  

```
