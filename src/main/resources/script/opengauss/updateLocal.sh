#!/bin/bash

rm -rf /usr/local/docs/target/*
mkdir -p /usr/local/docs/target/zh/
mkdir -p /usr/local/docs/target/en/

# shellcheck disable=SC2164
cd /usr/local/docs/source/gaussv2
git pull
#pnpm install
#pnpm build
#
#cp -r /usr/local/docs/source/gaussv2/app/.vitepress/dist/zh /usr/local/docs/target/
#cp -r /usr/local/docs/source/gaussv2/app/.vitepress/dist/en /usr/local/docs/target/

rm -rf /usr/local/docs/target/zh/blogs
cp -r /usr/local/docs/source/gaussv2/app/zh/blogs /usr/local/docs/target/zh/
rm -rf /usr/local/docs/target/zh/news
cp -r /usr/local/docs/source/gaussv2/app/zh/news /usr/local/docs/target/zh/
rm -rf /usr/local/docs/target/zh/events
cp -r /usr/local/docs/source/gaussv2/app/zh/events /usr/local/docs/target/zh/

rm -rf /usr/local/docs/target/en/blogs
cp -r /usr/local/docs/source/gaussv2/app/en/blogs /usr/local/docs/target/en/
rm -rf /usr/local/docs/target/en/news
cp -r /usr/local/docs/source/gaussv2/app/en/news /usr/local/docs/target/en/
rm -rf /usr/local/docs/target/en/events
cp -r /usr/local/docs/source/gaussv2/app/en/events /usr/local/docs/target/en/

# shellcheck disable=SC2164
cd /usr/local/docs/source/docs
for r in $(git branch -r --list "origin/*"); do
  b=${r##*origin/}
 # shellcheck disable=SC2170
 # shellcheck disable=SC1073
 # shellcheck disable=SC1072
 # shellcheck disable=SC1020
 # shellcheck disable=SC1009
 # shellcheck disable=SC2053
 if [[ "website" != $b ]] && [[ "HEAD" != $b ]] && [[ "->" != $b ]]; then
    git checkout $r
    mkdir -p /usr/local/docs/target/zh/docs/$b/docs
    mkdir -p /usr/local/docs/target/en/docs/$b/docs
    cp -r /usr/local/docs/source/docs/content/zh/docs/* /usr/local/docs/target/zh/docs/$b/docs/
    cp -r /usr/local/docs/source/docs/content/en/docs/* /usr/local/docs/target/en/docs/$b/docs/

    mkdir -p /usr/local/docs/target/zh/docs/$b-lite/docs
    mkdir -p /usr/local/docs/target/en/docs/$b-lite/docs
    cp -r /usr/local/docs/source/docs/content/docs-lite/zh/docs/* /usr/local/docs/target/zh/docs/$b-lite/docs/
    cp -r /usr/local/docs/source/docs/content/docs-list/en/docs/* /usr/local/docs/target/en/docs/$b-lite/docs/
 fi
done

