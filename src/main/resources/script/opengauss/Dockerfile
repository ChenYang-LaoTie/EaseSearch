FROM node

ENV LANG C.UTF-8
ENV LC_ALL C.UTF-8
ENV LANGUAGE C.UTF-8
ENV searchsystem openGauss

RUN apt update \
    && apt install libasm-java \
    && wget http://security.debian.org/debian-security/pool/updates/main/j/json-smart/libjson-smart-java_2.2-2+deb10u1_all.deb \
    && dpkg -i libjson-smart-java_2.2-2+deb10u1_all.deb  \
    && cd /root \
    && wget https://download.java.net/java/GA/jdk19.0.2/fdb695a9d9064ad6b064dc6df578380c/7/GPL/openjdk-19.0.2_linux-x64_bin.tar.gz \
    && tar -zxvf openjdk-19.0.2_linux-x64_bin.tar.gz \
    && mkdir -p /EaseSearch \
    && apt update \
    && npm i pnpm -g

ENV JAVA_HOME=/root/jdk-19.0.2
ENV PATH=${JAVA_HOME}/bin:$PATH

WORKDIR /EaseSearch
COPY . /EaseSearch
RUN chmod 777 -R ./* \
    && ./mvnw clean install package -Dmaven.test.skip \
    && rm -rf `ls | grep -v target` \
    && cd ./target/classes \
    && chmod 777 -R script \
    && mkdir -p /usr/local/docs/source/ \
    && cd /usr/local/docs/source \
    && git clone https://gitee.com/opengauss/blog.git -b v2 \
    && git clone https://gitee.com/opengauss/website.git -b v2  \
    && cp -r ./blog/app/zh/blogs/* ./website/app/zh/blogs/ \
    && cp -r ./blog/app/en/blogs/* ./website/app/en/blogs/ \
    && cd website \
    && pnpm install \
    && pnpm build \
    && cd /EaseSearch/target

EXPOSE 8080
CMD java -jar ./target/EaseSearch-0.0.1-SNAPSHOT.jar --spring.config.location=${APPLICATION_PATH}
