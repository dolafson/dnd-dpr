#!/bin/bash

./gradlew --no-build-cache --info -DRunSlowTests=true \
    :dprlib:cleanJvmTest :dprlib:jvmTest \
    --tests "com.vikinghelmet.dnd.dpr.DprTest"

