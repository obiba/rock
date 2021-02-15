# Rock [![Build Status](https://travis-ci.com/obiba/rock.svg?branch=master)](https://travis-ci.com/obiba/rock)

Rock is an [R](http://www.r-project.org/) server with a REST API. It is based on the [Rserve](http://rforge.net/Rserve/) R package and exposes the following REST web services:

* to start/stop and get the status of the R server
* to create and interact with stateful R server sessions

In addition to that Rock aims at being:

* safe, by using authentication and restricting the scope of activities of an R session thanks to [RAppArmor](https://cran.r-project.org/package=RAppArmor)
* scalable, by registering itself in [consul](https://www.consul.io/) service registry and also as an [Opal](https://github.com/obiba/opal) app.

Requires Java8 and [R](http://www.r-project.org/) to be installed with [Rserve](http://rforge.net/Rserve/) package.

See [rockr](https://github.com/obiba/rockr) for an R implementation of a Rock server client.

* Have a bug or a question? Please create an issue on [GitHub](https://github.com/obiba/rock/issues).
* Continuous integration is on [Travis](https://travis-ci.com/obiba/rock).

## REST API

### Authentication

Rock requires to be authenticated. The following roles are defined:

* `administrator`
* `manager`
* `user`

HTTP header for `Basic` authentication is required at each request: the HTTP sessions are stateless whereas the R sessions 
are stateful.

### Cluster Integration

| REST             | Description
| ---------------- | --------------------------
| `GET /_info`     | Get node identification for service discovery
| `GET /_check`    | Get OK when service is up and running

### R Server Administration

| REST             | Description                | Roles
| ---------------- | -------------------------- | -------
| `GET /rserver`   | Get the status of R server | `administrator`, `manager`
| `PUT /rserver`   | Start the R server         | `administrator`, `manager`
| `DELETE /rserver`| Stop the R server          | `administrator`, `manager`
| `GET /rserver/_log?limit=100`   | Tail the R server log file          | `administrator`, `manager`
| `GET /rserver/_version`         | Get the R server version (R object) | `administrator`, `manager`

### R Server Packages Administration

| REST             | Description                | Roles
| ---------------- | -------------------------- | -------
| `GET /rserver/packages`               | Get the installed R packages as a matrix | `administrator`, `manager`
| `DELETE /rserver/packages?name=xxx`   | Delete specified package                | `administrator`, `manager`
| `POST /rserver/packages?name=xxx&manager=<cran\|github\|bioconductor>` | Install a R package | `administrator`, `manager`
| `GET /rserver/package/xxx`            | Get a R package description              | `administrator`, `manager`
| `DELETE /rserver/package/xxx`         | Delete a R package                       | `administrator`, `manager`
| `GET /rserver/packages/_datashield`   | Get the settings of the installed [DataSHIELD](https://www.datashield.ac.uk/) R packages | `administrator`, `manager`

### R Server Usage

| REST             | Description                | Roles
| ---------------- | -------------------------- | -----------------
| `POST /r/sessions`             | Create a R session, requesting subject becomes the owner of the session | `administrator`, `user`
| `GET /r/sessions`              | List the R sessions                    | `administrator`, `manager`, `user` (only own sessions)
| `DELETE /r/sessions`           | Close all R sessions                   | `administrator`, `manager`
| `GET /r/session/<id>`          | Get info about a R session             | `administrator`, session owner
| `POST /r/session/<id>/_assign` | Assign a R expression (`application/x-rscript`) or R data (`application/x-rdata`) in a R session | `administrator`, session owner
| `POST /r/session/<id>/_eval`   | Evaluate a R expression in a R session | `administrator`, session owner
| `POST /r/session/<id>/_upload` | Upload a file in a R session           | `administrator`, session owner
| `GET /r/session/<id>/_download`| Download a file from a R session       | `administrator`, session owner
| `DELETE /r/session/<id>`       | Close a R session                      | `administrator`, session owner

## Usage

On debian systems, R can be installed via `apt`:

```
sudo apt-get install r-base r-cran-rserve
```

It is recommended to install the latest Rserve R package:

```
install.packages('Rserve',,'http://www.rforge.net/')
```

Rock is also available in Docker: [docker-rock](https://github.com/obiba/docker-rock)

## Development

### Start server

```
make all debug
```

### Test server

Requires `curl` and running server.

```
make status
```

See other Makefile targets.

## Mailing list

Have a question? Ask on our mailing list!

obiba-users@googlegroups.com

[http://groups.google.com/group/obiba-users](http://groups.google.com/group/obiba-users)

## License

OBiBa software are open source and made available under the [GPL3 licence](http://www.obiba.org/pages/license/). OBiBa software are free of charge.

## OBiBa acknowledgments

If you are using OBiBa software, please cite our work in your code, websites, publications or reports.

"The work presented herein was made possible using the OBiBa suite (www.obiba.org), a  software suite developed by Maelstrom Research (www.maelstrom-research.org)"
