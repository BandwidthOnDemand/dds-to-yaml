#! /bin/sh

TRUSTSTORE=/Users/hacksaw/pkcs/truststore.p12
KEYSTORE=/Users/hacksaw/pkcs/keystore.p12
STORETYPE="PKCS12"
PASSWORD="changeit"

java \
        -Xmx512m -Djava.net.preferIPv4Stack=true  \
        -Dcom.sun.xml.bind.v2.runtime.JAXBContextImpl.fastBoot=true \
        -Djavax.net.ssl.trustStore=$TRUSTSTORE \
        -Djavax.net.ssl.trustStorePassword=$PASSWORD \
        -Djavax.net.ssl.trustStoreType=$STORETYPE \
        -Djavax.net.ssl.keyStore=$KEYSTORE \
        -Djavax.net.ssl.keyStorePassword=$PASSWORD \
        -Djavax.net.ssl.keyStoreType=$STORETYPE \
	-Dlog4j.configurationFile=config/log4j.xml \
	-Dspring.config.name=secure-application \
	--illegal-access=debug \
        -jar "target/dds-yaml-1.0.0.jar" \
	-pidFile "./yaml.pid" 
