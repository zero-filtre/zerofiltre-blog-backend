FROM adoptopenjdk/openjdk11:alpine-jre

ARG JAR_FILE=target/blog.jar

ARG PROFILE=dev

WORKDIR /opt/app

COPY opentelemetry-javaagent.jar /opt/app/opentelemetry-javaagent.jar

COPY ${JAR_FILE} blog.jar


ENV OTEL_SERVICE_NAME=zerofiltre-backend-${PROFILE}

ENV OTEL_METRICS_EXPORTER=none

ENV OTEL_LOGS_EXPORTER=none

ENV OTEL_EXPORTER_OTLP_PROTOCOL=grpc

ENV OTEL_EXPORTER_OTLP_ENDPOINT=http://otelcol-opentelemetry-collector.monitoring.svc.cluster.local:4317

COPY entrypoint.sh entrypoint.sh

RUN chmod 755 entrypoint.sh

ENTRYPOINT ["./entrypoint.sh"]