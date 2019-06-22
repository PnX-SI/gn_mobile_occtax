#!/usr/bin/env sh

APP_HOME="`pwd -P`"

cd $APP_HOME/commons
git checkout -- .

cd $APP_HOME/viewpager
git checkout -- .

cd $APP_HOME/maps
git checkout -- .

cd $APP_HOME/gn_mobile_core
git pull origin develop

cd $APP_HOME/gn_mobile_maps
git pull origin develop

cd $APP_HOME
git submodule update --remote
git submodule foreach git checkout develop
git submodule foreach git pull origin develop
