Plain old Java programm

docker rmi hornetq-client
docker build -t hornetq-client .


docker tag hornetq-client:latest alainpham/hornetq-client:latest
docker push alainpham/hornetq-client:latest


docker run -it --net primenet --ip 172.18.0.240 --rm hornetq-client

cp tls/local/* /deployments/tls 

cp tls/ocp/* /deployments/tls 


serverhost=test.apps.cluster-33cc.33cc.example.opentlc.com
serverport=443

echo -n | openssl s_client -connect $serverhost:$serverport | sed -ne '/-BEGIN CERTIFICATE-/,/-END CERTIFICATE-/p' > tls/trusted-certs/ocp.pem


keytool -import \
    -alias ocp \
    -storepass password\
    -storetype JKS \
    -noprompt \
    -keystore tls/truststore.jks \
    -file tls/trusted-certs/ocp.pem

keytool -list -storepass password -keystore tls/truststore.jks 


oc delete -f deploy.yml
oc apply -f deploy.yml