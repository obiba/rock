#'
#' Extracts some system information.
#'
system.info <- function() {
  tryCatch({
    if (!require(parallel)) {
      install.packages("parallel", repos = "https://cloud.r-project.org")
    }

    cores <- parallel::detectCores()

    osName <- Sys.info()[["sysname"]]
    freeMemory <- -1
    if (osName == "Windows") {
      x <- system2("wmic", args =  "OS get FreePhysicalMemory /Value", stdout = TRUE)
      x <- x[grepl("FreePhysicalMemory", x)]
      x <- gsub("FreePhysicalMemory=", "", x, fixed = TRUE)
      x <- gsub("\r", "", x, fixed = TRUE)
      freeMemory <- as.integer(x)
    } else if (osName == 'Linux') {
      x <- system2('free', args='-k', stdout=TRUE)
      # get the available memory, not the free one
      x <- strsplit(x[2], " +")[[1]][7]
      freeMemory <- as.integer(x)
    } else {
      warn("Only supported on Windows and Linux")
    }
    list(cores = cores, freeMemory = freeMemory)
  }, error=function(e){
    list(cores = -1, freeMemory = -1)
  })
}
