FROM registry.redhat.io/fuse7/fuse-java-openshift
COPY src/main/fabric8-includes/prometheus-config.yml /deployments
ENV AB_JMX_EXPORTER_CONFIG prometheus-config.yml
COPY target/*.jar /deployments
COPY tls/* /deployments/tls/