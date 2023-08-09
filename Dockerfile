FROM node

ENV LANG C.UTF-8
ENV LC_ALL C.UTF-8
ENV LANGUAGE C.UTF-8
ENV NODE_OPTIONS --max-old-space-size=8192

RUN groupadd -g 1001 easysearch \
    && useradd -u 1001 -g easysearch -s /bin/bash -m easysearch

ENV WORKSPACE=/home/easysearch
ENV SOURCE=${WORKSPACE}/file/source
ENV TARGET=${WORKSPACE}/file/target
ENV BASEPATH=${WORKSPACE}/EaseSearch

WORKDIR ${WORKSPACE}

RUN apt update \
    && apt install libasm-java \
    && wget http://security.debian.org/debian-security/pool/updates/main/j/json-smart/libjson-smart-java_2.2-2+deb10u1_all.deb \
    && dpkg -i libjson-smart-java_2.2-2+deb10u1_all.deb

RUN wget https://download.java.net/java/GA/jdk19.0.2/fdb695a9d9064ad6b064dc6df578380c/7/GPL/openjdk-19.0.2_linux-x64_bin.tar.gz \
    && tar -zxvf openjdk-19.0.2_linux-x64_bin.tar.gz \
    && npm i pnpm -g \
    && mkdir -p ${BASEPATH}

ENV JAVA_HOME=${WORKSPACE}/jdk-19.0.2
ENV PATH=${JAVA_HOME}/bin:$PATH

COPY . ${BASEPATH}
RUN cd ${BASEPATH} \
    && chmod +x mvnw \
    && ./mvnw clean install package -Dmaven.test.skip \
    && rm -rf `ls | grep -v target`

ARG PUBLIC_USER
ARG PUBLIC_PASSWORD
RUN cd ${BASEPATH} \
   && git clone https://$PUBLIC_USER:$PUBLIC_PASSWORD@github.com/Open-Infra-Ops/plugins \
   && cp plugins/armorrasp/rasp.tgz . \
   && tar -zxf rasp.tgz \
   && rm -rf plugins

USER easysearch

EXPOSE 8080

CMD java -javaagent:${BASEPATH}/rasp/rasp.jar -jar ${BASEPATH}/target/EaseSearch-0.0.1-SNAPSHOT.jar --spring.config.location=${APPLICATION_PATH}
