#!/bin/bash
if [ -d "/usr/local/docs" ]; then
  rm -rf /usr/local/docs/source/*
  rm -rf /usr/local/docs/target/*
fi

mkdir -p /usr/local/docs/source/
mkdir -p /usr/local/docs/target/

#shellcheck disable=SC2164
cd /usr/local/docs/source
git clone https://${GIT_USERNAME}:${GIT_PASSWORD}@gitee.com/mindspore/mindspore.github.io.git


# shellcheck disable=SC2164
cd /usr/local/docs/source/mindspore.github.io

cp -r /usr/local/docs/source/mindspore.github.io/webapp/public/* /usr/local/docs/target/

cd /usr/local/docs/target/
rm -rf admin
rm -rf allow_sersor







