#!/bin/bash
mkdir -p ${TARGET}/zh/
mkdir -p ${TARGET}/en/

cp -r ${SOURCE}/website-v2/app/zh/information/news ${TARGET}/zh/
cp -r ${SOURCE}/website-v2/app/zh/information/blog ${TARGET}/zh/
cp -r ${SOURCE}/website-v2/app/zh/information/activity ${TARGET}/zh/
cp -r ${SOURCE}/website-v2/app/zh/information/video ${TARGET}/zh/

cp -r ${SOURCE}/website-v2/app/en/information/news ${TARGET}/en/
cp -r ${SOURCE}/website-v2/app/en/information/blog ${TARGET}/en/
cp -r ${SOURCE}/website-v2/app/en/information/activity ${TARGET}/en/
cp -r ${SOURCE}/website-v2/app/en/information/video ${TARGET}/en/

cd ${SOURCE}
git clone https://gitee.com/openlookeng/website-docs.git
cp -r ${SOURCE}/website-docs/content/zh/docs/docs/ ${TARGET}/zh/docs/
cp -r ${SOURCE}/website-docs/content/en/docs/docs/ ${TARGET}/en/docs/