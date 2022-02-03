#!/bin/sh

echo "The app is starting ..."
exec java -jar -Dspring.profiles.active=kubernetes "blog.jar"
