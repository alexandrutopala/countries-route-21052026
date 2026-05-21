# BUILDPLATFORM = platform of the machine running the build (native, no emulation)
# TARGETPLATFORM = platform of the image being produced
ARG BUILDPLATFORM
ARG TARGETPLATFORM

# Stage 1: Build — runs on the native platform so Maven never runs under emulation
FROM --platform=$BUILDPLATFORM eclipse-temurin:25-jdk AS builder
WORKDIR /build

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline -q

COPY src/ src/
RUN ./mvnw package -DskipTests -q

# Stage 2: Runtime — uses the target platform JRE
FROM --platform=$TARGETPLATFORM eclipse-temurin:25-jre AS runtime
WORKDIR /app

COPY --from=builder /build/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
