#!/bin/bash

# loop in folders to update scd4j version and execute ./gradlew wrapper

if [ $# -eq 0 ]
  then
    echo "You must supplie scd4j version"
    exit 1
fi

for f in *;do [[ -d "$f" && ! -L "$f" && ! $f == ".*" && ! $f == "gradle" ]] || continue; echo '-----------------'; cd $f; pwd; sed -i "18 c\    id \"com.datamaio.scd4j\" version \"$1\"" build.gradle; ./gradlew wrapper; cd ..; pwd; done
