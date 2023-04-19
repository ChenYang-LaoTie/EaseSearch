#!/bin/bash
if [ -d "/usr/local/docs" ]; then
  rm -rf /usr/local/docs/source/*
  rm -rf /usr/local/docs/target/*
fi

mkdir -p /usr/local/docs/source/
mkdir -p /usr/local/docs/target/

#shellcheck disable=SC2164
cd /usr/local/docs/source
git clone https://${GIT_USERNAME}:${GIT_PASSWORD}@gitee.com/mindspore/website-docs.git


# shellcheck disable=SC2164
cd /usr/local/docs/source/website-docs

cp -r /usr/local/docs/source/website-docs/public/* /usr/local/docs/target/

# shellcheck disable=SC2164
cd /usr/local/docs/target/

# shellcheck disable=SC2035

rm -rf admin
rm -rf allow_sensor
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
rm -rf tutorial
rm -rf video

rm -f *

# shellcheck disable=SC2038
find ./ -name _images |xargs rm -rf
# shellcheck disable=SC2038
find ./ -name _modules |xargs rm -rf
# shellcheck disable=SC2038
find ./ -name _sources |xargs rm -rf
# shellcheck disable=SC2038
find ./ -name _static |xargs rm -rf



