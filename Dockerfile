FROM adoptopenjdk/openjdk11:alpine-jre

ARG JAR_FILE=target/blog.jar

WORKDIR /opt/app

COPY ${JAR_FILE} blog.jar

COPY entrypoint.sh entrypoint.sh

RUN chmod 755 entrypoint.sh

ENTRYPOINT ["./entrypoint.sh"]