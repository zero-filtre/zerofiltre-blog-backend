FROM adoptopenjdk/openjdk11:alpine-jre AS builder

ARG JAR_FILE=target/blog.jar

WORKDIR /opt/app

COPY opentelemetry-javaagent.jar ${JAR_FILE} entrypoint.sh ./

RUN chmod 755 entrypoint.sh

FROM adoptopenjdk/openjdk11:alpine-jre

ARG PROFILE=dev

RUN addgroup -S appgroup && \
    adduser -S appuser -G appgroup

WORKDIR /opt/app

COPY --from=builder /opt/app/opentelemetry-javaagent.jar /opt/app/blog.jar /opt/app/entrypoint.sh ./

ENV OTEL_SERVICE_NAME=zerofiltre-backend-${PROFILE} \
    OTEL_EXPORTER_OTLP_PROTOCOL=grpc \
    OTEL_EXPORTER_OTLP_ENDPOINT=http://otelcol-opentelemetry-collector.monitoring.svc.cluster.local:4317

USER appuser

ENTRYPOINT ["./entrypoint.sh"]