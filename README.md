# PSknowHOW

> PSknowHOW started its life as an addition of bespoke features on top of Hygieia and embarked its own journey to support the digital transformation of Publicis Sapient clients.A dedicated team of engineers crafted it into a product which stands on its own. 
PSknowHOW has enabled multiple organization to optimize their ways of working. 

PSknowHOW is a measurement framework that delivers an intuitive, visual dashboard to track key performance indicators (KPIs) across entire organizations transformation programs. It empowers teams with the knowledge of HOW work is progressing, areas of health, as well as achievement gaps and areas for improvement.

Extraordinarily useful for businesses implementing digital business transformation programs, it should be implemented as part of program startup, and used as a tool for continuous reflection and improvement.

This KPI dashboard connects with all the different tools including Product Management, Design, Technical Quality, Agile Project Management, and Team Operations – and has a flexible architecture to capture engagement metrics and health for a program.  It consolidates the information in one place. By consolidating and visualizing the data, team members, program leaders and executives get a single source of truth and understanding of the program health.

For Detailed product documentation and feature list please refer to the link ->
https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/1212417/About+PSknowHOW

For installation instruction please refer to the link -> 
https://psknowhow.atlassian.net/wiki/spaces/PSKNOWHOW/pages/1966195/KnowHOW+-+Installation+Setup


# Installation Guide of AuthNAuth application

## Prerequisite

1. DNS allocated to the Host machine
2. Valid SSL certificate with Common Name

## Steps

1. Download the `docker-compose.yaml` file.
2. Open the `docker-compose.yaml` file in any preferred editor and uncomment the auth service. and replace all the placeholders like Docker image tags, DB username, and password, etc., and save it.
3. Now pull the Docker image by running:
    ```
    docker-compose pull
    ```
4. After successfully pulling the Docker image, run the container by executing:
    ```
    docker-compose up -d
    ```
5. Your application will now be running and can be accessed from a browser using the DNS..






 
