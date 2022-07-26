#!/bin/sh

rm -rf /usr/local/docs/target/*
mkdir -p /usr/local/docs/target/zh/
mkdir -p /usr/local/docs/target/en/

# shellcheck disable=SC2164
cd /usr/local/docs/source/website-v2
git pull
mkdir -p /usr/local/docs/target/zh/
cp -r /usr/local/docs/source/website-v2/web-ui/docs/zh/* /usr/local/docs/target/zh/

mkdir -p /usr/local/docs/target/en/
cp -r /usr/local/docs/source/website-v2/web-ui/docs/en/* /usr/local/docs/target/en/

# shellcheck disable=SC2164
cd /usr/local/docs/source/openEuler-portal
git pull
cp -r /usr/local/docs/source/openEuler-portal/app/zh/showcase /usr/local/docs/target/zh/


# shellcheck disable=SC2164
cd /usr/local/docs/source/docs
git pull
for r in $(git branch -r --list "origin/stable2-*")
do
  b=${r##*origin/stable2-}
  git checkout -b $b $r
  git pull
  mkdir -p /usr/local/docs/target/zh/docs/$b/docs
  mkdir -p /usr/local/docs/target/en/docs/$b/docs
  cp -r /usr/local/docs/source/docs/docs/zh/docs/* /usr/local/docs/target/zh/docs/$b/docs/
  cp -r /usr/local/docs/source/docs/docs/en/docs/* /usr/local/docs/target/en/docs/$b/docs/
done