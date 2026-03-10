#!/bin/bash

# ./gradlew build publishToMavenLocal   # is publish still needed ???
./gradlew build 

java -jar dprcmd/build/libs/dprcmd-standalone.jar

