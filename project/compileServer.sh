#!/bin/bash

TARGET="target"

mkdir ${TARGET}
cd ${TARGET}

javac -d ./ -Xlint:deprecation ../src/*
java -Djava.security.policy==../resources/java.policy SecServer

cd ..
rm -r ${TARGET}
