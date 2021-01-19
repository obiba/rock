skipTests = false
version = 1.0-SNAPSHOT
current_dir = $(shell pwd)
mvn_exec = mvn -Dmaven.test.skip=${skipTests}

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
	curl --user ${user}:${password} localhost:6312/rserver
	@echo
	@echo

start:
	curl -X PUT --user ${user}:${password} localhost:6312/rserver
	@echo
	@echo

stop:
	curl -X DELETE --user ${user}:${password} localhost:6312/rserver
	@echo
	@echo

packages:
	curl --user ${user}:${password} localhost:6312/rserver/packages
	@echo
	@echo

check:
	curl -v localhost:6312/_check
	@echo
	@echo

check2:
	curl -v --user ${user}:${password} localhost:6312/_check
	@echo
	@echo

sessions:
	curl -X GET --user ${user}:${password} localhost:6312/r/sessions
	@echo
	@echo

sessions-close:
	curl -X DELETE --user ${user}:${password} localhost:6312/r/sessions
	@echo
	@echo

session:
	curl -X GET --user ${user}:${password} localhost:6312/r/session/${sid}
	@echo
	@echo

consul:
	consul agent -dev -advertise=127.0.0.1
