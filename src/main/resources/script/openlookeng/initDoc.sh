#!/bin/bash
if [ -d "/usr/local/docs/target" ]; then
  rm -rf /usr/local/docs/target/*
fi

mkdir -p /usr/local/docs/target/zh/
mkdir -p /usr/local/docs/target/en/

cp -r /usr/local/docs/source/website-v2/app/zh/information/news /usr/local/docs/target/zh/
cp -r /usr/local/docs/source/website-v2/app/zh/information/blog /usr/local/docs/target/zh/
cp -r /usr/local/docs/source/website-v2/app/zh/information/activity /usr/local/docs/target/zh/
cp -r /usr/local/docs/source/website-v2/app/zh/information/video /usr/local/docs/target/zh/

cp -r /usr/local/docs/source/website-v2/app/en/information/news /usr/local/docs/target/en/
cp -r /usr/local/docs/source/website-v2/app/en/information/blog /usr/local/docs/target/en/
cp -r /usr/local/docs/source/website-v2/app/en/information/activity /usr/local/docs/target/en/
cp -r /usr/local/docs/source/website-v2/app/en/information/video /usr/local/docs/target/en/

cd /usr/local/docs/source
git clone https://gitee.com/openlookeng/website-docs.git
cp -r /usr/local/docs/source/website-docs/content/zh/docs/docs/ /usr/local/docs/target/zh/docs/
cp -r /usr/local/docs/source/website-docs/content/en/docs/docs/ /usr/local/docs/target/en/docs/