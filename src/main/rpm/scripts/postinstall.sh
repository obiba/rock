#!/bin/sh
# postinst script for rock
#

set -e

installOrUpdate() {
    # RServer file structure on Debian
    # /etc/rock: configuration
    # /usr/share/rock: executable
    # /var/lib/rock: data runtime
    # /var/log: logs

    rm -f /usr/share/rock
    new_release="$(ls -t /usr/share/ |grep rock|head -1)"
    ln -s /usr/share/${new_release} /usr/share/rock

    if [ ! -e /var/lib/rock/conf ] ; then
      ln -s /etc/rock /var/lib/rock/conf
    fi

    # move installed r packages to the new library location
    rlibs=/var/lib/rock/R
    mkdir -p $rlibs
    if [ ! -e $rlibs/library ] ; then
      mkdir -p $rlibs/library
      if [ -e $rlibs/x* ] ; then
        rpkgs=`find $rlibs/x* -maxdepth 2 -mindepth 2 -type d`
        for pkg in $rpkgs
        do
          pkg_name=`basename $pkg`
          if [ ! -e $rlibs/library/$pkg_name ] ; then
            mv $pkg $rlibs/library
          fi
        done
      fi
    fi

    chown -R rock:adm /var/lib/rock /var/log/rock /etc/rock /tmp/rock
    chmod -R 750      /var/lib/rock /var/log/rock /etc/rock/ /tmp/rock
    find /etc/rock/ -type f | xargs chmod 640

    # if upgrading to 2.0, delete old log4j config
    if [ -f "/etc/rock/log4j.properties" ]; then
      mv /etc/rock/log4j.properties /etc/rock/log4j.properties.old
    fi

    # Install RServe via R
    Rscript -e "install.packages('Rserve', repos='https://cran.r-project.org')"

    # auto start on reboot
    chkconfig --add rock

    # start rock
    echo "### You can start rock service by executing:"
    echo "sudo /etc/init.d/rock start"

    exit 0
}

# summary of how this script can be called:
#        * <postinst> `configure' <most-recently-configured-version>
#        * <old-postinst> `abort-upgrade' <new version>
#        * <conflictor's-postinst> `abort-remove' `in-favour' <package>
#          <new-version>
#        * <postinst> `abort-remove'
#        * <deconfigured's-postinst> `abort-deconfigure' `in-favour'
#          <failed-install-package> <version> `removing'
#          <conflicting-package> <version>
# for details, see http://www.debian.org/doc/debian-policy/ or
# the debian-policy package

NAME=rock

[ -r /etc/default/$NAME ] && . /etc/default/$NAME

case "$1" in
  1)
    installOrUpdate
  ;;

  2)
    installOrUpdate
  ;;

  *)
    echo "postinst called with unknown argument \`$1'" >&2
    exit 1
  ;;
esac

exit 0
