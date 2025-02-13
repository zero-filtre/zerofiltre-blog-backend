# Phase de build
FROM adoptopenjdk/openjdk11:alpine-jre AS builder

# Arguments pour le fichier JAR et le profil
ARG JAR_FILE=target/blog.jar

# Définition du répertoire de travail
WORKDIR /opt/app

# Copie des fichiers nécessaires
COPY opentelemetry-javaagent.jar ${JAR_FILE} entrypoint.sh ./

# Modification des permissions du script d'entrée
RUN chmod 755 entrypoint.sh

# Phase de runtime
FROM adoptopenjdk/openjdk11:alpine-jre

ARG PROFILE=dev

# Création d'un nouvel utilisateur non-root
RUN addgroup -S appgroup && \
    adduser -S appuser -G appgroup

# Définition du répertoire de travail
WORKDIR /opt/app

# Copie des artefacts de la phase de build
COPY --from=builder /opt/app/opentelemetry-javaagent.jar /opt/app/blog.jar /opt/app/entrypoint.sh ./

# Configuration des variables d'environnement
ENV OTEL_SERVICE_NAME=zerofiltre-backend-${PROFILE} \
    OTEL_EXPORTER_OTLP_PROTOCOL=grpc \
    OTEL_EXPORTER_OTLP_ENDPOINT=http://otelcol-opentelemetry-collector.monitoring.svc.cluster.local:4317

# Basculer vers l'utilisateur non-root
USER appuser

# Définition du point d'entrée
ENTRYPOINT ["./entrypoint.sh"]