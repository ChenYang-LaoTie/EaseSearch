FROM gplane/pnpm as Builder

WORKDIR /

RUN apt update \
    && wget https://download.oracle.com/java/17/archive/jdk-17.0.7_linux-x64_bin.tar.gz \
    && tar -zxvf jdk-17.0.7_linux-x64_bin.tar.gz \
    && wget https://repo.huaweicloud.com/apache/maven/maven-3/3.8.1/binaries/apache-maven-3.8.1-bin.tar.gz \
    && tar -zxvf apache-maven-3.8.1-bin.tar.gz \
    && npm i pnpm -g

ENV JAVA_HOME=/jdk-17.0.7
ENV PATH=${JAVA_HOME}/bin:$PATH

ENV MAVEN_HOME=/apache-maven-3.8.1
ENV PATH=${MAVEN_HOME}/bin:$PATH

COPY . /EaseSearch

RUN cd /EaseSearch \
    && mvn clean install package -Dmaven.test.skip

ENV SOURCE=/docs-file/source
ENV TARGET=/docs-file/target

RUN mkdir -p ${SOURCE} \
    && cd ${SOURCE} \
    && git clone https://gitee.com/mindspore/website-docs.git

RUN cd /EaseSearch/target/classes/script/mindspore \
    && chmod +x initDoc.sh \
    && ./initDoc.sh 

RUN cp -r jdk-17.0.7 jre

FROM openeuler/openeuler:22.03
ENV searchsystem=mindspore

RUN groupadd -g 1001 easysearch \
    && useradd -u 1001 -g easysearch -s /bin/bash -m easysearch

ENV WORKSPACE=/home/easysearch
ENV SOURCE=${WORKSPACE}/file/source
ENV TARGET=${WORKSPACE}/file/target

WORKDIR ${WORKSPACE}

COPY --chown=easysearch --from=Builder /EaseSearch/target ${WORKSPACE}/target
COPY --chown=easysearch --from=Builder /jre ${WORKSPACE}/jre
COPY --chown=easysearch --from=Builder /docs-file/target ${WORKSPACE}/file/target

ENV JAVA_HOME=${WORKSPACE}/jre
ENV PATH=${JAVA_HOME}/bin:$PATH

EXPOSE 8080

USER easysearch

CMD java -jar ${WORKSPACE}/target/EaseSearch-0.0.1-SNAPSHOT.jar --spring.config.location=${APPLICATION_PATH}