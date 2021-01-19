FROM registry.redhat.io/ubi8/openjdk-8:latest
USER root
RUN microdnf install git && microdnf clean all
RUN /usr/sbin/usermod -u 1000 jboss
ADD settings.xml /home/jboss/.m2/
USER jboss