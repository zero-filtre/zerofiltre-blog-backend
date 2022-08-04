#!/bin/sh

echo "The app is starting ..."

chmod +x /vault/secrets/config

. /vault/secrets/config

java -jar -Xms1g -Xmx4g -XX:+ExitOnOutOfMemoryError -Dspring.profiles.active=kubernetes "blog.jar"
