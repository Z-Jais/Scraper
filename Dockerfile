FROM maven:3.9.4-amazoncorretto-17 AS build
WORKDIR /app
COPY . /app
RUN mvn clean package -DskipTests

FROM mcr.microsoft.com/playwright:v1.38.0-jammy

ARG version=17.0.8.8-1
RUN set -eux \
    && apt-get update \
    && apt-get install -y --no-install-recommends \
        curl ca-certificates gnupg software-properties-common fontconfig java-common \
    && curl -fL https://apt.corretto.aws/corretto.key | apt-key add - \
    && add-apt-repository 'deb https://apt.corretto.aws stable main' \
    && mkdir -p /usr/share/man/man1 || true \
    && apt-get update \
    && apt-get install -y java-17-amazon-corretto-jdk=1:"$version" \
    && apt-get purge -y --auto-remove -o APT::AutoRemove::RecommendsImportant=false \
        curl gnupg software-properties-common \
    && rm -rf /var/lib/apt/lists/* /var/cache/apt/archives/*

ENV LANG C.UTF-8
ENV JAVA_HOME=/usr/lib/jvm/java-17-amazon-corretto

COPY --from=build /app/target/scraper-1.0-SNAPSHOT-jar-with-dependencies.jar /app/scraper.jar

RUN apt-get update && DEBIAN_FRONTEND=noninteractive apt-get install -y tzdata && rm -rf /var/lib/apt/lists/* /var/cache/apt/archives/*
ENV TZ=Europe/Paris
RUN ln -snf /usr/share/zoneinfo/"$TZ" /etc/localtime && echo "$TZ" > /etc/timezone
RUN dpkg-reconfigure --frontend noninteractive tzdata

WORKDIR /app
CMD ["java", "-jar", "scraper.jar"]