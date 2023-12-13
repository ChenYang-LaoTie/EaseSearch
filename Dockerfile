FROM openeuler/openeuler:22.03 as BUILDER

RUN cd / \
    && yum install -y wget \
    && wget https://mirrors.tuna.tsinghua.edu.cn/Adoptium/17/jdk/x64/linux/OpenJDK17U-jdk_x64_linux_hotspot_17.0.9_9.tar.gz \
    && tar -zxvf OpenJDK17U-jdk_x64_linux_hotspot_17.0.9_9.tar.gz \
    && wget https://repo.huaweicloud.com/apache/maven/maven-3/3.8.1/binaries/apache-maven-3.8.1-bin.tar.gz \
    && tar -zxvf apache-maven-3.8.1-bin.tar.gz \
    && yum install -y git

COPY . /EaseSearch-search

ENV JAVA_HOME=/jdk-17.0.9+9
ENV PATH=${JAVA_HOME}/bin:$PATH

ENV MAVEN_HOME=/apache-maven-3.8.1
ENV PATH=${MAVEN_HOME}/bin:$PATH

RUN cd /EaseSearch-search \
    && mvn clean install package -Dmaven.test.skip

ARG PUBLIC_USER
ARG PUBLIC_PASSWORD
RUN cd / \
    && git clone https://$PUBLIC_USER:$PUBLIC_PASSWORD@github.com/Open-Infra-Ops/plugins \
    && cp plugins/armorrasp/rasp.tgz . \
    && tar -zxf rasp.tgz \
    && chown -R root:root rasp && chmod 755 -R rasp \
    && rm -rf plugins

RUN cp -r jdk-17.0.9+9 jre


FROM openeuler/openeuler:22.03

RUN yum update -y \
    && yum install -y shadow

RUN groupadd -g 1001 easysearch \
    && useradd -u 1001 -g easysearch -s /bin/bash -m easysearch

ENV WORKSPACE=/home/easysearch

WORKDIR ${WORKSPACE}

COPY --chown=easysearch --from=Builder /EaseSearch-search/target ${WORKSPACE}/target
COPY --chown=easysearch --from=Builder /rasp ${WORKSPACE}/rasp

RUN echo "umask 027" >> /home/easysearch/.bashrc \
    && source /home/easysearch/.bashrc \
    && chmod 550 -R /home/easysearch

RUN dnf install -y wget \
    && wget https://mirrors.tuna.tsinghua.edu.cn/Adoptium/17/jre/x64/linux/OpenJDK17U-jre_x64_linux_hotspot_17.0.9_9.tar.gz -O jre-17.0.9.tar.gz \
    && tar -zxvf jre-17.0.9.tar.gz

ENV JAVA_HOME=${WORKSPACE}/jdk-17.0.9+9-jre
ENV PATH=${JAVA_HOME}/bin:$PATH
ENV LANG="C.UTF-8"
ENV NO_ID_USER=anonymous

EXPOSE 8080

USER easysearch

CMD java -javaagent:${WORKSPACE}/rasp/rasp.jar -jar ${WORKSPACE}/target/EaseSearch-0.0.1-SNAPSHOT.jar --spring.config.location=${APPLICATION_PATH}