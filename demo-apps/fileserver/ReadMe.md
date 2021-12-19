# Spring Boot with camel and other useful things fileserver 


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
    -alias fileserver  \
    -storepass password \
    -keyalg RSA \
    -storetype PKCS12 \
    -dname "cn=fileserver" \
    -validity 365000 \
    -keystore tls/keystore.p12

keytool -export \
    -alias fileserver \
    -rfc \
    -storepass password \
    -keystore tls/broker-keystore.p12 \
    -file tls/fileserver.pem

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
BASEURL=https://raw.githubusercontent.com/jboss-fuse/application-templates/application-templates-2.1.0.fuse-sb2-790047-redhat-00005

oc create -n openshift -f ${BASEURL}/fis-image-streams.json
oc replace -n openshift -f ${BASEURL}/fis-image-streams.json
```

```
oc create secret generic fileserver-tls-secret \
--from-file=keystore.p12=tls/keystore.p12 \
--from-file=truststore.p12=tls/truststore.p12

oc create secret generic fileserver-prop-secret \
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
docker stop fileserver
docker rm fileserver
docker rmi fileserver
docker build -t fileserver .
docker run -d --net primenet --ip 172.18.0.10 --name fileserver -e SPRING_PROFILES_ACTIVE=dev fileserver
```

Stop or launch multple instaces

```
NB_CONTAINERS=2
for (( i=0; i<$NB_CONTAINERS; i++ ))
do
   docker stop fileserver-$i
   docker rm fileserver-$i
done

docker rmi fileserver
docker build -t fileserver .

for (( i=0; i<$NB_CONTAINERS; i++ ))
do
    docker run -d --net primenet --ip 172.18.0.1$i --name fileserver-$i -e SPRING_PROFILES_ACTIVE=dev fileserver
done
```

## To release without deploying straight to an ocp cluster

```
mvn  -P ocp package
```

## To deploy using binary build on ocp

```
tar xzvf fileserver-ocp.tar.gz
cd fileserver
oc apply -f openshift.yml
oc start-build fileserver-s2i --from-dir=deploy --follow
```

## Push on dockerhub

```
docker login
docker build -t fileserver -f DockerfileCommunity .
docker tag fileserver:latest alainpham/fileserver:latest
```


## How to 

put files 

curl --request POST -H "Content-Type: application/octet-stream"  --data-binary @700MB.xml http://localhost:8090/camel/upload?file=test.zip  -v

get files 

curl http://thiswebsite/camel/download?file=fileName  -o fileName