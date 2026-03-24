#!/bin/bash

# TODO: move logging.properties into jar file
dir=`dirname $0`/../config

./gradlew :dprlib:build :dprcmd:build >&2  && java \
    -Djava.util.logging.config.file=$dir/logging.properties \
    -jar dprcmd/build/libs/dprcmd-standalone.jar "$@"

