#!/bin/sh
# preinst script for rock

getent group adm >/dev/null || groupadd -r adm

$(getent passwd rock >/dev/null)

if [ $? != 0 ]; then
  useradd -r -g nobody -d /var/lib/rock -s /sbin/nologin \
    -c "User for Rock" rock
  # set the correct home directory
  usermod -d /var/lib/rock rock
  # set the group
  usermod -g nobody rock
fi

# stop the service if running
systemctl stop rock

# clean old init
if [ -e /etc/init.d/rock ]; then
  chkconfig --del rock
  rm -f /etc/init.d/rock
  systemctl daemon-reload
fi

exit 0
