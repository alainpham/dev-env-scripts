# Purpose of this repo

This is a script to setup what I commonly use in a dev environement to build PoCs.

# Prereqs

You should have docker installed with a dedicated.
Create a network called primenet

```
docker network create --driver=bridge --subnet=172.18.0.0/16 --gateway=172.18.0.1 primenet 
```

# Hosts file

```
172.18.0.40 portainer

172.18.0.50 mysql
172.18.0.51 oracle
172.18.0.52 couchbase
172.18.0.53 infinispan

172.18.0.60 artemis
172.18.0.61 zookeeper
172.18.0.62 kafka

172.18.0.70 prometheus
172.18.0.71 grafana

```

# Portainer

```
docker run -d --name=portainer --net primenet --ip 172.18.0.40 -v /var/run/docker.sock:/var/run/docker.sock portainer/portainer:linux-amd64-1.20.2
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

# Messaging

## Artemis

```
docker run -d --name artemis --net primenet --ip 172.18.0.60  \
  -e ARTEMIS_USERNAME=artemis \
  -e ARTEMIS_PASSWORD=password \
  -e ENABLE_JMX_EXPORTER=true \
  vromero/activemq-artemis:2.13.0-alpine
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
docker rmi graf:5.4.3

cd grafana
docker build -t graf:5.4.3 .
docker run -d --name grafana --net primenet --ip 172.18.0.71 graf:5.4.3
cd ..
```

Goto http://grafana:3000 for admin console
