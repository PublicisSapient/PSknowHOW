# Installation Guide

## Prerequisite

1. DNS allocated to the Host machine
2. Valid SSL certificate with Common Name

## Steps

1. Download the `docker-compose.yaml` file from the `central-auth-service` folder.
2. Open the `docker-compose.yaml` file in any preferred editor and replace all the placeholders like Docker image tags, DB username, and password, etc., and save it.
3. Now pull the Docker image by running:
    ```
    docker-compose pull
    ```
4. After successfully pulling the Docker image, run the container by executing:
    ```
    docker-compose up -d
    ```
5. Your application will now be running and can be accessed from a browser using the DNS or Host IP.
