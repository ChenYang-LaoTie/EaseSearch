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
    && wget https://repo.huaweicloud.com/apache/maven/maven-3/3.8.1/binaries/apache-maven-3.8.1-bin.tar.gz \
    && tar -zxvf apache-maven-3.8.1-bin.tar.gz \
    && npm i pnpm -g \
    && mkdir -p ${BASEPATH}

ENV JAVA_HOME=${WORKSPACE}/jdk-19.0.2
ENV PATH=${JAVA_HOME}/bin:$PATH

ENV MAVEN_HOME=${WORKSPACE}/apache-maven-3.8.1
ENV PATH=${MAVEN_HOME}/bin:$PATH

COPY . ${BASEPATH}
RUN cd ${BASEPATH} \
    && mvn clean install package -Dmaven.test.skip \
    && rm -rf `ls | grep -v target`
#    && rm ${BASEPATH}/target/classes/application.yaml

ARG PUBLIC_USER
ARG PUBLIC_PASSWORD
RUN cd ${BASEPATH} \
   && git clone https://$PUBLIC_USER:$PUBLIC_PASSWORD@github.com/Open-Infra-Ops/plugins \
   && cp plugins/armorrasp/rasp.tgz . \
   && tar -zxf rasp.tgz \
   && rm -rf plugins

RUN mkdir -p ${SOURCE} \
    && cd ${SOURCE} \
    && git clone https://gitee.com/mindspore/website-docs.git \
    && cd ${BASEPATH}/target/classes/script/mindspore \
    && chmod +x initDoc.sh \
    && ./initDoc.sh

ENV searchsystem mindspore

RUN rm -rf `find / -iname "*gcc*"` \
    && rm -rf `find / -iname "*cpp*"` \
    && rm -rf `find /etc/ -iname "*gdb*"` \
    && rm -rf `find /usr/ -iname "*gdb*"` \
    && rm -rf /root/.m2/repository/* \
    && jlink --module-path ${JAVA_HOME}/jmods --add-modules java.base,java.sql,java.naming,java.desktop,java.management,java.security.jgss,java.instrument,java.rmi,jdk.unsupported --output ${WORKSPACE}/jre \
    && rm -rf ${WORKSPACE}/jdk-19.0.2 \
    && rm -rf ${WORKSPACE}/apache-maven-3.8.1 \
    && rm -rf ${WORKSPACE}/openjdk-19.0.2_linux-x64_bin.tar.gz \
    && rm -rf ${WORKSPACE}/apache-maven-3.8.1-bin.tar.gz

ENV JAVA_HOME=${WORKSPACE}/jre
ENV PATH=${JAVA_HOME}/bin:$PATH

USER easysearch

EXPOSE 8080

CMD java -javaagent:${BASEPATH}/rasp/rasp.jar -jar ${BASEPATH}/target/EaseSearch-0.0.1-SNAPSHOT.jar --spring.config.location=${APPLICATION_PATH}
