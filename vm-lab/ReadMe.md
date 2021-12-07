

# TOC

- [TOC](#toc)
- [Purpose of this repo](#purpose-of-this-repo)
- [Prereq](#prereq)
- [Install](#install)
- [Setup dnsmasq with Networkmanager](#setup-dnsmasq-with-networkmanager)
- [Create a VM](#create-a-vm)
- [Delete vms example](#delete-vms-example)
- [Run Ansible to install Kubernetes](#run-ansible-to-install-kubernetes)
- [(Draft) Install vanilla Kubernetes](#draft-install-vanilla-kubernetes)
- [Using ansible playbooks](#using-ansible-playbooks)
  - [prepare hosts](#prepare-hosts)
  - [setup masters and workers](#setup-masters-and-workers)
  - [setup different components](#setup-different-components)
- [Setup SAP on a single VM](#setup-sap-on-a-single-vm)


# Purpose of this repo 

How to create vms with command line using virt install

# Prereq 

Install ansible

# Install

```
sudo cp scripts/* /usr/local/bin/

```

# Setup dnsmasq with Networkmanager

```
# /etc/NetworkManager/conf.d/00-use-dnsmasq.conf
#
# This enabled the dnsmasq plugin.
[main]
dns=dnsmasq

```

Setup custom locations

```

#/etc/NetworkManager/dnsmasq.d/dev.conf
address=/kube.loc/192.168.122.100
```

# Create a VM


```
ssh-keygen -f ~/.ssh/vm
```


```
vmcreate master 4048 4 debian 10 40G debian10
vmcreate node01 4048 4 debian 11 40G debian10
vmcreate node02 4048 4 debian 12 40G debian10

vmcreate-rhel vhcalnplci 10000 4 rhel-server-7.9-update-9-x86_64-kvm 13 60G rhel7-unknown $RHUSER $RHPWD $RHPOOLID



```

for ocp

```
sudo virt-install -n ocp-dev --memory 65536 --vcpus=12 --os-variant=fedora-coreos-stable --accelerate -v --cpu host-passthrough,cache.mode=passthrough --disk path=/var/lib/libvirt/images/ocp-dev.qcow2,size=120 --network network=ocp-dev,mac=02:01:00:00:00:66 --cdrom /var/lib/libvirt/images/discovery_image.iso


sudo virsh net-update default add ip-dhcp-host "<host mac='52:54:00:00:00:20' name='thematrix' ip='192.168.122.20' />" --live --config

sudo virt-install --name thematrix --ram 18432 --vcpus 6 --disk \
    /home/workdrive/virt/runtime/thematrix.qcow2,format=qcow2,bus=virtio,size=50 --cdrom /home/workdrive/virt/images/discovery-image.iso --network \
    bridge=virbr0,model=virtio,mac=52:54:00:00:00:20 --os-variant=rhel8-unknown --noautoconsole --cpu host-passthrough,cache.mode=passthrough

  
```


# Delete vms example

```
dvm master
dvm node01
dvm node02
dvm sap
dvm vhcalnplci
```

# Run Ansible to install Kubernetes

```
cd kube-ansible
ansible-playbook install.yml
```

# (Draft) Install vanilla Kubernetes

```
kvsh prime

vi /etc/modules
(add)
br_netfilter

update-alternatives --config iptables 

select legacy

update-alternatives --set iptables /usr/sbin/iptables-legacy

cat > /etc/docker/daemon.json <<EOF
{
  "exec-opts": ["native.cgroupdriver=systemd"],
  "log-driver": "json-file",
  "log-opts": {
    "max-size": "100m"
  },
  "storage-driver": "overlay2"
}
EOF

systemctl restart docker

apt -y install apt-transport-https gnupg2 curl

curl -s https://packages.cloud.google.com/apt/doc/apt-key.gpg | apt-key add -


echo "deb http://apt.kubernetes.io/ kubernetes-xenial main" | tee -a /etc/apt/sources.list.d/kubernetes.list

apt update

apt -y install kubeadm kubelet kubectl


systemctl enable kubelet

on master node specifically

kubeadm init --apiserver-advertise-address=192.168.122.10 --pod-network-cidr=10.244.0.0/16

mkdir -p $HOME/.kube
cp -i /etc/kubernetes/admin.conf $HOME/.kube/config
chown $(id -u):$(id -g) $HOME/.kube/config

kubectl apply -f https://raw.githubusercontent.com/coreos/flannel/master/Documentation/kube-flannel.yml

kubectl get nodes

kubectl get pods --all-namespaces


on worker node
kubeadm join 192.168.122.10:6443 --token xxipx5.8yhjqrhdt4hx1thp \
    --discovery-token-ca-cert-hash sha256:837e6e5f83fa5f4abfcbb4f646026937f15205c7c2fb791b53e160fe65c6a799


kubectl apply -f https://raw.githubusercontent.com/kubernetes/dashboard/master/aio/deploy/recommended.yaml


helm repo add haproxytech https://haproxytech.github.io/helm-charts
helm repo update
helm search repo haproxy --versions
helm install kubernetes-ingress haproxytech/kubernetes-ingress
kubectl get pods -A
kubectl get svc -A

install kubernetes dashboard

kubectl apply -f kube-yaml/kubernetes-dashboard-ingress.yml -n kubernetes-dashboard

visit https://admin.kube.loc:30350/#/login

get token

kubectl -n kube-system describe $(kubectl -n kube-system get secret -n kube-system -o name | grep namespace)


Operator hub

kubectl apply -f https://github.com/operator-framework/operator-lifecycle-manager/releases/download/v0.17.0/crds.yaml
kubectl apply -f https://github.com/operator-framework/operator-lifecycle-manager/releases/download/v0.17.0/olm.yaml
```

create a certificate for ha proxy ingress


kubectl get secret haproxy-ingress-tls -o yaml


kubectl create secret tls  haproxy-ingress-tls \
  --key="tls/kube.loc.key" \
  --cert="tls/kube.loc.crt"

```
openssl req -x509 \
  -newkey rsa:2048 \
  -keyout kube-yaml/tls/kube.loc.key \
  -out kube-yaml/tls/kube.loc.crt \
  -days 365000 \
  -nodes \
  -subj "/CN=*.kube.loc"

openssl req -x509 \
  -newkey rsa:2048 \
  -keyout kube-yaml/tls/event-broker-kafka.key \
  -out kube-yaml/tls/event-broker-kafka.crt \
  -days 365000 \
  -nodes \
  -subj "/CN=event-broker-kafka"
```

If you have pullsecrets to add put them in file with name kube-pull-secret kube-ansible/kube-pullsecrets/kube-pull-secret.yaml


# Using ansible playbooks

```
cd kube-ansible
```

## prepare hosts

```
ansible-playbook prepare-hosts.yml
```

## setup masters and workers

```
ansible-playbook setup-master.yml
ansible-playbook setup-node.yml
```

## setup different components

```
ansible-playbook setup-master.yml
ansible-playbook setup-node.yml
```
# Setup SAP on a single VM

```
sudo su
yum -y update
yum -y install https://dl.fedoraproject.org/pub/epel/epel-release-latest-7.noarch.rpm

yum -y install fuse-sshfs tcsh libaio uuidd
yum -y install tcsh libaio uuidd
password : Abap10
```
