# Spring Boot Apache Camel Application for messaging bus testing

This project aims at giving you the tools to test your messaging infrastructure.
Currently it is prepackaged with ActiveMQ Artemis protocol and AMQP clients.
You can extend the project to import dependencies to other brokers and protocols easily and configure them with JMS connection factory settings.

## Run prebuilt package

```
wget https://github.com/alainpham/dev-env-scripts/releases/download/latest/messaging-tester.tar.gz
tar xzvf messaging-tester.tar.gz
```

Configure config config/application.properties using one of the examples provided and run the application

```
cd messaging-tester
java -jar messaging-tester.jar
```

Example running with docker

```
docker run -it --rm --net primenet --ip 172.18.0.10 -e SPRING_PROFILES_ACTIVE=dev-core -e JMS_CONNECTIONFACTORY_DEFAULT=tcp://amqbrokera0:61616 -e THEME=main-dark-blue -e QUEUE_DEFAULTAPP=app.queue-a --name messaging-tester alainpham/messaging-tester:latest

```
with amqp trustall=true
```
docker run -p 8090:8090 -it --rm --net primenet --ip 172.18.0.10 -e SPRING_PROFILES_ACTIVE=dev -e JMS_URI=amqps://interconnect-cluster-a-5671-appdev-amq-interconnect.apps.my-cluster.ocp4.openshift.es:443 -e THEME=main-dark-blue -e QUEUE_DEFAULTAPP=app.queue.a --name messaging-tester alainpham/messaging-tester:latest

docker run -p 8090:8090 -it --rm --net primenet --ip 172.18.0.11 -e SPRING_PROFILES_ACTIVE=dev -e JMS_URI=amqps://interconnect-cluster-a-5671-appdev-amq-interconnect.apps.my-cluster.ocp4.openshift.es:443 -e THEME=main-dark-red -e QUEUE_DEFAULTAPP=app.queue.a -e JMS_CACHE_LEVEL=CACHE_CONNECTION --name messaging-tester alainpham/messaging-tester:latest
```

```
docker run -it --rm --net primenet --ip 172.18.0.10 -e SPRING_PROFILES_ACTIVE=dev-amqp \
    -e JMS_CONNECTIONFACTORY_DEFAULT=amqp://amqbroker:61616 \
    -e THEME=main-dark-blue \
    -e QUEUE_DEFAULTAPP=app.queue.a \
    -e QUEUE_DEFAULTAPP_ALT=app.queue.b \
    -e QUEUE_DEFAULTAPP_SEND=app.queue.a \
    --name messaging-tester alainpham/messaging-tester:latest
```

```
docker run -it --rm -p 8090:8090 -e SPRING_PROFILES_ACTIVE=dev-amqp \
    -e JMS_CONNECTIONFACTORY_DEFAULT=amqps://interconnect-cluster-a-5671-appdev-amq-interconnect.apps.my-cluster.ocp4.openshift.es:443?transport.trustAll=true \
    -e THEME=main-dark-blue \
    -e QUEUE_DEFAULTAPP=app.queue.a \
    -e QUEUE_DEFAULTAPP_ALT=app.queue.b \
    -e QUEUE_DEFAULTAPP_SEND=app.queue.a \
    --name messaging-tester alainpham/messaging-tester:latest
```


## To build this project use

```
mvn install
```

## To run this project with Maven use

```
mvn spring-boot:run
```

## Dealing with SSL/TLS

Useful commands to download public keys from https sni servers 

```
mkdir tls/trusted-certs

serverhost=amq-broker-a-generic-0-svc-rte-amq-messaging-a.apps.cluster-f646.f646.example.opentlc.com
serverport=443

echo -n | openssl s_client -connect $serverhost:$serverport | sed -ne '/-BEGIN CERTIFICATE-/,/-END CERTIFICATE-/p' > tls/trusted-certs/amq-broker.pem
```

Generate some private keys and truststores

```
keytool -genkey \
    -alias messaging-tester  \
    -storepass password \
    -keyalg RSA \
    -storetype PKCS12 \
    -dname "cn=messaging-tester" \
    -validity 365000 \
    -keystore tls/keystore.p12

keytool -export \
    -alias messaging-tester \
    -rfc \
    -storepass password \
    -keystore tls/keystore.p12 \
    -file tls/trusted-certs/messaging-tester.pem


FILES=tls/trusted-certs/*
for f in $FILES
do
    full="${f##*/}"
    extension="${full##*.}"
    filename="${full%.*}"
    echo "importing $full in alias $filename"

    keytool -import \
        -alias $filename \
        -storepass password\
        -storetype PKCS12 \
        -noprompt \
        -keystore tls/truststore.p12 \
        -file $f
done

FILES=tls/trusted-certs/*
for f in $FILES
do
    full="${f##*/}"
    extension="${full##*.}"
    filename="${full%.*}"
    echo "importing $full in alias $filename"

    keytool -import \
        -alias $filename \
        -storepass ""\
        -storetype PKCS12 \
        -noprompt \
        -keystore tls/truststore-nopw.p12 \
        -file $f
done

keytool -list -storepass password -keystore tls/keystore.p12 -v
keytool -list -storepass password -keystore tls/truststore.p12 -v
keytool -list -keystore tls/truststore-nopw.p12 -v
```

