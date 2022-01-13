FROM rockylinux/rockylinux:8
ENV container=docker

RUN yum -y update && yum clean all
RUN yum -y install epel-release openssh-clients \
    && yum -y install ansible \
    && yum clean all

RUN useradd -u 1000 ansible

WORKDIR /home/ansible/source
    
USER ansible
