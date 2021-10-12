#!/bin/bash
rm -f /usr/share/rock
find /usr/share/rock-* -empty -type d -delete
ln -s /usr/share/rock-* /usr/share/rock
# for update from System-V
systemctl preset rock.service
systemctl start rock.service
exit 0