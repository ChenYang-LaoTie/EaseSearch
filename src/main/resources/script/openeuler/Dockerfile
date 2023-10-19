FROM gplane/pnpm as Builder

WORKDIR /

RUN apt update \
    && wget https://download.java.net/java/GA/jdk19.0.2/fdb695a9d9064ad6b064dc6df578380c/7/GPL/openjdk-19.0.2_linux-x64_bin.tar.gz \
    && tar -zxvf openjdk-19.0.2_linux-x64_bin.tar.gz \
    && wget https://repo.huaweicloud.com/apache/maven/maven-3/3.8.1/binaries/apache-maven-3.8.1-bin.tar.gz \
    && tar -zxvf apache-maven-3.8.1-bin.tar.gz \
    && npm i pnpm -g

ENV JAVA_HOME=/jdk-19.0.2
ENV PATH=${JAVA_HOME}/bin:$PATH

ENV MAVEN_HOME=/apache-maven-3.8.1
ENV PATH=${MAVEN_HOME}/bin:$PATH

COPY . /EaseSearch

RUN cd /EaseSearch \
    && mvn clean install package -Dmaven.test.skip

RUN git clone https://gitee.com/openeuler/openEuler-portal.git \
    && git clone https://gitee.com/openeuler/docs.git \
    && cd openEuler-portal \
    && pnpm install \
    && pnpm build 

RUN cp -r jdk-19.0.2 jre



FROM openeuler/openeuler:22.03
ENV searchsystem=openEuler

RUN groupadd -g 1001 easysearch \
    && useradd -u 1001 -g easysearch -s /bin/bash -m easysearch

ENV WORKSPACE=/home/easysearch
ENV SOURCE=${WORKSPACE}/file/source
ENV TARGET=${WORKSPACE}/file/target

WORKDIR ${WORKSPACE}

COPY --chown=easysearch --from=Builder /EaseSearch/target ${WORKSPACE}/target
COPY --chown=easysearch --from=Builder /jre ${WORKSPACE}/jre
COPY --chown=easysearch --from=Builder /openEuler-portal ${SOURCE}/openEuler-portal
COPY --chown=easysearch --from=Builder /docs ${SOURCE}/docs

RUN chmod -x ${WORKSPACE}/target/classes/script

ENV JAVA_HOME=/jre
ENV PATH=${JAVA_HOME}/bin:$PATH

EXPOSE 8080

CMD java -jar ${WORKSPACE}/target/EaseSearch-0.0.1-SNAPSHOT.jar --spring.config.location=${APPLICATION_PATH}