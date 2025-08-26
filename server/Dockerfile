# syntax=docker/dockerfile:1
FROM gradle:8.10.2-jdk17-alpine AS build
WORKDIR /app
COPY . /app
# Build server distribution only
RUN gradle :server:installDist --no-daemon

FROM eclipse-temurin:17-jre-alpine
ENV DATABASE_URL=jdbc:postgresql://db:5432/translatio \
    POSTGRES_USER=postgres \
    POSTGRES_PASSWORD=postgres \
    SERVER_PORT=8080
WORKDIR /app
COPY --from=build /app/server/build/install/server /app/server
EXPOSE 8080
CMD ["/app/server/bin/server"]
