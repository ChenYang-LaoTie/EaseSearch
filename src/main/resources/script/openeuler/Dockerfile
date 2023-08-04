FROM node

ENV LANG C.UTF-8
ENV LC_ALL C.UTF-8
ENV LANGUAGE C.UTF-8
ENV NODE_OPTIONS --max-old-space-size=8192
ENV searchsystem openEuler

RUN mkdir /workspace
WORKDIR /workspace

RUN apt update \
    && apt install libasm-java \
    && wget http://security.debian.org/debian-security/pool/updates/main/j/json-smart/libjson-smart-java_2.2-2+deb10u1_all.deb \
    && dpkg -i libjson-smart-java_2.2-2+deb10u1_all.deb  \
    && cd /workspace \
    && wget https://download.java.net/java/GA/jdk19.0.2/fdb695a9d9064ad6b064dc6df578380c/7/GPL/openjdk-19.0.2_linux-x64_bin.tar.gz \
    && tar -zxvf openjdk-19.0.2_linux-x64_bin.tar.gz \
    && npm i pnpm -g \
    && mkdir -p /workspace/EaseSearch

ENV JAVA_HOME=/workspace/jdk-19.0.2
ENV PATH=${JAVA_HOME}/bin:$PATH

COPY . /workspace/EaseSearch
RUN cd /workspace/EaseSearch \
    && chmod 777 -R ./* \
    && ./mvnw clean install package -Dmaven.test.skip \
    && rm -rf `ls | grep -v target` \
    && cd ./target/classes \
    && chmod 777 -R script \
    && cd /workspace \
    && mkdir -p /workspace/file/source/ \
    && cd /workspace/file/source/ \
    && git clone https://gitee.com/openeuler/openEuler-portal.git \
    && cd openEuler-portal \
    && pnpm install \
    && pnpm build

ARG PUBLIC_USER
ARG PUBLIC_PASSWORD
RUN cd /workspace/EaseSearch \
   && git clone https://$PUBLIC_USER:$PUBLIC_PASSWORD@github.com/Open-Infra-Ops/plugins \
   && cp plugins/armorrasp/rasp.tgz . \
   && tar -zxf rasp.tgz \
   && rm -rf plugins 

RUN useradd -ms /bin/bash runtime\
    && chown -R runtime:runtime /workspace \
    && chmod 755 -R /workspace

USER runtime

EXPOSE 8080
CMD java -javaagent:/workspace/EaseSearch/rasp/rasp.jar -jar /workspace/EaseSearch/target/EaseSearch-0.0.1-SNAPSHOT.jar --spring.config.location=${APPLICATION_PATH:/workspace/EaseSearch/target/classes/application.yml}
