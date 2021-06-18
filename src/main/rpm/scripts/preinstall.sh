getent group %{name} >/dev/null || groupadd -r %{name}
getent passwd %{name} >/dev/null || \
	  useradd -r -g %{name} -d %{_sharedstatedir}/%{name} -s /sbin/nologin \
	  -C "%{name} service user" %{name}
exit 0