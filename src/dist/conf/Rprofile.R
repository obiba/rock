# ensure HOME system env variable
home <- Sys.getenv("HOME")
if (home == "") {
    Sys.setenv(HOME="/var/lib/rock")
    home <- Sys.getenv("HOME")
}
# install R libraries in a R version independent directory
libPath <- file.path(home, "R", "library")
if (!dir.exists(libPath)) {
  dir.create(libPath, recursive=TRUE)
}
.libPaths(libPath)
# cleanup
rm(home)
rm(libPath)
# Hint for using RSPM(https://packagemanager.posit.co)
# You must often modify the HTTPUserAgent header when using the OS R
# The correct header can be determined by https://docs.posit.co/rspm/admin/serving-binaries/#binary-user-agents
# Sample for RHEL-9
# options(HTTPUserAgent = sprintf("R/%s R (%s)", getRversion(), paste(getRversion(), R.version["platform"], R.version["arch"], R.version["os"])))

# ensure default CRAN repositories
# This will be done in the yml file(/etc/rock/application.yml)
# newline required
