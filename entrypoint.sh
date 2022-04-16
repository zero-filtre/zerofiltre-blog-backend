#!/bin/sh

echo "The app is starting ..."
exec java -jar "blog.jar"
#exec java -jar -Dspring.profiles.active=deploy "blog.jar"
