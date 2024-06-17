#!/bin/sh

echo "The app is starting ..."

source /vault/secrets/config

java -jar -Xms1g -Xmx4g -XX:+ExitOnOutOfMemoryError -Dspring.profiles.active=kubernetes -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 "blog.jar"
