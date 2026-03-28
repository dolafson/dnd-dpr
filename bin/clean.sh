#!/bin/bash
# TODO: bury this logic into build.gradle.kts ...

./gradlew clean

# during build, json files get copied from shared/resources to build-specific src dir ...
# see also .gitignore

/bin/rm -f dprcmd/src/main/resources/*.json
/bin/rm -f composeApp/src/commonMain/composeResources/files/*.json

