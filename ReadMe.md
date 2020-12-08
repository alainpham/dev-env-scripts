

# TOC

- [TOC](#toc)
- [Purpose of this repo](#purpose-of-this-repo)
- [Prereqs](#prereqs)
- [Hosts /etc/hosts](#hosts-etchosts)
- [Portainer](#portainer)
- [Nexus](#nexus)
- [Databases](#databases)
  - [Mysql Database](#mysql-database)
  - [Oracle DB](#oracle-db)
  - [Couchbase](#couchbase)
  - [Infinispan](#infinispan)
  - [Enterprise image of Datagrid](#enterprise-image-of-datagrid)
  - [Postgres Database](#postgres-database)
  - [Elastic & Kibana](#elastic--kibana)
- [Messaging](#messaging)
  - [Artemis](#artemis)
  - [AMQ Broker (enterprise version)](#amq-broker-enterprise-version)
    - [simplest variant no ssl no custom broker.xml](#simplest-variant-no-ssl-no-custom-brokerxml)
    - [Generate keystores and truststores](#generate-keystores-and-truststores)
    - [With SSL no custom tweaks](#with-ssl-no-custom-tweaks)
    - [!!!USE THIS!!! : SSL with custom Broker.xml : MOST flexible confguration](#use-this--ssl-with-custom-brokerxml--most-flexible-confguration)
      - [AMQBROKER](#amqbroker)
        - [How many replicas and clusters](#how-many-replicas-and-clusters)
        - [Draft](#draft)
        - [TLS Stuff](#tls-stuff)
        - [Broker creation](#broker-creation)
        - [Broker removal](#broker-removal)
  - [Interconnect (enterprise version)](#interconnect-enterprise-version)
  - [Kafka](#kafka)
  - [Kafdrop](#kafdrop)
  - [Debezium Change Data Capture](#debezium-change-data-capture)
- [Monitoring](#monitoring)
  - [Prometheus](#prometheus)
  - [Grafana](#grafana)
- [Management](#management)
  - [Apicurio Schema Registry](#apicurio-schema-registry)
- [Application Servers](#application-servers)
  - [EAP](#eap)

# Purpose of this repo

This is a script to setup what I commonly use in a dev environement to build PoCs. It allows you to mainly build/run docker images on your workstation to avoid having to provision a whole Kubernetes cluster for dev purposes. It requires much less resources than a full blown container platform.

# Prereqs

You should have docker installed with a dedicated.
Create a network called primenet

```
docker network create --driver=bridge --subnet=172.18.0.0/16 --gateway=172.18.0.1 primenet 
```

# Hosts /etc/hosts

This is to have some static name resolution docker containers we run locally

```
172.18.0.40 portainer
172.18.0.41 nexus

172.18.0.50 mysql
172.18.0.51 oracle
172.18.0.52 couchbase
172.18.0.53 infinispan
172.18.0.54 datagrid
172.18.0.55 postgres
172.18.0.56 elastic
172.18.0.57 kibana

172.18.0.60 artemis
172.18.0.61 zookeeper
172.18.0.62 kafka
172.18.0.64 dbz


172.18.0.70 prometheus
172.18.0.71 grafana

172.18.0.80 schemareg

172.18.0.90 eap

172.18.0.100 amqbrokera0
172.18.0.101 amqbrokera1

172.18.0.110 amqbrokerb0
172.18.0.111 amqbrokerb1

```

# Portainer

```
docker run -d --name=portainer --net primenet --ip 172.18.0.40 -v /var/run/docker.sock:/var/run/docker.sock portainer/portainer:linux-amd64-1.24.1
```

# Nexus

```

docker run --name nexus \
    -d --net primenet --ip 172.18.0.41 \
	sonatype/nexus3:3.28.1
```

# Databases

## Mysql Database

```
docker run -e MYSQL_ROOT_PASSWORD=password -e MYSQL_DATABASE=mysqldb -e MYSQL_USER=user -e MYSQL_PASSWORD=password -d --net primenet --ip 172.18.0.50 --name mysql mysql:8.0.19
```

Run this as root user for change data capture later on 

```
mysql -u root mysqldb
```

```
GRANT SELECT, RELOAD, SHOW DATABASES, REPLICATION SLAVE, REPLICATION CLIENT ON *.* TO 'user';
GRANT ALL PRIVILEGES ON mysqldb.* TO 'user'@'%';
alter user 'user' identified with mysql_native_password by 'password';
flush privileges;
```

## Oracle DB

```
docker run --name oracle \
	-e ORACLE_SID=ORCLCDB \
	-e ORACLE_PDB=ORCLPDB1 \
	-e ORACLE_PWD=password \
	-e ORACLE_CHARACTERSET=AL32UTF8 \
    -d --net primenet --ip 172.18.0.51 \
	oracle/database:12.2.0.1-ee
```

Connect as root user and run

```
alter session set "_ORACLE_SCRIPT"=true;
CREATE USER "USER" IDENTIFIED BY "password"  
DEFAULT TABLESPACE "USERS"
TEMPORARY TABLESPACE "TEMP";
GRANT "DBA" TO "USER";
```

## Couchbase

```
docker run \
    -d --name couchbase  \
    -d --net primenet --ip 172.18.0.52 \
    couchbase:enterprise-6.5.1

```

Goto http://couchbase:8091/ for admin console

## Infinispan

```

docker stop infinispan
docker rm infinispan

docker run \
    -e USER="user" -e PASS="password" \
    -d --name infinispan  \
    -d --net primenet --ip 172.18.0.53 \
    infinispan/server:11.0.0.Final-2
```

## Enterprise image of Datagrid
```
docker run \
    -e USER="user" -e PASS="password" \
    -d --name datagrid  \
    -d --net primenet --ip 172.18.0.54 \
    registry.redhat.io/datagrid/datagrid-8-rhel8:latest
```

## Postgres Database

```
docker run \
    -e POSTGRES_USER="user" -e POSTGRES_PASSWORD="password" -e POSTGRES_DB="db" \
    -d --name postgres  \
    -d --net primenet --ip 172.18.0.55 \
    postgres:12.3
```

## Elastic & Kibana


```
docker run \
    -e discovery.type=single-node \
    -d --name elastic  \
    -d --net primenet --ip 172.18.0.56 \
    elasticsearch:7.8.0
```

Ports : 9200 and 9300
goto http://elastic:9200/

```

docker stop kibana
docker rm kibana

docker run \
    -e "ELASTICSEARCH_HOSTS=http://elastic:9200" \
    -e "monitoring.ui.container.elasticsearch.enabled=false" \
    -d --name kibana  \
    --net primenet --ip 172.18.0.57 \
    kibana:7.8.0
```

got to http://kibana:5601

# Messaging

## Artemis

```
docker stop artemis
docker rm artemis
docker rmi artemis-broker:2.13.0-alpine

cd artemis
docker build -t artemis-broker:2.13.0-alpine .
cd ..


docker run -d --name artemis --net primenet --ip 172.18.0.60  \
  -e ARTEMIS_USERNAME=artemis \
  -e ARTEMIS_PASSWORD=password \
  -e ENABLE_JMX_EXPORTER=true \
  artemis-broker:2.13.0-alpine
```

## AMQ Broker (enterprise version)


### simplest variant no ssl no custom broker.xml

```
docker run \
    -e AMQ_USER="adm" \
    -e AMQ_PASSWORD="password" \
    -e AMQ_ROLE="admin" \
    -e AMQ_NAME="amqbroker" \
    -e AMQ_TRANSPORTS="openwire,amqp,stomp,mqtt,hornetq" \
    -e AMQ_QUEUES="app.queue" \
    -e AMQ_ADDRESSES="app.addr" \
    -e AMQ_GLOBAL_MAX_SIZE="100 gb" \
    -e AMQ_REQUIRE_LOGIN="false" \
    -e AMQ_ENABLE_METRICS_PLUGIN="true" \
    -e AMQ_JOURNAL_TYPE="nio" \
    -e AMQ_DATA_DIR="/opt/amq/data" \
    -e AMQ_DATA_DIR_LOGGING="true" \
    -e AMQ_CLUSTERED="false" \
    -e AMQ_REPLICAS="0" \
    -e AMQ_CLUSTER_USER="amq-cluster-user" \
    -e AMQ_CLUSTER_PASSWORD="password" \
    -e AMQ_KEYSTORE_TRUSTSTORE_DIR="/etc/amq-secret-volume" \
    -e AMQ_TRUSTSTORE="broker-truststore.p12" \
    -e AMQ_TRUSTSTORE_PASSWORD="password" \
    -e AMQ_KEYSTORE="broker-keystore.p12" \
    -e AMQ_KEYSTORE_PASSWORD="password" \
    -e AMQ_SSL_PROVIDER="JDK" \
    -e BROKER_XML="$(cat amqbroker/broker.xml)" \
    -d --name amqbroker  \
    -d --net primenet --ip 172.18.0.65 \
    registry.redhat.io/amq7/amq-broker:latest
```


### Generate keystores and truststores

```
keytool -genkey \
    -alias amqbrokera  \
    -storepass password \
    -keyalg RSA \
    -storetype PKCS12 \
    -dname "cn=amqbrokera" \
    -validity 365000 \
    -keystore amqbroker/tls/amqbrokera-keystore.p12

keytool -genkey \
    -alias amqbrokerb  \
    -storepass password \
    -keyalg RSA \
    -storetype PKCS12 \
    -dname "cn=amqbrokerb" \
    -validity 365000 \
    -keystore amqbroker/tls/amqbrokerb-keystore.p12

keytool -export \
    -alias amqbrokera \
    -rfc \
    -storepass password \
    -keystore amqbroker/tls/amqbrokera-keystore.p12 \
    -file amqbroker/tls/amqbrokera_public_cert.pem

keytool -export \
    -alias amqbrokerb \
    -rfc \
    -storepass password \
    -keystore amqbroker/tls/amqbrokerb-keystore.p12 \
    -file amqbroker/tls/amqbrokerb_public_cert.pem

openssl pkcs12 -in amqbroker/tls/amqbrokera-keystore.p12 -password pass:password -clcerts -nokeys -out amqbroker/tls/amqbrokera_public_cert_openssl.pem
openssl pkcs12 -in amqbroker/tls/amqbrokera-keystore.p12 -password pass:password -nodes -nocerts -out amqbroker/tls/amqbrokera_private_key.key

openssl pkcs12 -in amqbroker/tls/amqbrokerb-keystore.p12 -password pass:password -clcerts -nokeys -out amqbroker/tls/amqbrokerb_public_cert_openssl.pem
openssl pkcs12 -in amqbroker/tls/amqbrokerb-keystore.p12 -password pass:password -nodes -nocerts -out amqbroker/tls/amqbrokerb_private_key.key

keytool -import \
    -alias amqbrokera \
    -storepass password\
    -storetype PKCS12 \
    -noprompt \
    -keystore amqbroker/tls/client-truststore.p12 \
    -file amqbroker/tls/amqbrokera_public_cert.pem

keytool -import \
    -alias amqbrokerb \
    -storepass password\
    -storetype PKCS12 \
    -noprompt \
    -keystore amqbroker/tls/client-truststore.p12 \
    -file amqbroker/tls/amqbrokerb_public_cert.pem

cp amqbroker/tls/client-truststore.p12 amqbroker/tls/amqbrokera-truststore.p12
cp amqbroker/tls/client-truststore.p12 amqbroker/tls/amqbrokerb-truststore.p12

keytool -list -storepass password -keystore amqbroker/tls/amqbrokera-keystore.p12 -v
keytool -list -storepass password -keystore amqbroker/tls/amqbrokerb-keystore.p12 -v
keytool -list -storepass password -keystore amqbroker/tls/client-truststore.p12 -v
keytool -list -storepass password -keystore amqbroker/tls/amqbrokera-truststore.p12 -v
keytool -list -storepass password -keystore amqbroker/tls/amqbrokerb-truststore.p12 -v

```

### With SSL no custom tweaks
```
docker run \
    -e AMQ_USER="adm" \
    -e AMQ_PASSWORD="password" \
    -e AMQ_ROLE="admin" \
    -e AMQ_NAME="amqbroker" \
    -e AMQ_TRANSPORTS="openwire,amqp,stomp,mqtt,hornetq" \
    -e AMQ_QUEUES="app.queue" \
    -e AMQ_ADDRESSES="app.addr" \
    -e AMQ_GLOBAL_MAX_SIZE="100 gb" \
    -e AMQ_REQUIRE_LOGIN="false" \
    -e AMQ_ENABLE_METRICS_PLUGIN="true" \
    -e AMQ_JOURNAL_TYPE="nio" \
    -e AMQ_DATA_DIR="/opt/amq/data" \
    -e AMQ_DATA_DIR_LOGGING="true" \
    -e AMQ_CLUSTERED="false" \
    -e AMQ_REPLICAS="0" \
    -e AMQ_CLUSTER_USER="amq-cluster-user" \
    -e AMQ_CLUSTER_PASSWORD="password" \
    -e AMQ_KEYSTORE_TRUSTSTORE_DIR="/etc/amq-secret-volume" \
    -e AMQ_TRUSTSTORE="broker-truststore.p12" \
    -e AMQ_TRUSTSTORE_PASSWORD="password" \
    -e AMQ_KEYSTORE="broker-keystore.p12" \
    -e AMQ_KEYSTORE_PASSWORD="password" \
    -e AMQ_SSL_PROVIDER="JDK" \
    -d --name amqbroker  \
    -d --net primenet --ip 172.18.0.65 \
    -v "$(pwd)"/amqbroker/tls:/etc/amq-secret-volume:ro \
    registry.redhat.io/amq7/amq-broker:latest
```

### !!!USE THIS!!! : SSL with custom Broker.xml : MOST flexible confguration

#### AMQBROKER

##### How many replicas and clusters

```
export AMQ_CLUSTERS=(a b)
export AMQ_REPLICAS_NB=2

declare -A upstream
upstream=( [a]=b [b]=a)
export upstream
```

##### Draft

```
offset=0
for cluster in ${AMQ_CLUSTERS[@]}
do
    export AMQ_NAME_SUFFIX=$cluster
    export AMQ_MULTICASTPORT=$((9876 + $offset))
    export AMQ_INTERFACE_IP_PREFIX=172.18.0.1${offset}
    echo "DO STUFF for : $AMQ_NAME_SUFFIX, $AMQ_MULTICASTPORT, $AMQ_INTERFACE_IP_PREFIX"
    offset=$((offset+1))
done
```

##### TLS Stuff

```

offset=0
for cluster in ${AMQ_CLUSTERS[@]}
do
    export AMQ_NAME_SUFFIX=$cluster
    export AMQ_MULTICASTPORT=$((9876 + $offset))
    export AMQ_INTERFACE_IP_PREFIX=172.18.0.1${offset}

    x=0
    while [ $x -lt $AMQ_REPLICAS_NB ]
    do
        export AMQ_CLUSTER_NAME=amqbroker${AMQ_NAME_SUFFIX}
        export AMQ_NAME=${AMQ_CLUSTER_NAME}${x}
        keytool -genkey \
            -alias ${AMQ_NAME}  \
            -storepass password \
            -keyalg RSA \
            -storetype PKCS12 \
            -dname "cn=${AMQ_NAME}" \
            -validity 365000 \
            -keystore amqbroker/tls/${AMQ_NAME}-keystore.p12

        keytool -export \
            -alias ${AMQ_NAME} \
            -rfc \
            -storepass password \
            -keystore amqbroker/tls/${AMQ_NAME}-keystore.p12 \
            -file amqbroker/tls/trusted-certs/${AMQ_NAME}.pem

        # openssl pkcs12 -in amqbroker/tls/${AMQ_NAME}-keystore.p12 -password pass:password -clcerts -nokeys -out amqbroker/tls/trusted-certs/${AMQ_NAME}.pem
        openssl pkcs12 -in amqbroker/tls/${AMQ_NAME}-keystore.p12 -password pass:password -nodes -nocerts -out amqbroker/tls/${AMQ_NAME}.key

        x=$(( $x + 1 ))
    done

    offset=$((offset+1))
done

echo "Generate client keys"

keytool -genkey \
    -alias amqclient  \
    -storepass password \
    -keyalg RSA \
    -storetype PKCS12 \
    -dname "cn=amqclient" \
    -validity 365000 \
    -keystore amqbroker/tls/keystore.p12

keytool -export \
    -alias amqclient \
    -rfc \
    -storepass password \
    -keystore amqbroker/tls/keystore.p12 \
    -file amqbroker/tls/trusted-certs/amqclient.pem

FILES=amqbroker/tls/trusted-certs/*
rm amqbroker/tls/truststore.p12
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
        -keystore amqbroker/tls/truststore.p12 \
        -file $f
done

keytool -list -storepass password -keystore amqbroker/tls/truststore.p12
```

##### Broker creation

```

offset=0
for cluster in ${AMQ_CLUSTERS[@]}
do
    export AMQ_NAME_SUFFIX=$cluster
    export AMQ_MULTICASTPORT=$((9876 + $offset))
    export AMQ_INTERFACE_IP_PREFIX=172.18.0.1${offset}

    x=0
    while [ $x -lt $AMQ_REPLICAS_NB ]
    do
        export AMQ_CLUSTER_NAME=amqbroker${AMQ_NAME_SUFFIX}
        export AMQ_NAME=${AMQ_CLUSTER_NAME}${x}
        export AMQ_JOURNAL_TYPE="nio" 
        export AMQ_DATA_DIR="/opt/amq/data" 
        export AMQ_KEYSTORE_TRUSTSTORE_DIR="/etc/amq-secret-volume" 
        export AMQ_TRUSTSTORE="truststore.p12" 
        export AMQ_TRUSTSTORE_PASSWORD="password" 
        export AMQ_KEYSTORE="${AMQ_NAME}-keystore.p12" 
        export AMQ_KEYSTORE_PASSWORD="password" 
        export AMQ_SSL_PROVIDER="JDK" 
        export AMQ_SSL_NEED_CLIENT_AUTH="true"
        export AMQ_MIRROR_HOST=tcp://amqbroker${upstream[${AMQ_NAME_SUFFIX}]}${x}:61617
        export AMQ_INTERFACE_IP=${AMQ_INTERFACE_IP_PREFIX}${x}
        export AMQ_CLUSTER_USER=cluster-user
        export AMQ_CLUSTER_PASSWORD=password
        
        export AMQ_JOURNAL_TYPE_UPPER=$(echo $AMQ_JOURNAL_TYPE | tr [:lower:] [:upper:])

        envsubst '\
            $AMQ_CLUSTER_NAME,\
            $AMQ_NAME,\
            $AMQ_JOURNAL_TYPE_UPPER,\
            $AMQ_DATA_DIR,\
            $AMQ_KEYSTORE_TRUSTSTORE_DIR,\
            $AMQ_TRUSTSTORE,\
            $AMQ_TRUSTSTORE_PASSWORD,\
            $AMQ_KEYSTORE,\
            $AMQ_KEYSTORE_PASSWORD,\
            $AMQ_SSL_PROVIDER,\
            $AMQ_SSL_NEED_CLIENT_AUTH, \
            $AMQ_MIRROR_HOST,\
            $AMQ_INTERFACE_IP,\
            $AMQ_CLUSTER_USER,\
            $AMQ_CLUSTER_PASSWORD,\
            $AMQ_MULTICASTPORT,\
            ' < amqbroker/broker-template.xml > amqbroker/${AMQ_NAME}.xml
        
        envsubst '\
            $AMQ_CLUSTER_NAME,\
            ' < amqbroker/jgroups-file-template.xml > amqbroker/jgroups/$AMQ_CLUSTER_NAME-ping.xml

        mkdir -p amqbroker/jgroups/$AMQ_CLUSTER_NAME-pingdir
        chmod -R 777 amqbroker/jgroups

        docker create \
            -e AMQ_USER="admin" \
            -e AMQ_PASSWORD="password" \
            -e AMQ_ROLE="admin" \
            -e AMQ_NAME=$AMQ_NAME \
            -e AMQ_REQUIRE_LOGIN="false" \
            -e AMQ_JOURNAL_TYPE=$AMQ_JOURNAL_TYPE \
            -e AMQ_DATA_DIR=$AMQ_DATA_DIR \
            -e AMQ_DATA_DIR_LOGGING="true" \
            -e AMQ_KEYSTORE_TRUSTSTORE_DIR=$AMQ_KEYSTORE_TRUSTSTORE_DIR \
            -e AMQ_TRUSTSTORE=$AMQ_TRUSTSTORE \
            -e AMQ_TRUSTSTORE_PASSWORD=$AMQ_TRUSTSTORE_PASSWORD \
            -e AMQ_KEYSTORE=$AMQ_KEYSTORE \
            -e AMQ_KEYSTORE_PASSWORD=$AMQ_KEYSTORE_PASSWORD \
            -e AMQ_SSL_PROVIDER=$AMQ_SSL_PROVIDER \
            -e BROKER_XML="$(cat amqbroker/${AMQ_NAME}.xml)" \
            --name ${AMQ_NAME}  \
            --net primenet --ip ${AMQ_INTERFACE_IP} \
            -v "$(pwd)"/amqbroker/tls:/etc/amq-secret-volume:ro \
            -v "$(pwd)"/amqbroker/jgroups:/jgroups:rw \
            registry.redhat.io/amq7/amq-broker:latest

        docker cp amqbroker/jgroups/${AMQ_CLUSTER_NAME}-ping.xml ${AMQ_NAME}:/opt/amq/conf/jgroups-ping.xml

        docker start ${AMQ_NAME}
        x=$(( $x + 1 ))
    done

    offset=$((offset+1))
done
```

##### Broker removal
```
offset=0
for cluster in ${AMQ_CLUSTERS[@]}
do
    export AMQ_NAME_SUFFIX=$cluster
    export AMQ_MULTICASTPORT=$((9876 + $offset))
    export AMQ_INTERFACE_IP_PREFIX=172.18.0.1${offset}
   
    export AMQ_CLUSTER_NAME=amqbroker${AMQ_NAME_SUFFIX}

    x=0
    while [ $x -lt $AMQ_REPLICAS_NB ]
    do
        export AMQ_NAME=${AMQ_CLUSTER_NAME}${x}
       
        docker stop $AMQ_NAME
        docker rm $AMQ_NAME

        x=$(( $x + 1 ))
    done
    
    rm -rf amqbroker/jgroups/$AMQ_CLUSTER_NAME-pingdir

    offset=$((offset+1))
done
```

## Interconnect (enterprise version)

```
docker run \
    -e QDROUTERD_CONF="$(cat interconnect/qdrouterd.conf)" \
    --memory="1g" \
    -d --name interconnect  \
    -d --net primenet --ip 172.18.0.67 \
    registry.redhat.io/amq7/amq-interconnect:latest
```



## Kafka

```
cd kafka
docker build -t kaf:0.18.0-kafka-2.5.0 .
```

```
docker run -d --name zookeeper --net primenet --ip 172.18.0.61  \
  -e LOG_DIR=/tmp/logs \
  -e KAFKA_OPTS=-javaagent:/opt/kafka/libs/jmx_prometheus_javaagent-0.12.0.jar=9404:/opt/kafka/custom-config/zookeeper-prometheus-config.yaml \
  kaf:0.18.0-kafka-2.5.0 \
  sh -c "bin/zookeeper-server-start.sh config/zookeeper.properties"

docker run -d --name kafka --net primenet --ip 172.18.0.62  \
  -e LOG_DIR=/tmp/logs \
  -e KAFKA_OPTS=-javaagent:/opt/kafka/libs/jmx_prometheus_javaagent-0.12.0.jar=9404:/opt/kafka/custom-config/kafka-prometheus-config.yaml \
  kaf:0.18.0-kafka-2.5.0 \
  sh -c "bin/kafka-server-start.sh config/server.properties --override listeners=PLAINTEXT://0.0.0.0:9092 --override advertised.listeners=PLAINTEXT://kafka:9092 --override zookeeper.connect=zookeeper:2181"
```

## Kafdrop

```
docker run -d --name kafdrop --net primenet --ip 172.18.0.63 \
    -e KAFKA_BROKERCONNECT=kafka:9092 \
    -e JVM_OPTS="-Xms32M -Xmx128M" \
    -e SERVER_SERVLET_CONTEXTPATH="/" \
    obsidiandynamics/kafdrop:3.26.0
```

Goto http://kafdrop:9000 for admin console

## Debezium Change Data Capture

```
docker run -d --name dbz --net primenet --ip 172.18.0.64 \
   -e GROUP_ID="dbz" \
   -e CONFIG_STORAGE_TOPIC="dbz-config" \
   -e OFFSET_STORAGE_TOPIC="dbz-offset" \
   -e STATUS_STORAGE_TOPIC="dbz-status" \
   -e BOOTSTRAP_SERVERS="kafka:9092" \
   debezium/connect:1.2
```

http://dbz:8083

Create connector
```
curl -X POST \
    -H "Accept:application/json" \
    -H "Content-Type:application/json" \
    http://dbz:8083/connectors -d @- <<'EOF'
{
    "name": "mysqldb-connector",
    "config": {
        "connector.class": "io.debezium.connector.mysql.MySqlConnector",
        "tasks.max": "1",
        "database.hostname": "mysql",
        "database.port": "3306",
        "database.user": "user",
        "database.password": "password",
        "database.server.id": "1000",
        "database.server.name": "mysqldbsvr",
        "database.whitelist": "mysqldb",
        "database.history.kafka.bootstrap.servers": "kafka:9092",
        "database.history.kafka.topic": "schema-changes.mysqldb"
    }
}
EOF
```

# Monitoring

## Prometheus

```

docker stop prometheus
docker rm prometheus
docker rmi prom:v2.19.0

cd prometheus
docker build -t prom:v2.19.0 .
docker run -d --name prometheus --net primenet --ip 172.18.0.70 prom:v2.19.0
cd ..
```




Goto http://prometheus:9000 for admin console

## Grafana

```
docker stop grafana
docker rm grafana
docker rmi graf:7.0.6

cd grafana
docker build -t graf:7.0.6 .
docker run -d --name grafana --net primenet --ip 172.18.0.71 graf:7.0.6
cd ..
```

Goto http://grafana:3000 for admin console

# Management

## Apicurio Schema Registry

```
docker run -d --name schemareg --net primenet --ip 172.18.0.80 \
    -e QUARKUS_DATASOURCE_URL=jdbc:postgresql://postgres:5432/db \
    -e QUARKUS_DATASOURCE_USERNAME=user \
    -e QUARKUS_DATASOURCE_PASSWORD=password \
    apicurio/apicurio-registry-jpa:1.2.2.Final
```
Console 
http://schemareg:8080/ui/artifacts
http://schemareg:8080/api


# Application Servers

## EAP

Place the installation packages form access.redhat.com in the folder `eap/.packages`

You should have :
```
├── .packages
│   ├── fuse-eap-installer-7.6.0-1.jar
│   ├── jbeap-19359.zip
│   ├── jboss-eap-7.2.0.zip
│   └── jboss-eap-7.2.8-patch.zip
```

Run these commands to build and run the container

```
docker stop eap
docker rm eap
docker rmi eap:7.2

cd eap
docker build -t eap:7.2 .
cd ..


docker run -d --name eap --net primenet --ip 172.18.0.90 \
    eap:7.2

```
