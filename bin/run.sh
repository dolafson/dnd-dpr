#!/bin/bash

./gradlew :dprlib:build :dprcmd:build >&2  && java \
    -jar dprcmd/build/libs/dprcmd-standalone.jar "$@"

