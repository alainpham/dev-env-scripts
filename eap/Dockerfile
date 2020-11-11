FROM registry.access.redhat.com/ubi8/openjdk-8:1.3 as build-stage

ARG EAP_PACKAGE_VERSION_SHORT=7.2
ARG EAP_PACKAGE_VERSION_FULL=${EAP_PACKAGE_VERSION_SHORT}.0
ARG EAP_PACKAGE_PREFIX=jboss-eap

ARG EAP_PACKAGE_NAME=${EAP_PACKAGE_PREFIX}-${EAP_PACKAGE_VERSION_SHORT}
ARG EAP_PACKAGE_ZIP=${EAP_PACKAGE_PREFIX}-${EAP_PACKAGE_VERSION_FULL}.zip
ARG PATCH_LIST="jboss-eap-7.2.8-patch.zip jbeap-19359.zip"
ARG CAMEL_PACKAGE="fuse-eap-installer-7.7.0.jar"
ARG EAP_ROOT_FOLDER=/opt/eap

ARG EAP_USER=admin
ARG EAP_PASSWORD=admin

ARG MYSQL_VERSION=8.0.20
ARG POSGRES_VERSION=42.2.14

ADD .packages/* /tmpeap/
USER root

RUN unzip /tmpeap/${EAP_PACKAGE_ZIP} -d /tmpeap/eap/ \
    && mv /tmpeap/eap/${EAP_PACKAGE_NAME} ${EAP_ROOT_FOLDER} \
    && ${EAP_ROOT_FOLDER}/bin/add-user.sh -u ${EAP_USER} -p ${EAP_PASSWORD} \
    && cd /tmpeap \
    && for PATCH in ${PATCH_LIST}; do ${EAP_ROOT_FOLDER}/bin/jboss-cli.sh "patch apply ${PATCH}"; done \
    && cd ${EAP_ROOT_FOLDER} && java -jar /tmpeap/${CAMEL_PACKAGE}

ADD config.properties ${EAP_ROOT_FOLDER}/custom-config/config.properties
ADD standalone.conf ${EAP_ROOT_FOLDER}/bin/standalone.conf
ADD https://raw.githubusercontent.com/alainpham/app-archetypes/master/spring-boot-camel/src/main/resources/archetype-resources/src/main/prometheus/prometheus-config.yml ${EAP_ROOT_FOLDER}/custom-config/prometheus-config.yml

#JDBC Drivers
ADD https://repo1.maven.org/maven2/mysql/mysql-connector-java/${MYSQL_VERSION}/mysql-connector-java-${MYSQL_VERSION}.jar ${EAP_ROOT_FOLDER}/modules/system/layers/base/com/mysql/main/mysql-connector-java-${MYSQL_VERSION}.jar
ADD mysql.xml ${EAP_ROOT_FOLDER}/modules/system/layers/base/com/mysql/main/module.xml
RUN sed -i "s/VERSION/${MYSQL_VERSION}/g" ${EAP_ROOT_FOLDER}/modules/system/layers/base/com/mysql/main/module.xml

ADD https://repo1.maven.org/maven2/org/postgresql/postgresql/${POSGRES_VERSION}/postgresql-${POSGRES_VERSION}.jar ${EAP_ROOT_FOLDER}/modules/system/layers/base/org/postgresql/main/postgresql-${POSGRES_VERSION}.jar
ADD postgresql.xml ${EAP_ROOT_FOLDER}/modules/system/layers/base/org/postgresql/main/module.xml
RUN sed -i "s/VERSION/${POSGRES_VERSION}/g" ${EAP_ROOT_FOLDER}/modules/system/layers/base/org/postgresql/main/module.xml

#add all resources
ADD resources.cli /tmpeap/resources.cli
RUN ${EAP_ROOT_FOLDER}/bin/jboss-cli.sh  --file=/tmpeap/resources.cli

RUN chown 185:185 ${EAP_ROOT_FOLDER}
RUN chown -R 185:185 ${EAP_ROOT_FOLDER}

# Second stage to reduce image size
FROM registry.access.redhat.com/ubi8/openjdk-8:1.3 AS runtime-image
ARG EAP_ROOT_FOLDER=/opt/eap
COPY --from=build-stage ${EAP_ROOT_FOLDER} ${EAP_ROOT_FOLDER}
USER 185
EXPOSE 8080
EXPOSE 9990
WORKDIR ${EAP_ROOT_FOLDER}
ENTRYPOINT [ "bin/standalone.sh" , "-P", "custom-config/config.properties"]