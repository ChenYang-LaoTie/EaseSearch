FROM openjdk:17-alpine
RUN mkdir -p /EaseSearch

WORKDIR /EaseSearch
COPY . /EaseSearch
RUN chmod 777 -R ./* \
    && ./mvnw clean install package -Dmaven.test.skip \
    && cd ./target/classes \
    && chmod 777 -R script \
    && apk add git \
    && cd ../
EXPOSE 8080
CMD java -jar ./target/EaseSearch-0.0.1-SNAPSHOT.jar



#FROM ubuntu
#RUN mkdir -p /EaseSearch \
#    && apt update \
#    && apt-get install -y openjdk-17-jdk
#
#WORKDIR /EaseSearch
#COPY . /EaseSearch
#RUN chmod 777 -R ./* \
#    && ./mvnw clean install package -Dmaven.test.skip \
#    && cd ./target/classes \
#    && chmod 777 -R script \
#    && cd ../
#EXPOSE 8080
#CMD java -jar ./target/EaseSearch-0.0.1-SNAPSHOT.jar