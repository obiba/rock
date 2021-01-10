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

HTTP header for `Basic` authentication is required at each request.

### R Server Administration

| REST             | Description                | Roles
| ---------------- | -------------------------- | -------
| `GET /rserver`   | Get the status of R server | `administrator`, `manager`
| `PUT /rserver`   | Start the R server         | `administrator`, `manager`
| `DELETE /rserver`| Stop the R server          | `administrator`, `manager`

### R Server Usage

| REST             | Description                | Roles
| ---------------- | -------------------------- | -----------------
| `POST /r/sessions`| Create a R session, requesting subject becomes the owner of the session        | `administrator`, `user`
| `GET /r/sessions`| List the R sessions        | `administrator`, `manager`, `user` (only own sessions)
| `GET /r/sessions`| Close all R sessions       | `administrator`, `manager`
| `GET /r/session/<id>`          | Get info about a R session             | `administrator`, session owner
| `POST /r/session/<id>/_assign` | Assign a R expression in a R session   | `administrator`, session owner
| `POST /r/session/<id>/_eval`   | Evaluate a R expression in a R session | `administrator`, session owner
| `POST /r/session/<id>/_upload` | Upload a file in a R session           | `administrator`, session owner
| `GET /r/session/<id>/_download`| Download a file from a R session       | `administrator`, session owner
| `DELETE /r/session/<id>`       | Close a R session                      | `administrator`, session owner

## Usage

On debian systems, R and Rserve can be installed via `apt`:

```
sudo apt-get install r-base r-cran-rserve
```

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
