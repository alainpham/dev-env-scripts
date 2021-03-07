

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
vmcreate prime 2048 4 debian 10 40G debian10
vmcreate second 2048 4 debian 11 40G debian10

```

# Delete vms example

```

dvm prime
dvm second
```