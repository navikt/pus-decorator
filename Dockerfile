# gjør det mulig å bytte base-image slik at vi får bygd både innenfor og utenfor NAV
ARG BASE_IMAGE_PREFIX=""
FROM ${BASE_IMAGE_PREFIX}maven:3.6.2 as maven-builder
ADD / /source
WORKDIR /source
RUN mvn install -DskipTests

FROM docker.pkg.github.com/navikt/pus-nais-java-app/pus-nais-java-app:java8
COPY --from=maven-builder /source/target/decorator /app
