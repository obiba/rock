#clean up old stuff
# clean old init
if [ -e /etc/init.d/rock ]; then
  service %{name} stop
  chkconfig --del rock
  rm -f /etc/init.d/rock
  systemctl daemon-reload
fi
# legacy folder: move content
if [ ! -L "%{_sharedstatedir}/%{name}/logs" ]; then
  # exits any logs?
  if [ -n "$(ls -A %{_sharedstatedir}/%{name}/logs)" ]; then
	mkdir -p %{_localstatedir}/log/%{name}
  	mv %{_sharedstatedir}/%{name}/logs/* %{_localstatedir}/log/%{name}
  fi
  rm -rf %{_sharedstatedir}/%{name}/logs
fi
# make symlink
if [ ! -e "%{_sharedstatedir}/%{name}/logs" ]; then
  ln -s %{_localstatedir}/log/%{name} %{_sharedstatedir}/%{name}/logs
fi
exit 0
