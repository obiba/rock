#!/bin/sh
# postinst script for rock

set -e

[ -r /etc/default/rock ] && . /etc/default/rock

# symlink to new install
rm -f /usr/share/rock
new_release="$(ls -t /usr/share/ | grep rock | head -1)"
ln -s /usr/share/${new_release} /usr/share/rock

# symlink to conf
if [ ! -e /var/lib/rock/conf ]; then
  ln -s /etc/rock /var/lib/rock/conf
fi


# legacy folder: move content
if [ ! -L /var/lib/rock/logs ]; then
  mv /var/lib/rock/logs/* /var/log/rock
  rmdir /var/lib/rock/logs
fi
# make symlink
if [ ! -e /var/lib/rock/logs ]; then
  ln -s /var/log/rock /var/lib/rock/logs
fi

# Install RServe via R
# prepare R packages install location (not managed by RPM)
mkdir -p /var/lib/rock/R/library
Rscript -e "install.packages('Rserve','/var/lib/rock/R/library','http://www.rforge.net/')"
chown -R rock:adm /var/lib/rock/R
chmod -R 750 /var/lib/rock/R

# start rock
systemctl daemon-reload
systemctl enable rock
systemctl start rock

exit 0
