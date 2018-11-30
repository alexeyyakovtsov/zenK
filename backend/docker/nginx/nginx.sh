#!/bin/bash

NGINX_SERVICE_NAME=${1:-nginx}
VOLUME_OR_PATH=$2
DIRECTORY=`dirname $0`

# CREATE NGINX CONTAINER
docker create \
    --name ${NGINX_SERVICE_NAME} \
    --network host \
    --restart=always \
    -v ${VOLUME_OR_PATH}:/www/data/images:ro \
    nginx:1.15.7

# COPY CONFIG FOR NGINX TO CONTAINER
docker cp ${DIRECTORY}/nginx.conf ${NGINX_SERVICE_NAME}:/etc/nginx/conf.d/

# RUN CONTAINER
docker start nginx