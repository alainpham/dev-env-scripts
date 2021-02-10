FROM registry.redhat.io/amq7/amq-interconnect:latest
USER root
ADD tls/* /etc/qpid-dispatch-certs/default/interconnect-tls/
RUN chown -R jboss /etc/qpid-dispatch-certs/default/interconnect-tls
USER jboss