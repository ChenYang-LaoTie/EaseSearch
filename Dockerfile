FROM openeuler/openeuler:22.03 as BUILDER

RUN cd / \
    && yum install -y wget \
    && wget https://download.oracle.com/java/17/archive/jdk-17.0.7_linux-x64_bin.tar.gz \
    && tar -zxvf jdk-17.0.7_linux-x64_bin.tar.gz \
    && wget https://repo.huaweicloud.com/apache/maven/maven-3/3.8.1/binaries/apache-maven-3.8.1-bin.tar.gz \
    && tar -zxvf apache-maven-3.8.1-bin.tar.gz \
    && yum install -y git

COPY . /EaseSearch-search

ENV JAVA_HOME=/jdk-17.0.7
ENV PATH=${JAVA_HOME}/bin:$PATH

ENV MAVEN_HOME=/apache-maven-3.8.1
ENV PATH=${MAVEN_HOME}/bin:$PATH

RUN cd /EaseSearch-search \
    && mvn clean install package -Dmaven.test.skip

RUN cp -r jdk-17.0.7 jre


FROM openeuler/openeuler:22.03

RUN groupadd -g 1001 easysearch \
    && useradd -u 1001 -g easysearch -s /bin/bash -m easysearch

ENV WORKSPACE=/home/easysearch

WORKDIR ${WORKSPACE}

COPY --chown=easysearch --from=Builder /EaseSearch-search/target ${WORKSPACE}/target

RUN echo "umask 027" >> /home/easysearch/.bashrc
RUN source /home/easysearch/.bashrc
RUN chmod 550 -R /home/easysearch

RUN dnf install -y wget \
    && wget https://download.bell-sw.com/java/17.0.9+11/bellsoft-jre17.0.9+11-linux-amd64.tar.gz -O jre-17.0.9.tar.gz \
    && tar -zxvf jre-17.0.9.tar.gz

ENV JAVA_HOME=${WORKSPACE}/jre-17.0.9
ENV PATH=${JAVA_HOME}/bin:$PATH
ENV LANG="C.UTF-8"
ENV NO_ID_USER=anonymous

EXPOSE 8080

USER easysearch

CMD java -jar ${WORKSPACE}/target/EaseSearch-0.0.1-SNAPSHOT.jar --spring.config.location=${APPLICATION_PATH}