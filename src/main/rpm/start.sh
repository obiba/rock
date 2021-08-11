#!/bin/bash

$JAVA $JAVA_ARGS -cp "${ROCK_HOME}/conf:${ROCK_DIST}/lib/*" -DROCK_HOME=${ROCK_HOME} -DROCK_DIST=${ROCK_DIST} -DROCK_LOG=${ROCK_LOG} org.springframework.boot.loader.JarLauncher $ROCK_ARGS
