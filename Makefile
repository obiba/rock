skipTests = false
version = 1.0-SNAPSHOT
current_dir = $(shell pwd)
mvn_exec = mvn -Dmaven.test.skip=${skipTests}
port = 8085

user=administrator
password=password

all: clean install

clean:
	${mvn_exec} clean

install:
	${mvn_exec} install

launch:
	export ROCK_HOME=$(shell pwd)/target/rock-${version}-dist/rock-${version} && \
	cd target/rock-${version}-dist/rock-${version} && \
	chmod +x ./bin/rock && \
	./bin/rock

debug:
	export ROCK_HOME=$(shell pwd)/target/rock-${version}-dist/rock-${version} && \
	export JAVA_OPTS=-agentlib:jdwp=transport=dt_socket,server=y,address=8003,suspend=n && \
	cd target/rock-${version}-dist/rock-${version} && \
	chmod +x ./bin/rock && \
	./bin/rock

deb:
	mvn clean install -Pci-build

log:
	tail -f target/rock-${version}-dist/rock-${version}/logs/rock.log

rlog:
	tail -f target/rock-${version}-dist/rock-${version}/logs/Rserve.log

test: status stop start

status:
	curl --user ${user}:${password} localhost:${port}/rserver
	@echo
	@echo

start:
	curl -X PUT --user ${user}:${password} localhost:${port}/rserver
	@echo
	@echo

stop:
	curl -X DELETE --user ${user}:${password} localhost:${port}/rserver
	@echo
	@echo

_log:
	curl -X GET --user ${user}:${password} -H "Accept: text/plain" localhost:${port}/rserver/_log?limit=10
	@echo
	@echo

packages:
	curl --user ${user}:${password} localhost:${port}/rserver/packages
	@echo
	@echo

packages-datashield:
	curl --user ${user}:${password} localhost:${port}/rserver/packages/_datashield
	@echo
	@echo

package:
	curl --user ${user}:${password} localhost:${port}/rserver/package/${n}
	@echo
	@echo

check:
	curl -v localhost:${port}/_check
	@echo
	@echo

info:
	curl -v localhost:${port}/_info
	@echo
	@echo

sessions:
	curl -X GET --user ${user}:${password} localhost:${port}/r/sessions
	@echo
	@echo

sessions-close:
	curl -X DELETE --user ${user}:${password} localhost:${port}/r/sessions
	@echo
	@echo

session:
	curl -X GET --user ${user}:${password} localhost:${port}/r/session/${sid}
	@echo
	@echo

consul:
	consul agent -dev -advertise=127.0.0.1
