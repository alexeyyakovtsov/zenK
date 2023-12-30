FROM gradle:jdk11 as builder

WORKDIR /app

COPY . /app

RUN gradle build

FROM openjdk:11-jre-slim

WORKDIR /app

COPY --from=builder /app/build/libs/backend-*.jar ./application.jar

CMD java -server -Xmx256M -jar application.jar