#FROM maven:3-openjdk-15 AS MAVEN_BUILD
#
#ARG GITHUB_TOKEN
#
#ENV BUILD_HOME /home/safnari
#
#ENV HOME /application
#WORKDIR $HOME
#COPY . .
#RUN GITHUB_TOKEN=$GITHUB_TOKEN mvn clean install -Dmaven.test.skip=true -Ddocker.nocache
# 
FROM openjdk:8

ENV HOME /application
ENV JAR  dds-yaml-1.0.0.jar

USER 1000:1000
WORKDIR $HOME
COPY target/$JAR .

EXPOSE 8080/tcp
CMD java \
    -Xmx512m \
    -Djava.net.preferIPv4Stack=true \
    -Dcom.sun.xml.bind.v2.runtime.JAXBContextImpl.fastBoot=true \
    -Dlog4j.configurationFile=/config/log4j.xml \
    -Dspring.config.name=application \
    -Dspring.config.location=/config/ \
    -jar "$JAR" \
    -pidFile "./yaml.pid"
