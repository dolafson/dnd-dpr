#!/bin/bash

./gradlew --no-build-cache --info -DRunSlowTests=group \
    :dprlib:cleanJvmTest :dprlib:jvmTest \
    --tests "com.vikinghelmet.dnd.dpr.DprTest"

