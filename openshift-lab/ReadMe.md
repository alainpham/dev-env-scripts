# TOC

- [TOC](#toc)
- [Prepare cluster](#prepare-cluster)
  - [Currently used versions](#currently-used-versions)
  - [User and password](#user-and-password)
- [Fuse](#fuse)
  - [Import images](#import-images)
- [AMQ Broker](#amq-broker)
  - [Import images](#import-images-1)
  - [Install hotfix](#install-hotfix)
  - [Setting up vars](#setting-up-vars)
  - [Creating keystores and truststores](#creating-keystores-and-truststores)
  - [Config generation](#config-generation)
  - [Create 2 Cluster in Federation](#create-2-cluster-in-federation)
  - [Deploy Local Monitoring](#deploy-local-monitoring)
  - [Install using operator](#install-using-operator)
  - [Single Cluster using stateful set only](#single-cluster-using-stateful-set-only)
- [Install Interconnect](#install-interconnect)



# Prepare cluster 

## Currently used versions
```
export fuse_app_template_version=2.1.0.fuse-sb2-780019-redhat-00005
export amq_broker_image_version=78-7.8-12
```

## User and password

Set your user token or credentials and login

```
export ocpuser="USR"
export ocppwd="PASSWORD"
export ocptoken="TOKEN"
export ocpurl="https://api.cluster-968d.968d.example.opentlc.com:6443"
```

```
oc login -u ${ocpuser} -p ${ocppwd} --server=${ocpurl}
```

```
oc login --token=${ocptoken} --server=${ocpurl}
```


# Fuse

## Import images
```
BASEURL=https://raw.githubusercontent.com/jboss-fuse/application-templates/application-templates-${fuse_app_template_version}

oc create -n openshift -f ${BASEURL}/fis-image-streams.json
oc replace -n openshift -f ${BASEURL}/fis-image-streams.json
```

# AMQ Broker

## Import images
```
oc replace -n openshift  -f https://raw.githubusercontent.com/jboss-container-images/jboss-amq-7-broker-openshift-image/${amq_broker_image_version}/amq-broker-7-image-streams.yaml
oc create -n openshift -f https://raw.githubusercontent.com/jboss-container-images/jboss-amq-7-broker-openshift-image/${amq_broker_image_version}/amq-broker-7-image-streams.yaml
```

## Install hotfix

Hotfix contains this [fix](https://github.com/alainpham/activemq-artemis/commit/e25fd3f64a95a5092b582c05f3e03486442df242) for federation connections

```
oc project openshift
oc new-build --strategy docker --binary -i amq-broker:7.8 --name amq-broker-hotfix
oc start-build amq-broker-hotfix --from-dir=amqbroker/hotfix/build --follow
```

## Setting up vars

```
export AMQ_CLUSTERS=(amq-messaging.apps.cluster-968d.968d.example.opentlc.com amq-messaging-mirror.apps.cluster-968d.968d.example.opentlc.com)
export AMQ_REPLICAS_NB=2
declare -A upstream
upstream=( [amq-messaging.apps.cluster-968d.968d.example.opentlc.com]=amq-messaging-mirror.apps.cluster-968d.968d.example.opentlc.com [amq-messaging-mirror.apps.cluster-968d.968d.example.opentlc.com]=amq-messaging.apps.cluster-968d.968d.example.opentlc.com)
export upstream


declare -A brokername
brokername=( [amq-messaging.apps.cluster-968d.968d.example.opentlc.com]=amq-broker-a [amq-messaging-mirror.apps.cluster-968d.968d.example.opentlc.com]=amq-broker-b)
export brokername

declare -A mirrorname
mirrorname=( [amq-messaging.apps.cluster-968d.968d.example.opentlc.com]=amq-broker-b [amq-messaging-mirror.apps.cluster-968d.968d.example.opentlc.com]=amq-broker-a)
export mirrorname

declare -A fedname
fedname=( [amq-messaging.apps.cluster-968d.968d.example.opentlc.com]=amqafederation [amq-messaging-mirror.apps.cluster-968d.968d.example.opentlc.com]=amqbfederation)
export fedname

```

## Creating keystores and truststores

```

for cluster in ${AMQ_CLUSTERS[@]}
do
  rm amqbroker/tls/${cluster}-keystore.p12
  keytool -genkey \
      -alias ${cluster}  \
      -storepass password \
      -keyalg RSA \
      -storetype PKCS12 \
      -dname "cn=${cluster}" \
      -validity 365000 \
      -keystore amqbroker/tls/${cluster}-keystore.p12

  keytool -export \
      -alias ${cluster} \
      -rfc \
      -storepass password \
      -keystore amqbroker/tls/${cluster}-keystore.p12 \
      -file amqbroker/tls/trusted-certs/${cluster}.pem
done


echo "Generate client keys"

keytool -genkey \
    -alias amqclient  \
    -storepass password \
    -keyalg RSA \
    -storetype PKCS12 \
    -dname "cn=amqclient" \
    -validity 365000 \
    -keystore amqbroker/tls/client-keystore.p12

keytool -export \
    -alias amqclient \
    -rfc \
    -storepass password \
    -keystore amqbroker/tls/client-keystore.p12 \
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

## Config generation

```
function generateconfigs(){

  for cluster in ${AMQ_CLUSTERS[@]}
  do
    export AMQ_NS="amq-messaging"
    export AMQ_DOMAIN="$cluster"
    export AMQ_NAME=${brokername[$cluster]}
    export AMQ_FEDNAME=${fedname[$cluster]}
    export AMQ_STATEFUL_SET_NAME="${AMQ_NAME}-ss"
    export AMQ_JOURNAL_TYPE="nio" 
    export AMQ_DATA_DIR="/opt/${AMQ_NAME}/data" 
    export AMQ_KEYSTORE_TRUSTSTORE_DIR="/etc/amq-secret-volume" 
    export AMQ_TRUSTSTORE="truststore.p12" 
    export AMQ_TRUSTSTORE_PASSWORD="password" 
    export AMQ_KEYSTORE="${cluster}-keystore.p12" 
    export AMQ_KEYSTORE_PASSWORD="password" 
    export AMQ_SSL_PROVIDER="JDK" 
    export AMQ_SSL_NEED_CLIENT_AUTH="true"
    export AMQ_MIRROR_HOST=tcp://${mirrorname[$cluster]}-acceptor-hls.${upstream[$cluster]}:443
    #export AMQ_MIRROR_HOST=amqbroker-mirror:61617
    export AMQ_CLUSTER_USER=${AMQ_NAME}-cluster
    export AMQ_CLUSTER_PASSWORD=${AMQ_NAME}-password
    export AMQ_JOURNAL_TYPE_UPPER=$(echo $AMQ_JOURNAL_TYPE | tr [:lower:] [:upper:])

    export AMQ_REQUIRE_LOGIN=false
    export AMQ_DATA_DIR_LOGGING="true"
    export AMQ_CLUSTERED=true
    export AMQ_USER=admin
    export AMQ_PASSWORD=admin
    export AMQ_ROLE=admin
    export AMQ_REPLICAS=$AMQ_REPLICAS_NB


    envsubst '\
        $AMQ_DOMAIN,\
        $AMQ_STATEFUL_SET_NAME, \
        $AMQ_NAME,\
        $AMQ_FEDNAME,\
        $AMQ_JOURNAL_TYPE,\
        $AMQ_JOURNAL_TYPE_UPPER,\
        $AMQ_DATA_DIR,\
        $AMQ_KEYSTORE_TRUSTSTORE_DIR,\
        $AMQ_TRUSTSTORE,\
        $AMQ_TRUSTSTORE_PASSWORD,\
        $AMQ_KEYSTORE,\
        $AMQ_KEYSTORE_PASSWORD,\
        $AMQ_SSL_PROVIDER,\
        $AMQ_SSL_NEED_CLIENT_AUTH, \
        $AMQ_MIRROR_HOST, \
        $AMQ_REQUIRE_LOGIN, \
        $AMQ_DATA_DIR_LOGGING, \
        $AMQ_CLUSTERED, \
        $AMQ_USER, \
        $AMQ_PASSWORD, \
        $AMQ_ROLE, \
        $AMQ_CLUSTER_USER,\
        $AMQ_CLUSTER_PASSWORD,\
        $AMQ_REPLICAS, \
        ' < amqbroker/${TMPL_FILE} > amq-broker-custom.final.${cluster}.kube.yml

    AMQ_INSTANCE_NB=0
    while [ $AMQ_INSTANCE_NB -lt $AMQ_REPLICAS ]
    do
      export AMQ_INSTANCE_NB
      envsubst '\
          $AMQ_DOMAIN,\
          $AMQ_NAME,\
          $AMQ_INSTANCE_NB,\
          ' < amqbroker/amq-broker-custom.network.template.kube.yml > amq-broker-custom.network.${cluster}.${AMQ_INSTANCE_NB}.kube.yml
      echo "oc apply -f amq-broker-custom.network.${cluster}.${AMQ_INSTANCE_NB}.kube.yml"
      AMQ_INSTANCE_NB=$(( $AMQ_INSTANCE_NB + 1 ))
    done

    echo "oc apply -f amq-broker-custom.final.${cluster}.kube.yml"

    echo "oc create secret generic ${AMQ_NAME}-tls-secret \
    --from-file=${AMQ_KEYSTORE}=amqbroker/tls/${AMQ_KEYSTORE} \
    --from-file=${AMQ_TRUSTSTORE}=amqbroker/tls/${AMQ_TRUSTSTORE}"

  done
}

```



## Create 2 Cluster in Federation
```
export TMPL_FILE=amq-broker-custom-cluster-federation.template.kube.yml
generateconfigs
oc new-project amq-messaging
oc new-project amq-messaging-mirror
```

## Deploy Local Monitoring

Follow these instructions 

https://github.com/alainpham/app-archetypes#install-prometheus-and-grafana-kubernetesopenshift-namespace-for-monitoring

## Install using operator

```

keytool -genkey \
      -alias amq-broker  \
      -storepass password \
      -keyalg RSA \
      -storetype PKCS12 \
      -dname "cn=amq-broker" \
      -validity 365000 \
      -keystore amqbroker/tls/amq-broker-keystore.p12


oc create secret generic amq-broker-generic-secret \
--from-file=broker.ks=amqbroker/tls/amq-broker-keystore.p12 \
--from-file=client.ts=amqbroker/tls/truststore.p12 \
--from-literal=keyStorePassword=password \
--from-literal=trustStorePassword=password

oc apply -f amqbroker/amq-broker-simple-cluster.yml
```


## Single Cluster using stateful set only
```
oc new-project amq-messaging-mirror

export AMQ_CLUSTERS=(amq-broker)

declare -A brokername
brokername=( [amq-broker]=amq-broker)
export brokername

export TMPL_FILE=amq-broker-custom-cluster.template.kube.yml

generateconfigs

```

# Install Interconnect

Install the operator

custom install
```
oc new-project amq-messaging
oc create -f interconnect/deploy/service_account.yaml
oc create -f interconnect/deploy/role.yaml
oc create -f interconnect/deploy/role_binding.yaml
oc create -f interconnect/deploy/operator.yaml
```

Deploy cluster

```

mkdir interconnect/tls

openssl genrsa -out interconnect/tls/ca-key.pem 2048
openssl req -new -batch -key interconnect/tls/ca-key.pem -out interconnect/tls/ca-csr.pem
openssl x509 -req -in interconnect/tls/ca-csr.pem -signkey interconnect/tls/ca-key.pem -out interconnect/tls/ca.crt


openssl genrsa -out interconnect/tls/tls.key 2048
openssl req -new -batch -subj "/CN=amq-interconnect.amq-messaging.svc.cluster.local" -key interconnect/tls/tls.key -out interconnect/tls/server-csr.pem

openssl x509 -req -in interconnect/tls/server-csr.pem -CA interconnect/tls/ca.crt -CAkey interconnect/tls/ca-key.pem -out interconnect/tls/tls.crt -CAcreateserial

oc create secret generic interconnect-cluster-default-credentials --from-file=tls.crt=interconnect/tls/tls.crt  --from-file=tls.key=interconnect/tls/tls.key  --from-file=ca.crt=interconnect/tls/ca.crt

oc apply -f interconnect/interconnect-cluster.yml

oc apply -f interconnect/interconnect-cluster-mirror.yml

```