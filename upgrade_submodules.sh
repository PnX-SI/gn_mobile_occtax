#!/usr/bin/env sh

APP_HOME="`pwd -P`"

cd $APP_HOME/commons
git checkout -- version.properties
cd $APP_HOME/viewpager
git checkout -- version.properties
cd $APP_HOME
git submodule update --remote
