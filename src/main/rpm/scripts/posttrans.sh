#!/bin/bash
rm -f /usr/share/rock
find /usr/share/rock-* -empty -type d -delete
ln -s /usr/share/rock-* /usr/share/rock
exit 0