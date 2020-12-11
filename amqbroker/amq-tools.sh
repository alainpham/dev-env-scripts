
#!/bin/bash

function initamqvars(){
    export AMQ_CLUSTERS=(a b)
    export AMQ_REPLICAS_NB=2

    declare -A upstream
    upstream=( [a]=b [b]=a)
    export upstream

    declare -A all_mirror_hosts

    offset=0
    for cluster in ${AMQ_CLUSTERS[@]}
    do
        y=0
        while [ $y -lt $AMQ_REPLICAS_NB ]
        do
            x=0
            while [ $x -lt $AMQ_REPLICAS_NB ]
            do
                if [ $x -eq 0 ]
                then
                    all_mirror_hosts[$cluster$y]=tcp://amqbroker${upstream[${cluster}]}${y}:61617,tcp://amqbroker${upstream[${cluster}]}${x}:61617
                else
                    all_mirror_hosts[$cluster$y]=${all_mirror_hosts[$cluster$y]},tcp://amqbroker${upstream[${cluster}]}${x}:61617
                fi
                x=$(( $x + 1 ))
            done
            all_mirror_hosts[$cluster$y]='('${all_mirror_hosts[$cluster$y]}')'
            echo "upstream for " $cluster$y ${all_mirror_hosts[${cluster}${y}]}
            export all_mirror_hosts
            y=$((y+1))
        done
        offset=$((offset+1))

    done


    declare -A federation_connectors
    declare -A federation_refs


    offset=0
    for cluster in ${AMQ_CLUSTERS[@]}
    do
        y=0
        while [ $y -lt $AMQ_REPLICAS_NB ]
        do
            federation_connectors[$cluster$y]=""
            federation_refs[$cluster$y]=""
            x=0
            while [ $x -lt $AMQ_REPLICAS_NB ]
            do
                federation_connectors[$cluster$y]=${federation_connectors[$cluster$y]}$'\n''<connector name="'${upstream[${cluster}]}${x}'">'tcp://amqbroker${upstream[${cluster}]}${x}':61617?sslEnabled=true;keyStorePath=${AMQ_KEYSTORE_TRUSTSTORE_DIR}/${AMQ_KEYSTORE};keyStorePassword=${AMQ_KEYSTORE_PASSWORD};trustStorePath=${AMQ_KEYSTORE_TRUSTSTORE_DIR}/${AMQ_TRUSTSTORE};trustStorePassword=${AMQ_TRUSTSTORE_PASSWORD};sslProvider=${AMQ_SSL_PROVIDER};ackBatchSize=64</connector>'
                
                federation_refs[$cluster$y]=${federation_refs[$cluster$y]}$'\n''<connector-ref>'${upstream[${cluster}]}${x}'</connector-ref>'

                x=$(( $x + 1 ))
            done
            
            echo "upstream for " $cluster$y "${federation_connectors[${cluster}${y}]}"
            echo "upstream for " $cluster$y "${federation_refs[${cluster}${y}]}"

            y=$((y+1))
        done
        offset=$((offset+1))

    done
    export federation_connectors
    export federation_refs

}

function drafttopology(){
    offset=0
    for cluster in ${AMQ_CLUSTERS[@]}
    do

        export AMQ_NAME_SUFFIX=$cluster
        export AMQ_MULTICASTPORT=$((9876 + $offset))
        export AMQ_INTERFACE_IP_PREFIX=172.18.0.1${offset}



        echo "DO STUFF for : $AMQ_NAME_SUFFIX, $AMQ_MULTICASTPORT, $AMQ_INTERFACE_IP_PREFIX"
        offset=$((offset+1))
    done
}

function tlsgen(){
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
}


function runbrokers(){
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
        export AMQ_MIRROR_HOST=${all_mirror_hosts[$cluster$x]}
        export AMQ_MIRROR_XML=$(echo "${federation_connectors[$cluster$x]}" | envsubst  )
        export AMQ_MIRROR_HOST_REF=${federation_refs[$cluster$x]}
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
            $AMQ_MIRROR_XML,\
            $AMQ_MIRROR_HOST_REF,\
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
}

function intobrokers(){
    docker exec -it amqbrokera0 bash
}

function stopbrokers(){
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
}