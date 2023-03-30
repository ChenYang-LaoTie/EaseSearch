FROM node

ENV LANG C.UTF-8
ENV LC_ALL C.UTF-8
ENV LANGUAGE C.UTF-8
ENV searchsystem openGauss

RUN mkdir -p /EaseSearch \
    && apt update \
    && apt-get install -y openjdk-17-jdk \
    && npm i pnpm -g

WORKDIR /EaseSearch
COPY . /EaseSearch
RUN chmod 777 -R ./* \
    && ./mvnw clean install package -Dmaven.test.skip \
    && cd ./target/classes \
    && chmod 777 -R script \
    && mkdir -p /usr/local/docs/source/ \
    && cd /usr/local/docs/source \
    && git clone -b https://gitee.com/opengauss/website.git v2 \
    && cd website \
    && pnpm install \
    && pnpm build \
    && cd /EaseSearch/target

EXPOSE 8080
CMD java -jar ./target/EaseSearch-0.0.1-SNAPSHOT.jar