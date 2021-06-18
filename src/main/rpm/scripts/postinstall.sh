#!/bin/bash
# Install RServe via R
# prepare R packages install location (not managed by RPM)
mkdir -p %{_sharedstatedir}/%{name}/R/library
Rscript -e "install.packages('Rserve','/var/lib/rock/R/library','http://www.rforge.net/')"
chown -R %{name}:%{name} %{_sharedstatedir}/%{name}/R
chmod -R 750 %{_sharedstatedir}/%{name}/R
# for clean install
%systemd_post %{name}.service
exit 0