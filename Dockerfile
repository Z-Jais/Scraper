FROM maven:3.8.6-jdk-11-slim
WORKDIR /app
COPY . /app
RUN apt-get update && apt-get install -y screen libgtk-3-0 libasound2 libx11-6 libxcomposite1 libxdamage1 libxext6 libxfixes3 libxrandr2 libxrender1 libxtst6 libfreetype6 libfontconfig1 libpangocairo-1.0-0 libpangocairo-1.0-0 libpango-1.0-0 libatk1.0-0 libcairo-gobject2 libcairo2 libgdk-pixbuf-2.0-0 libglib2.0-0 libdbus-glib-1-2 libdbus-1-3 libxcb-shm0 libx11-xcb1 libxcb1 libxcursor1 libxi6 libnss3 libnspr4 libdrm2 libgbm1
RUN mvn clean package -DskipTests
RUN ls -lash
RUN mv target/scraper-1.0-SNAPSHOT-jar-with-dependencies.jar /scraper.jar
RUN rm -rf /app/**
RUN mv /scraper.jar /app/scraper.jar
CMD ["screen", "-dmS", "Scraper", "java", "-jar", "scraper.jar"]