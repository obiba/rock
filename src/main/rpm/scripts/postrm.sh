#!/bin/sh
# postrm script for rock

set -e

case "$1" in
	0)
    userdel -f rock || true
    unlink /usr/share/rock
    rm -rf /run/rock /var/log/rock /tmp/rock
  ;;
esac

exit 0
