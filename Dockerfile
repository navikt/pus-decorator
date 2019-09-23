# gjør det mulig å bytte base-image slik at vi får bygd både innenfor og utenfor NAV
ARG BASE_IMAGE_PREFIX=""
FROM ${BASE_IMAGE_PREFIX}maven as maven-builder
ADD / /source
WORKDIR /source
RUN mvn install -DskipTests

FROM navikt/java:8-appdynamics
ENV APPD_ENABLED=true
COPY --from=maven-builder /source/target/decorator /app