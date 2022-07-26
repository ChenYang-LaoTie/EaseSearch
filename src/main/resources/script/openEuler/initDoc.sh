#!/bin/sh
if [ -d "/usr/local/docs" ]; then
  rm -rf /usr/local/docs/source/*
  rm -rf /usr/local/docs/target/*
fi
mkdir -p /usr/local/docs/source/
mkdir -p /usr/local/docs/target/zh/
mkdir -p /usr/local/docs/target/en/

# shellcheck disable=SC2164
cd /usr/local/docs/source
git clone https://gitee.com/openeuler/website-v2.git

mkdir -p /usr/local/docs/target/zh/
cp -r /usr/local/docs/source/website-v2/web-ui/docs/zh/* /usr/local/docs/target/zh/

mkdir -p /usr/local/docs/target/en/
cp -r /usr/local/docs/source/website-v2/web-ui/docs/en/* /usr/local/docs/target/en/

git clone https://gitee.com/openeuler/openEuler-portal.git
cp -r /usr/local/docs/source/openEuler-portal/app/zh/showcase /usr/local/docs/target/zh/

git clone https://gitee.com/openeuler/docs.git

# shellcheck disable=SC2164
cd ./docs
for r in $(git branch -r --list "origin/stable2-*")
do
  b=${r##*origin/stable2-}
  git checkout -b $b $r
  mkdir -p /usr/local/docs/target/zh/docs/$b/docs
  mkdir -p /usr/local/docs/target/en/docs/$b/docs
  cp -r /usr/local/docs/source/docs/docs/zh/docs/* /usr/local/docs/target/zh/docs/$b/docs/
  cp -r /usr/local/docs/source/docs/docs/en/docs/* /usr/local/docs/target/en/docs/$b/docs/
done












