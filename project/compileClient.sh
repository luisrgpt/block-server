#!/bin/bash

TARGET="target"

cd ${TARGET}

java -Djava.security.policy==../resources/java.policy SecClient

cd ..
