

# TOC

- [TOC](#toc)
- [Purpose of this repo](#purpose-of-this-repo)
- [Prereq](#prereq)
- [Install](#install)
- [Setup dnsmasq with Networkmanager](#setup-dnsmasq-with-networkmanager)
- [Create a VM](#create-a-vm)
- [Delete vms example](#delete-vms-example)


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
address=/prime.loc/192.168.122.10
address=/secondary.loc/192.168.122.11
```

# Create a VM


```
ssh-keygen -f ~/.ssh/vm
```


```
vmcreate prime 2048 4 debian 10 40G debian10
vmcreate second 2048 4 debian 11 40G debian10
```

# Delete vms example

```
dvm prime
dvm second
```

#(Draft) Install vanilla Kubernetes

```
kvsh prime

vi /etc/modules
(add)
br_netfilter

update-alternatives --config iptables 

select legacy

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
```
