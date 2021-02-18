FROM registry.access.redhat.com/ubi8/openjdk-8:1.3 as build-stage

ARG EAP_PACKAGE_VERSION_SHORT=6.4
ARG EAP_PACKAGE_VERSION_FULL=${EAP_PACKAGE_VERSION_SHORT}.0
ARG EAP_PACKAGE_PREFIX=jboss-eap

ARG EAP_PACKAGE_NAME=${EAP_PACKAGE_PREFIX}-${EAP_PACKAGE_VERSION_SHORT}
ARG EAP_PACKAGE_ZIP=${EAP_PACKAGE_PREFIX}-${EAP_PACKAGE_VERSION_FULL}.zip
ARG EAP_ROOT_FOLDER=/opt/eap

ARG EAP_USER=admin
ARG EAP_PASSWORD=password0!

ADD .packages/* /tmpeap/
USER root

RUN unzip /tmpeap/${EAP_PACKAGE_ZIP} -d /tmpeap/eap/ \
    && mv /tmpeap/eap/${EAP_PACKAGE_NAME} ${EAP_ROOT_FOLDER} \
    && ${EAP_ROOT_FOLDER}/bin/add-user.sh -u ${EAP_USER} -p ${EAP_PASSWORD} \
    && cd /tmpeap


# Second stage to reduce image size
FROM registry.access.redhat.com/ubi8/openjdk-8:1.3 AS runtime-image
USER 185
ARG EAP_ROOT_FOLDER=/opt/eap

COPY --from=build-stage ${EAP_ROOT_FOLDER} ${EAP_ROOT_FOLDER}
ADD start.sh /opt/eap/start.sh
ADD config.properties ${EAP_ROOT_FOLDER}/custom-config/config.properties
USER root
RUN chown 185:185 ${EAP_ROOT_FOLDER}
RUN chown -R 185:185 ${EAP_ROOT_FOLDER}
USER 185
EXPOSE 8080
EXPOSE 9990
WORKDIR ${EAP_ROOT_FOLDER}
RUN microdnf vi \
    && microdnf update
ENTRYPOINT [ "/opt/eap/start.sh" ,"--server-config=standalone-full.xml", "-P", "custom-config/config.properties"]