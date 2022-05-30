# Rock [![Build Status](https://app.travis-ci.com/obiba/rock.svg?branch=master)](https://app.travis-ci.com/github/obiba/rock)

Rock is an [R](http://www.r-project.org/) server with a REST API. It is based on the [Rserve](http://rforge.net/Rserve/) R package and exposes the following REST web services:

* to start/stop and get the status of the R server
* to create and interact with stateful R server sessions

In addition to that Rock aims at being:

* safe, by using authentication and restricting the scope of activities of an R session thanks to [RAppArmor](https://cran.r-project.org/package=RAppArmor)
* scalable, by registering itself in [consul](https://www.consul.io/) service registry and also as an [Opal](https://github.com/obiba/opal) app.

Requires Java and [R](http://www.r-project.org/) to be installed with [Rserve](http://rforge.net/Rserve/) package.

* Have a bug or a question? Please create an issue on [GitHub](https://github.com/obiba/rock/issues).
* Continuous integration is on [Travis](https://travis-ci.com/obiba/rock).

## Documentation

Full documentation is available at: [Rock Documentation](https://rockdoc.obiba.org)

## Usage

Rock is available as Debian/RPM packages and as a Docker image. See installation instructions: [Rock Installation](https://rockdoc.obiba.org/en/latest/admin/installation.html)

See [rockr](https://github.com/obiba/rockr) for an R implementation of a Rock server client.

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

[Subscribe to obiba-users@googlegroups.com](mailto:obiba-users+subscribe@googlegroups.com)

[http://groups.google.com/group/obiba-users](http://groups.google.com/group/obiba-users)

## License

OBiBa software are open source and made available under the [GPL3 licence](http://www.obiba.org/pages/license/). OBiBa software are free of charge.

## OBiBa acknowledgments

If you are using OBiBa software, please cite our work in your code, websites, publications or reports.

"The work presented herein was made possible using the OBiBa suite (www.obiba.org), a  software suite developed by Maelstrom Research (www.maelstrom-research.org)"
