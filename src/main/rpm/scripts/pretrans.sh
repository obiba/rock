#clean up old stuff
# clean old init
if [ -e /etc/init.d/rock ]; then
  service rock stop
  chkconfig --del rock
  rm -f /etc/init.d/rock
  systemctl daemon-reload
fi
# legacy folder: move content
if [ ! -L "/var/lib/rock/logs" ]; then
  # exits any logs?
  if [ -n "$(ls -A /var/lib/rock/logs)" ]; then
	mkdir -p /var/log/rock
  	mv /var/lib/rock/logs/* /var/log/rock
  fi
  rm -rf /var/lib/rock/logs
fi
# make symlink
if [ ! -e "/var/lib/rock/logs" ]; then
  ln -s /var/log/rock /var/lib/rock/logs
fi
exit 0
