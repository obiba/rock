skipTests = false
version = 1.0-SNAPSHOT
current_dir = $(shell pwd)
mvn_exec = mvn -Dmaven.test.skip=${skipTests}

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
	curl localhost:6312/rserver
	@echo
	@echo

start:
	curl -X PUT localhost:6312/rserver
	@echo
	@echo

stop:
	curl -X DELETE localhost:6312/rserver
	@echo
	@echo

new-session:
	curl -v -X POST localhost:6312/r/sessions -H 'Content-Type:application/json'

get-session:
	curl -v -X GET localhost:6312/r/session/${id} -H 'Content-Type:application/json'

delete-session:
	curl -v -X DELETE localhost:6312/r/session/${id} -H 'Content-Type:application/json'

