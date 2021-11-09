#!/bin/bash
# Install RServe via R
# prepare R packages install location (not managed by RPM)
mkdir -p /var/lib/rock/R/library
# Rserve package
Rscript -e ".libPaths(.Library.site) ; if (!suppressWarnings(require(Rserve, quietly = TRUE))) { install.packages('Rserve', repos = 'https://www.rforge.net/') }"
# utility packages
Rscript -e ".libPaths(.Library.site) ; if (!suppressWarnings(require(unixtools, quietly = TRUE))) { install.packages('unixtools', repos = 'https://www.rforge.net/') }"
Rscript -e ".libPaths(.Library.site) ; if (!suppressWarnings(require(remotes, quietly = TRUE))) { install.packages('remotes', repos = 'https://cloud.r-project.org/') }"
Rscript -e ".libPaths(.Library.site) ; if (!suppressWarnings(require(BiocManager, quietly = TRUE))) { install.packages('BiocManager', repos = 'https://cloud.r-project.org/') }"
# additional packages
Rscript -e ".libPaths(.Library.site) ; if (!suppressWarnings(require(sqldf, quietly = TRUE))) { install.packages('sqldf', repos = 'https://cloud.r-project.org/') }"
Rscript -e ".libPaths(.Library.site) ; if (!suppressWarnings(require(haven, quietly = TRUE))) { install.packages('haven', repos = 'https://cloud.r-project.org/') }"
Rscript -e ".libPaths(.Library.site) ; if (!suppressWarnings(require(labelled, quietly = TRUE))) { install.packages('labelled', repos = 'https://cloud.r-project.org/') }"

chown -R rock:rock /var/lib/rock/R
chmod -R 750 /var/lib/rock/R
# make symlink
if [ ! -e /var/lib/rock/logs ]; then
  mkdir -p /var/log/rock
  ln -s /var/log/rock /var/lib/rock/logs
fi
if [ ! -e /var/lib/rock/conf ] ; then
  ln -s /etc/rock /var/lib/rock/conf
fi
exit 0