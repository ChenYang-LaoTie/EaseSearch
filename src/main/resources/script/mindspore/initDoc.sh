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

# shellcheck disable=SC2164
cd /usr/local/docs/target/

# shellcheck disable=SC2035
rm -f *
rm -rf admin
rm -rf allow_sersor
rm -rf api
rm -rf apicc
rm -rf cla
rm -rf commonJs
rm -rf doc
rm -rf images
rm -rf lib
rm -rf more
rm -rf pdfjs
rm -rf pic
rm -rf security
rm -rf statement
rm -rf statics
rm -rf video







