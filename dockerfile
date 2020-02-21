FROM openjdk:8-jre
MAINTAINER Maximiliano Vega <maxi@prinkipia.com.ar>

ENTRYPOINT ["/usr/bin/java", "-jar", "/usr/share/speedlayer/speedlayer.jar"]

# Add Maven dependencies (not shaded into the artifact; Docker-cached)
ADD target/lib           /usr/share/speedlayer/lib
# Add the service itself
ARG JAR_FILE
ADD target/${JAR_FILE} /usr/share/speedlayer/speedlayer.jar