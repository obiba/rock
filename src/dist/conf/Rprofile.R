# install R libraries in a R version independent directory
if (!file.exists("/var/lib/rock/R/library")) {
  dir.create("/var/lib/rock/R/library", recursive=TRUE)
}
.libPaths("/var/lib/rock/R/library")
# for Pandoc
Sys.setenv(HOME="/var/lib/rock")
# newline required