# Streaming Application
This module contains the Kafka streaming application created using streaming APIs. It's a java application which can be build in a jar file and same can be deploy.

## Build the application in Jar file:
```
$ mvn package build
```

## Run Application Container
Copy/deploy the jar file in to a volume accessible to your docker environment. For example, copy the jar file on all available swarm nodes e.g. /opt/streamapp/ folder.

Then you can run the command to call the streaming application from the jar file on a container as follow:

Command format is:
docker run --rm -v _**source dir:target dir**_ -w _**target dir**_ java:8 java -cp _**Jar file name**_ _**Application Class Name**_ _**"Kafka Brokers"**_

For example:
```
$ docker run --rm -v /opt/streamapp/:/app -w /app java:8 java -cp streamapp-1.0-SNAPSHOT-standalone.jar com.streamapp.TotalTraffic "172.16.41.216:9092,172.16.41.224:9093,172.16.41.227:9094"
```
