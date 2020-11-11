

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

Goto http://artemis:8161/console for admin console

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
