# Spring Boot with camel and other useful things rest-soap-conversion 


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
    -alias rest-soap-conversion  \
    -storepass password \
    -keyalg RSA \
    -storetype PKCS12 \
    -dname "cn=rest-soap-conversion" \
    -validity 365000 \
    -keystore tls/keystore.p12

keytool -export \
    -alias rest-soap-conversion \
    -rfc \
    -storepass password \
    -keystore tls/broker-keystore.p12 \
    -file tls/rest-soap-conversion.pem

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
oc create secret generic rest-soap-conversion-tls-secret \
--from-file=keystore.p12=tls/keystore.p12 \
--from-file=truststore.p12=tls/truststore.p12

oc create secret generic rest-soap-conversion-prop-secret \
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
docker stop rest-soap-conversion
docker rm rest-soap-conversion
docker rmi rest-soap-conversion
docker build -t rest-soap-conversion .
docker run -d --net primenet --ip 172.18.0.10 --name rest-soap-conversion -e SPRING_PROFILES_ACTIVE=dev rest-soap-conversion
```

Stop or launch multple instaces

```
NB_CONTAINERS=2
for (( i=0; i<$NB_CONTAINERS; i++ ))
do
   docker stop rest-soap-conversion-$i
   docker rm rest-soap-conversion-$i
done

docker rmi rest-soap-conversion
docker build -t rest-soap-conversion .

for (( i=0; i<$NB_CONTAINERS; i++ ))
do
    docker run -d --net primenet --ip 172.18.0.1$i --name rest-soap-conversion-$i -e SPRING_PROFILES_ACTIVE=dev rest-soap-conversion
done
```

## To release without deploying straight to an ocp cluster

```
mvn  -P ocp package
```

## To deploy using binary build on ocp

```
tar xzvf rest-soap-conversion-ocp.tar.gz
cd rest-soap-conversion
oc apply -f openshift.yml
oc start-build rest-soap-conversion-s2i --from-dir=deploy --follow
```

## Push on dockerhub

```
docker login
docker build -t rest-soap-conversion -f DockerfileCommunity .
docker tag rest-soap-conversion:latest alainpham/rest-soap-conversion:latest
```