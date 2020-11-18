FROM maven AS builder
WORKDIR /work
RUN git clone https://github.com/maodou38/JrebelLicenseServerforJava.git
WORKDIR /work/JrebelLicenseServerforJava
RUN mvn package
FROM openjdk:7-jre-alpine
ENV PORT 8081
COPY --from=builder /work/JrebelLicenseServerforJava/JrebelBrainsLicenseServerforJava-1.0-SNAPSHOT-jar-with-dependencies.jar /JrebelBrainsLicenseServerforJava.jar
CMD java -jar /JrebelBrains.jar -p $PORT
