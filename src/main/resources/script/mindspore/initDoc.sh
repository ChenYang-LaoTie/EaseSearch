#!/bin/bash
if [ -d "/workspace/file" ]; then
  rm -rf /workspace/file/source/*
  rm -rf /workspace/file/target/*
fi

mkdir -p /workspace/file/source/
mkdir -p /workspace/file/target/

#shellcheck disable=SC2164
cd /workspace/file/source
git clone https://gitee.com/mindspore/website-docs.git

if [ ! -d "/workspace/file/source/website-docs" ]; then
 rm -rf /workspace/file/target
 exit
fi


# shellcheck disable=SC2164
cd /workspace/file/source/website-docs

cp -r /workspace/file/source/website-docs/public/* /workspace/file/target/

# shellcheck disable=SC2164
cd /workspace/file/target/

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
rm -rf mindscience
rm -rf vision

rm -f *

find . -type f -name file_include_\* -exec rm {} \;
find . -type f -name program_listing_file_include_\* -exec rm {} \;

# shellcheck disable=SC2038
find ./ -name _images |xargs rm -rf
# shellcheck disable=SC2038
find ./ -name _modules |xargs rm -rf
# shellcheck disable=SC2038
find ./ -name _sources |xargs rm -rf
# shellcheck disable=SC2038
find ./ -name _static |xargs rm -rf
# shellcheck disable=SC2038
find ./ -name search.html |xargs rm -rf
# shellcheck disable=SC2038
find ./ -name genindex.html |xargs rm -rf
# shellcheck disable=SC2038
find ./ -name py-modindex.html |xargs rm -rf