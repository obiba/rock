#!/bin/sh

getent group adm >/dev/null || groupadd -r adm

$(getent passwd rock >/dev/null)

if [ $? != 0 ]; then
    useradd -r -g nobody -d /var/lib/rock -s /sbin/nologin \
        -c "User for Rock" rock
else

  # stop the service if running
  if [ $(systemctl list-unit-files "rock.service" | wc -l) -gt 3 ]; then
	  systemctl stop rock
  elif service rock status > /dev/null; then
    if which service >/dev/null 2>&1; then
      service rock stop
    elif which invoke-rc.d >/dev/null 2>&1; then
      invoke-rc.d rock stop
    else
      /etc/init.d/rock stop
    fi
  fi

  # clean old init
  if [ -e /etc/init.d/rock ]; then
    chkconfig --del rock
    rm -f /etc/init.d/rock
  fi

  # set the correct home directory
  usermod -d /var/lib/rock rock

  # set the group
  usermod -g nobody rock
fi

exit 0