# Build the nsi-client-lib to validate under different JVM.

all:    build

.PHONY: build test clean
build:
	docker run -it --rm --name dds-build \
		-v "$(PWD)":/usr/src/mymaven \
		-v "$(HOME)/.m2":/root/.m2  \
		-w /usr/src/mymaven \
		maven:3.8.1-openjdk-15-slim mvn clean install -DskipTests=true

test:
	docker run -it --rm --name dds-build \
		-v "$(PWD)":/usr/src/mymaven \
		-v "$(HOME)/.m2":/root/.m2  \
		-w /usr/src/mymaven \
		-p 8801:8801 \
		-p 8802:8802 \
		maven:3.8.1-openjdk-15-slim mvn clean install

clean:
	docker run -it --rm --name dds-clean \
		-v "$(PWD)":/usr/src/mymaven \
		-v "$(HOME)/.m2":/root/.m2  \
		-w /usr/src/mymaven \
		maven:3.8.1-openjdk-15-slim mvn clean

docker:

