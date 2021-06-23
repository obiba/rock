#!/bin/bash
# for update from System-V
if [ $1 -eq 0 ] ; then
  # Package removal, not upgrade
  systemctl --no-reload disable rock.service > /dev/null 2>&1 || :
  systemctl stop rock.service > /dev/null 2>&1 || :
fi
exit 0