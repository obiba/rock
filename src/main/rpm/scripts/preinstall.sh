getent group rock >/dev/null || groupadd -r rock
getent passwd rock >/dev/null || \
	  useradd -r -g rock -d /var/lib/rock -s /sbin/nologin \
	  -C "rock service user" rock
exit 0