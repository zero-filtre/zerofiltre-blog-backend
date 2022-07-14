#!/bin/sh

echo "The app is starting ..."
exec java -jar -Xms1g -Xmx4g -XX:+ExitOnOutOfMemoryError -Dspring.profiles.active=kubernetes "blog.jar"