turning a binary file into base64 string to be able to put into yaml files

```
base64 -w0 tls/keystore.p12 > tls/keystore.base64
base64 -w0 tls/truststore.p12 > tls/truststore.base64
```

## To deploy directly on openshift

make sure you have the image streams deployed

```
BASEURL=https://raw.githubusercontent.com/jboss-fuse/application-templates/application-templates-2.1.0.fuse-sb2-780019-redhat-00005

oc create -n openshift -f ${BASEURL}/fis-image-streams.json
oc replace -n openshift -f ${BASEURL}/fis-image-streams.json
```

```
oc create secret generic messaging-tester-tls-secret \
--from-file=keystore.p12=tls/keystore.p12 \
--from-file=truststore.p12=tls/truststore.p12

oc create secret generic messaging-tester-prop-secret \
--from-file=application.properties=src/main/resources/application.properties

mvn -P ocp fabric8:deploy fabric8:build
```

## For testing

```
curl http://localhost:8090/camel/api-docs
curl http://localhost:8090/camel/ping
```


## Acces Swagger UI with definition

```
http://localhost:8090/webjars/swagger-ui/index.html?url=/camel/api-docs
```

## Call the ping rest operation
```
curl http://localhost:8090/camel/restsvc/ping
```

## Run local container with specific network and IP address


```
docker stop messaging-tester
docker rm messaging-tester
docker rmi messaging-tester
docker build -t messaging-tester .
docker run -d --net primenet --ip 172.18.0.10 --name messaging-tester -e SPRING_PROFILES_ACTIVE=dev messaging-tester

docker run -it --net primenet --ip 172.18.0.10 --rm --name messaging-tester messaging-tester

docker run -it --net primenet --ip 172.18.0.10 --rm --name messaging-tester-red -e SPRING_PROFILES_ACTIVE=dev -e THEME=main-dark-red messaging-tester


function runtester(){
    docker run -d --rm --net primenet --ip 172.18.0.10 -e SPRING_PROFILES_ACTIVE=dev -e JMS_URI=tcp://amqbrokera0:61617 -e THEME=main-dark-blue --name messaging-testera0 messaging-tester
    docker run -d --rm --net primenet --ip 172.18.0.11 -e SPRING_PROFILES_ACTIVE=dev -e JMS_URI=tcp://amqbrokera1:61617 -e THEME=main-dark-green --name messaging-testera1 messaging-tester
    docker run -d --rm --net primenet --ip 172.18.0.12 -e SPRING_PROFILES_ACTIVE=dev -e JMS_URI=tcp://amqbrokerb0:61617 -e THEME=main-dark-red --name messaging-testerb0 messaging-tester
    docker run -d --rm --net primenet --ip 172.18.0.13 -e SPRING_PROFILES_ACTIVE=dev -e JMS_URI=tcp://amqbrokerb1:61617 -e THEME=main-dark-orange --name messaging-testerb1 messaging-tester
}


function restarttester(){
docker restart artemessagingmis-testera0 messaging-testera1  messaging-testerb0 messaging-testerb1
}

function stoptester(){
docker stop messaging-testera0 messaging-testera1  messaging-testerb0 messaging-testerb1
}
```

Stop or launch multple instaces

```
NB_CONTAINERS=2
for (( i=0; i<$NB_CONTAINERS; i++ ))
do
   docker stop messaging-tester-$i
   docker rm messaging-tester-$i
done

docker rmi messaging-tester
docker build -t messaging-tester .

for (( i=0; i<$NB_CONTAINERS; i++ ))
do
    docker run -d --net primenet --ip 172.18.0.1$i --name messaging-tester-$i -e SPRING_PROFILES_ACTIVE=dev messaging-tester
done
```

## To release without deploying straight to an ocp cluster

```
mvn  -P ocp package
```

## To deploy using binary build on ocp

```
tar xzvf messaging-tester-ocp.tar.gz
cd messaging-tester
oc apply -f openshift.yml
oc start-build messaging-tester-s2i --from-dir=deploy --follow
```

## Publish as community container

```
docker stop messaging-tester
docker rm messaging-tester
docker rmi messaging-tester
docker build -t messaging-tester -f DockerfileCommunity .
docker tag messaging-tester:latest alainpham/messaging-tester:latest
docker push alainpham/messaging-tester:latest
```


## Getting kafka certificates

oc extract secret/event-broker-cluster-ca-cert --keys=ca.crt --to=- >tls/trusted-certs/event-broker-kafka.crt

oc get secret kafka-user -o yaml | grep password | head -1 |  sed -E 's/.*password: (.*)/\1/'  | base64 -d
