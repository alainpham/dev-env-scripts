# Spring Boot with camel and other useful things iso8583-service 


## To build this project use

```
mvn install
```

## To run this project with Maven use

```
mvn spring-boot:run
```

##Dealing with SSL/TLS

Generate some private keys and truststores

```
keytool -genkey \
    -alias iso8583-service  \
    -storepass password \
    -keyalg RSA \
    -storetype PKCS12 \
    -dname "cn=iso8583-service" \
    -validity 365000 \
    -keystore tls/keystore.p12

keytool -export \
    -alias iso8583-service \
    -rfc \
    -storepass password \
    -keystore tls/broker-keystore.p12 \
    -file tls/iso8583-service.pem

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

keytool -list -storepass password -keystore tls/keystore.p12 -v
keytool -list -storepass password -keystore tls/truststore.p12 -v
```


## To deploy directly on openshift

make sure you have the image streams deployed

```
BASEURL=https://raw.githubusercontent.com/jboss-fuse/application-templates/application-templates-2.1.0.fuse-sb2-780019-redhat-00005

oc create -n openshift -f ${BASEURL}/fis-image-streams.json
oc replace -n openshift -f ${BASEURL}/fis-image-streams.json
```

```
oc create secret generic iso8583-service-tls-secret \
--from-file=keystore.p12=tls/keystore.p12 \
--from-file=truststore.p12=tls/truststore.p12

oc create secret generic iso8583-service-prop-secret \
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
docker stop iso8583-service
docker rm iso8583-service
docker rmi iso8583-service
docker build -t iso8583-service .
docker run -d --net primenet --ip 172.18.0.10 --name iso8583-service -e SPRING_PROFILES_ACTIVE=dev iso8583-service
```

Stop or launch multple instaces

```
NB_CONTAINERS=2
for (( i=0; i<$NB_CONTAINERS; i++ ))
do
   docker stop iso8583-service-$i
   docker rm iso8583-service-$i
done

docker rmi iso8583-service
docker build -t iso8583-service .

for (( i=0; i<$NB_CONTAINERS; i++ ))
do
    docker run -d --net primenet --ip 172.18.0.1$i --name iso8583-service-$i -e SPRING_PROFILES_ACTIVE=dev iso8583-service
done
```

## To release without deploying straight to an ocp cluster

```
mvn  -P ocp package
```

## To deploy using binary build on ocp

```
tar xzvf iso8583-service-ocp.tar.gz
cd iso8583-service
oc apply -f openshift.yml
oc start-build iso8583-service-s2i --from-dir=deploy --follow
```

## Push on dockerhub

```
docker login
docker build -t iso8583-service -f DockerfileCommunity .
docker tag iso8583-service:latest YOUR_REPO/iso8583-service:latest
```