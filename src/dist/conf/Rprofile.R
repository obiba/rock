# ensure HOME system env variable
home <- Sys.getenv("HOME")
if (home == "") {
    Sys.setenv(HOME="/var/lib/rock")
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
options(repos = c("https://cloud.r-project.org", "https://cran.obiba.org"))
# newline required