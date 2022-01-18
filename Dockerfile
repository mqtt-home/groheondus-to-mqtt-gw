# ---- Prod ----
FROM openjdk:18-jdk-alpine
LABEL maintainer="Philipp Arndt <2f.mail@gmx.de>"
LABEL version="1.0"
LABEL description="Grohe ondus to mqtt gateway"

RUN mkdir /opt/app
WORKDIR /opt/app
COPY src/de.rnd7.groheondustomqtt/target/groheondus-to-mqtt-gw.jar .

RUN sed -i 's/jdk.security.caDistrustPolicies=SYMANTEC_TLS/#jdk.security.caDistrustPolicies=SYMANTEC_TLS/'\
 /usr/lib/jvm/java-1.8-openjdk/jre/lib/security/java.security

CMD java -jar ./groheondus-to-mqtt-gw.jar /var/lib/groheondus-to-mqtt-gw/config.json
