
# Common steps for all kinds of machines

## Setup user and ssh on server

```
sudo adduser apham
```

add line to /etc/sudoers

```
sudo visudo
apham ALL=(ALL) NOPASSWD:ALL
```

exit server and run on workstation

copy ssh key
```
ssh-copy-id -i ~/.ssh/id_rsa.pub <serverhost>
```

remove ssh password auth
```
sudo vi /etc/ssh/sshd_config 

PasswordAuthentication no

sudo systemctl restart ssh
```

# Common installs

```

docker network create --driver=bridge --subnet=172.18.0.0/16 --gateway=172.18.0.1 primenet

sudo adduser apham libvirt
sudo groupadd docker
sudo usermod -aG docker apham

sudo curl -L https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp -o /usr/local/bin/yt-dlp
sudo chmod a+rx /usr/local/bin/yt-dlp



```

# Pure Server


Install base packages

```

apt install qemu-system qemu-utils virtinst libvirt-clients libvirt-daemon-system bridge-utils dnsmasq git ansible docker.io apparmor haproxy tmux vim maven openjdk-11-jdk prometheus-node-exporter

```


# Workstation

```
apt install vim vlc ffmpeg mpv curl lshw vdpauinfo vainfo mediainfo python-mutagen vim-gui-common linux-cpupower arandr v4l2loopback-utils ncdu openjdk-11-jdk curl mercurial vim flatpak ffmpeg qemu-system libvirt-clients libvirt-daemon-system docker.io jackd2 qjackctl pulseaudio-module-jack zita-ajbridge ardour v4l-utils  intel-microcode mesa-utils intel-media-va-driver-non-free virtinst  libosinfo-bin htop snapd firmware-linux intel-gpu-tools prometheus-node-exporter net-tools virt-manager qemu-kvm dnsutils gdb cmake lib32z1 libncurses5


sudo snap install obs-studio
Snap install dbeaver
```

# RPI Server

* flash drive imager using the minimal raspberryOS image
* plug in and turn on
* run intial config

```
sudo raspi-config

System Options -> Hostname
System Options -> Password
Interface Options -> Legacy Camera
Interface Options -> SSH
Localisation -> Timezone
Localisation -> Keyboard

Reboot
```

install base packages

```
sudo apt update 
sudo apt upgrade

sudo apt install dnsmasq git ansible docker.io apparmor haproxy tmux vim maven openjdk-11-jdk prometheus-node-exporter
```

configure docker for exec without sudo
```
sudo groupadd docker
sudo usermod -aG docker apham
```

# Services & Apps


## Pihole

```
sudo systemctl stop dnsmasq.service 
sudo systemctl disable dnsmasq.service


docker run -d \
--name=pihole \
--net=host \
--cap-add=NET_ADMIN \
--hostname ampi \
--restart=unless-stopped \
-e VERSION=latest \
-e WEBPASSWORD=cjsquhxdsd7864 \
-e TZ=Europe/Paris \
-e ServerIP=192.168.8.254 \
-e DHCP_ACTIVE=true \
-e DHCP_START=192.168.8.20 \
-e DHCP_END=192.168.8.200 \
-e DHCP_ROUTER=192.168.8.1 \
-v /home/apham/pihole/etc-pihole:/etc/pihole \
-v /home/apham/pihole/etc-dnsmasq.d:/etc/dnsmasq.d \
pihole/pihole

docker start pihole
```

Addlist from https://firebog.net/

Whitelist

clickserve.dartsearch.net	
Exact whitelist
dartsearch.net	
Exact whitelist
googleadservices.com	
Exact whitelist
www.googleadservices.com	
Exact whitelist
(\.|^)fonts\.gstatic\.com$	
Regex whitelist
s.youtube.com	
Exact whitelist
(\.|^)fbcdn\.net$	
Regex whitelist
(\.|^)facebook\.com$	
Regex whitelist
ad.doubleclick.net	
Exact whitelist


## Plex

```
sudo docker create \
--name=plex \
--net=host \
--restart=unless-stopped \
-e VERSION=latest \
-e PUID=1001 -e PGID=1001 \
-e TZ=Europe/Paris \
-v /home/apham/plex/config:/config \
-v /home/apham/plex/tvshows:/data/tvshows \
-v /home/apham/plex/movies:/data/movies \
-v /home/apham/plex/music:/data/music \
-v /home/apham/plex/transcode:/transcode \
-v /home/apham/plex/videos/home-videos:/data/home-videos \
-v /home/apham/plex/videos/kids-videos:/data/kids-videos \
-v /home/apham/plex/videos/general-videos:/data/general-videos \
linuxserver/plex

docker start plex


```
## Rentman

```
docker run -d --net primenet --ip 172.18.0.20 --name rentman rentman

```

## Teddycast

Download package

```

cd /home/apham/apps

wget https://github.com/itteddy/teddycast/releases/download/1.0.0/teddycast.tar.gz

tar xzvf teddycast.tar.gz

```

Run as systemD service

```
sudo vi /etc/systemd/system/teddycast.service
```

```
[Unit]
Description=teddycast

[Service]
WorkingDirectory=/home/apham/apps/teddycast
ExecStart=/bin/java -Xms64m -Xmx256m -jar /home/apham/apps/teddycast/quarkus-run.jar
User=apham
Type=simple
Restart=on-failure
RestartSec=10

[Install]
WantedBy=multi-user.target
```

```
sudo systemctl enable teddycast.service

sudo systemctl start teddycast.service 

```

## Traefik

```
docker run -d --name traefik --restart=unless-stopped --net primenet --ip 172.18.0.43 -p 80:80 -p 8080:8080 -v /home/apham/apps/traefik/config/traefik.yml:/etc/traefik/traefik.yml -v /var/run/docker.sock:/var/run/docker.sock traefik:latest

```

## Prometheus

```
mkdir -p /home/apham/apps/prometheus/data
mkdir -p /home/apham/apps/prometheus/config
sudo chown 65534:65534 /home/apham/apps/prometheus/data


docker run \
    -d --name prometheus --restart=unless-stopped  --net primenet --ip 172.18.0.70 \
    -v /home/apham/apps/prometheus/data:/prometheus \
    -v /home/apham/apps/prometheus/config/prometheus.yml:/etc/prometheus/prometheus.yml \
    prom/prometheus

```

## Grafana


Go to https://github.com/alainpham/app-archetypes/tree/master/camel-monitoring to get latest configs

```
docker run -d --name grafana \
    --restart=unless-stopped  --net primenet --ip 172.18.0.71 \
    -v /home/apham/apps/grafana/config/grafana-datasources.yml:/etc/grafana/provisioning/datasources/grafana-datasources.yml \
    -v /home/apham/apps/grafana/config/dashboards:/etc/grafana/provisioning/dashboards \
    grafana/grafana:latest 
```

## Heimdall

```
docker run --name=heimdall --restart=unless-stopped  --net primenet --ip 172.18.0.44 -d -v /home/apham/apps/heimdall/config:/config -e PGID=1000 -e PUID=1000 linuxserver/heimdall

```