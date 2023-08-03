#!/bin/bash
if [ -d "/workspace/file/target" ]; then
  rm -rf /workspace/file/target/*
fi

mkdir -p /workspace/file/target/zh/
mkdir -p /workspace/file/target/en/

cp -r /workspace/file/source/website-v2/app/zh/information/news /workspace/file/target/zh/
cp -r /workspace/file/source/website-v2/app/zh/information/blog /workspace/file/target/zh/
cp -r /workspace/file/source/website-v2/app/zh/information/activity /workspace/file/target/zh/
cp -r /workspace/file/source/website-v2/app/zh/information/video /workspace/file/target/zh/

cp -r /workspace/file/source/website-v2/app/en/information/news /workspace/file/target/en/
cp -r /workspace/file/source/website-v2/app/en/information/blog /workspace/file/target/en/
cp -r /workspace/file/source/website-v2/app/en/information/activity /workspace/file/target/en/
cp -r /workspace/file/source/website-v2/app/en/information/video /workspace/file/target/en/

cd /workspace/file/source
git clone https://gitee.com/openlookeng/website-docs.git
cp -r /workspace/file/source/website-docs/content/zh/docs/docs/ /workspace/file/target/zh/docs/
cp -r /workspace/file/source/website-docs/content/en/docs/docs/ /workspace/file/target/en/docs/