#!/bin/bash
# Install RServe via R
# prepare R packages install location (not managed by RPM)
mkdir -p /var/lib/rock/R/library
Rscript -e "install.packages('Rserve','/var/lib/rock/R/library','http://www.rforge.net/')"
chown -R rock:rock /var/lib/rock/R
chmod -R 750 /var/lib/rock/R
# for clean install
if [ $1 -eq 1 ] ; then
  # Initial installation
  systemctl preset rock.service >/dev/null 2>&1 || :
fi
exit 0