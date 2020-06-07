#!/usr/bin/env sh

APP_HOME="`pwd -P`"

git submodule foreach git reset --hard HEAD
git submodule foreach git clean -f
git submodule update --remote
git submodule foreach git checkout develop
git submodule foreach git pull origin develop
