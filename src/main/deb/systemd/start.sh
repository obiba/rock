#!/bin/bash

# Check if the Java version is 21
java_version_output=$($JAVA -version 2>&1)
java_version=$(echo "$java_version_output" | grep version | awk '{ print $3 }' | tr -d '"')
if [[ "${java_version:0:2}" != "21" ]]
then
  echo "Java 21 is required, aborting"
	exit 1
fi

$JAVA $JAVA_ARGS -cp "${ROCK_HOME}/conf:${ROCK_DIST}/lib/*" -DROCK_HOME=${ROCK_HOME} -DROCK_DIST=${ROCK_DIST} -DROCK_LOG=${ROCK_LOG} org.springframework.boot.loader.JarLauncher $ROCK_ARGS
