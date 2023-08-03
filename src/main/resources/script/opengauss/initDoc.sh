#!/bin/bash
if [ -d "/workspace/file/target" ]; then
  rm -rf /workspace/file/target/*
fi

mkdir -p /workspace/file/target/zh/
mkdir -p /workspace/file/target/en/

if [ ! -d "/workspace/file/source/website" ]; then
 rm -rf /workspace/file/target
 exit
fi

# shellcheck disable=SC2164
cd /workspace/file/source/website

cp -r /workspace/file/source/website/app/.vitepress/dist/zh /workspace/file/target/
cp -r /workspace/file/source/website/app/.vitepress/dist/en /workspace/file/target/

rm -rf /workspace/file/target/zh/blogs
cp -r /workspace/file/source/website/app/zh/blogs /workspace/file/target/zh/
rm -rf /workspace/file/target/zh/news
cp -r /workspace/file/source/website/app/zh/news /workspace/file/target/zh/
rm -rf /workspace/file/target/zh/events
cp -r /workspace/file/source/website/app/zh/events /workspace/file/target/zh/
rm -rf /workspace/file/target/zh/userPractice
cp -r /workspace/file/source/website/app/zh/userPractice /workspace/file/target/zh/

rm -rf /workspace/file/target/en/blogs
cp -r /workspace/file/source/website/app/en/blogs /workspace/file/target/en/
rm -rf /workspace/file/target/en/news
cp -r /workspace/file/source/website/app/en/news /workspace/file/target/en/
rm -rf /workspace/file/target/en/events
cp -r /workspace/file/source/website/app/en/events /workspace/file/target/en/
rm -rf /workspace/file/target/en/userPractice
cp -r /workspace/file/source/website/app/en/userPractice /workspace/file/target/en/

# shellcheck disable=SC2164
cd /workspace/file/source

git clone https://gitee.com/opengauss/docs.git

if [ ! -d "/workspace/file/source/docs" ]; then
 rm -rf /workspace/file/target
 exit
fi

# shellcheck disable=SC2164
cd ./docs

for r in $(git branch -r --list "origin/*"); do
  b=${r##*origin/}
 # shellcheck disable=SC2170
 # shellcheck disable=SC1073
 # shellcheck disable=SC1072
 # shellcheck disable=SC1020
 # shellcheck disable=SC1009
 # shellcheck disable=SC2053
 if [[ "website" != $b ]] && [[ "HEAD" != $b ]] && [[ "->" != $b ]] && [[ "reconstruct-frozen" != $b ]] && [[ "master-bak" != $b ]] && [[ "website-v2" != $b ]]; then
    git checkout $r
    mkdir -p /workspace/file/target/zh/docs/$b/docs
    mkdir -p /workspace/file/target/en/docs/$b/docs
    cp -r /workspace/file/source/docs/content/zh/docs/* /workspace/file/target/zh/docs/$b/docs/
    cp -r /workspace/file/source/docs/content/en/docs/* /workspace/file/target/en/docs/$b/docs/

    mkdir -p /workspace/file/target/zh/docs/$b-lite/docs
    mkdir -p /workspace/file/target/en/docs/$b-lite/docs
    cp -r /workspace/file/source/docs/content/docs-lite/zh/docs/* /workspace/file/target/zh/docs/$b-lite/docs/
    cp -r /workspace/file/source/docs/content/docs-list/en/docs/* /workspace/file/target/en/docs/$b-lite/docs/

 fi
done



