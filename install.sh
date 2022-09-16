#!/bin/sh

BRANCH=master

# Check args passed to script
if [ $# -eq 1 ]; then
    BRANCH=$1
fi

# Check if git is installed
if ! [ -x "$(command -v git)" ]; then
    echo 'Error: git is not installed.' >&2
    exit 1
fi

# Check if docker is installed
if ! [ -x "$(command -v docker)" ]; then
    echo 'Error: docker is not installed.' >&2
    exit 1
fi

# Clone repo from github "https://github.com/Z-Jais/Scraper.git" on branch $BRANCH
git clone --single-branch --branch "$BRANCH" https://github.com/Z-Jais/Scraper.git ./tmp

cd ./tmp || exit

# Build docker image
docker build -t scraper .
# Run docker container
docker run -it scraper

cd .. || exit
# Remove tmp folder
rm -rf ./tmp