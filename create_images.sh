#!/bin/sh
docker buildx create --use
docker buildx build --load --platform linux/amd64 -t jais-scraper:amd64 .
docker buildx build --load --platform linux/arm64 -t jais-scraper:arm64 .