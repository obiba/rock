#!/bin/bash

getPidFile() {
   while getopts ":p:" opt; do
     case $opt in
       p)
         echo $OPTARG
         return 0
         ;;
     esac
   done

   return 1
}

# OS specific support.
cygwin=false
case "`uname`" in
CYGWIN*) cygwin=true;;
esac

if [ -z "$JAVA_OPTS" ]
then
  if [ ! -z "$JAVA_ARGS" ]
  then
    JAVA_OPTS=$JAVA_ARGS
  else
    # Set default JAVA_OPTS
    JAVA_OPTS="-Xmx2G -XX:MaxPermSize=128M"
  fi

  export JAVA_OPTS
fi

# The directory containing the rock shell script
ROCK_BIN_DIR=`dirname $0`
# resolve links - $0 may be a softlink
ROCK_DIST=$(readlink -f $ROCK_BIN_DIR/..)

export ROCK_DIST

echo "JAVA_OPTS=$JAVA_OPTS"
echo "ROCK_HOME=$ROCK_HOME"
echo "ROCK_DIST=$ROCK_DIST"
echo "ROCK_LOG=$ROCK_LOG"

if [ -z "$ROCK_HOME" ]
then
  echo "ROCK_HOME not set."
  exit 2;
fi

if $cygwin; then
  # For Cygwin, ensure paths are in UNIX format before anything is touched
  [ -n "$ROCK_DIST" ] && ROCK_BIN=`cygpath --unix "$ROCK_DIST"`
  [ -n "$ROCK_HOME" ] && ROCK_HOME=`cygpath --unix "$ROCK_HOME"`

  # For Cygwin, switch paths to Windows format before running java
  export ROCK_DIST=`cygpath --absolute --windows "$ROCK_DIST"`
  export ROCK_HOME=`cygpath --absolute --windows "$ROCK_HOME"`
fi

# Java 6 supports wildcard classpath entries
# http://download.oracle.com/javase/6/docs/technotes/tools/solaris/classpath.html
CLASSPATH=$ROCK_HOME/conf:"$ROCK_DIST/lib/*"
if $cygwin; then
  CLASSPATH=$ROCK_HOME/conf;"$ROCK_DIST/lib/*"
fi

[ -e "$ROCK_HOME/logs" ] || mkdir "$ROCK_HOME/logs"

JAVA_DEBUG=-agentlib:jdwp=transport=dt_socket,server=y,address=8000,suspend=n

# Add $JAVA_DEBUG to this line to enable remote JVM debugging (for developers)
exec java $JAVA_OPTS -cp "$CLASSPATH" -DROCK_HOME="${ROCK_HOME}" \
  -DROCK_DIST=${ROCK_DIST} -DROCK_LOG=${ROCK_LOG}  org.obiba.rock.Application "$@" >$ROCK_LOG/stdout.log 2>&1 &

# On CentOS 'daemon' function does not initialize the pidfile
pidfile=$(getPidFile $@)

if [ ! -z "$pidfile" ]; then
  echo $! > $pidfile
fi