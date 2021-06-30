systemctl daemon-reload >/dev/null 2>&1 || :
case "$1" in
	0)
    # Package removal, not upgrade
    unlink /usr/share/rock
    # Remove logs and data
    rm -rf /var/lib/rock /var/log/rock /etc/rock /usr/share/rock-*
  ;;
  1)
    # Package upgrade, not removal
    find /usr/share/rock-* -empty -type d -delete
    systemctl try-restart rock.service >/dev/null 2>&1 || :
  ;;
esac