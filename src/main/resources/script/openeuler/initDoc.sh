#!/bin/bash
if [ -d "/workspace/file/target" ]; then
  rm -rf /workspace/file/target/*
fi

mkdir -p /workspace/file/target/zh/
mkdir -p /workspace/file/target/en/
mkdir -p /workspace/file/target/ru/

if [ ! -d "/workspace/file/source/openEuler-portal" ]; then
 rm -rf /workspace/file/target
 exit
fi

# shellcheck disable=SC2164
cd /workspace/file/source/openEuler-portal

cp -r /workspace/file/source/openEuler-portal/app/.vitepress/dist/zh /workspace/file/target/
cp -r /workspace/file/source/openEuler-portal/app/.vitepress/dist/en /workspace/file/target/
cp -r /workspace/file/source/openEuler-portal/app/.vitepress/dist/ru /workspace/file/target/


rm -rf /workspace/file/target/zh/blog
cp -r /workspace/file/source/openEuler-portal/app/zh/blog /workspace/file/target/zh/
rm -rf /workspace/file/target/zh/news
cp -r /workspace/file/source/openEuler-portal/app/zh/news /workspace/file/target/zh/
rm -rf /workspace/file/target/zh/showcase
cp -r /workspace/file/source/openEuler-portal/app/zh/showcase /workspace/file/target/zh/
cp /workspace/file/source/openEuler-portal/app/.vitepress/dist/zh/showcase/index.html /workspace/file/target/zh/showcase/
rm -rf /workspace/file/target/zh/migration
cp -r /workspace/file/source/openEuler-portal/app/zh/migration /workspace/file/target/zh/

rm -rf /workspace/file/target/en/blog
cp -r /workspace/file/source/openEuler-portal/app/en/blog /workspace/file/target/en/
rm -rf /workspace/file/target/en/news
cp -r /workspace/file/source/openEuler-portal/app/en/news /workspace/file/target/en/
rm -rf /workspace/file/target/en/showcase
cp -r /workspace/file/source/openEuler-portal/app/en/showcase /workspace/file/target/en/
cp /workspace/file/source/openEuler-portal/app/.vitepress/dist/en/showcase/index.html /workspace/file/target/en/showcase/
rm -rf /workspace/file/target/en/migration
cp -r /workspace/file/source/openEuler-portal/app/en/migration /workspace/file/target/en/

rm -rf /workspace/file/target/ru/blog
cp -r /workspace/file/source/openEuler-portal/app/ru/blog /workspace/file/target/ru/
rm -rf /workspace/file/target/ru/news
cp -r /workspace/file/source/openEuler-portal/app/ru/news /workspace/file/target/ru/
rm -rf /workspace/file/target/ru/showcase
cp -r /workspace/file/source/openEuler-portal/app/ru/showcase /workspace/file/target/ru/
cp /workspace/file/source/openEuler-portal/app/.vitepress/dist/ru/showcase/index.html /workspace/file/target/ru/showcase/
rm -rf /workspace/file/target/ru/migration
cp -r /workspace/file/source/openEuler-portal/app/ru/migration /workspace/file/target/ru/


# shellcheck disable=SC2164
cd /workspace/file/source

git clone https://gitee.com/openeuler/docs.git

if [ ! -d "/workspace/file/source/docs" ]; then
 rm -rf /workspace/file/target
 exit
fi

# shellcheck disable=SC2164
cd ./docs
for r in $(git branch -r --list "origin/stable2-*")
do
  b=${r##*origin/stable2-}
  git checkout $r
  mkdir -p /workspace/file/target/zh/docs/$b/docs
  mkdir -p /workspace/file/target/en/docs/$b/docs
  cp -r /workspace/file/source/docs/docs/zh/docs/* /workspace/file/target/zh/docs/$b/docs/
  cp -r /workspace/file/source/docs/docs/en/docs/* /workspace/file/target/en/docs/$b/docs/
done












