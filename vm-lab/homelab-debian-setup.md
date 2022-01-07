

- [Common steps for all kinds of machines](#common-steps-for-all-kinds-of-machines)
  - [Setup user and ssh on server](#setup-user-and-ssh-on-server)
- [Pure Server](#pure-server)
- [Workstation](#workstation)
  - [Initial install](#initial-install)
  - [Graphical Desktop Preferences](#graphical-desktop-preferences)
    - [General](#general)
    - [with Trackpad](#with-trackpad)
  - [Konsole setup](#konsole-setup)
  - [Configure Dolphine](#configure-dolphine)
  - [Configure Panel](#configure-panel)
  - [Install packages](#install-packages)
    - [Main packages](#main-packages)
    - [Apps packages](#apps-packages)
    - [Optional packages to be reviewed if necessary](#optional-packages-to-be-reviewed-if-necessary)
- [RPI Server](#rpi-server)
- [Services & Apps](#services--apps)
  - [Pihole](#pihole)
  - [Plex](#plex)
  - [Rentman](#rentman)
  - [Teddycast](#teddycast)
  - [Traefik](#traefik)
  - [Prometheus](#prometheus)
  - [Grafana](#grafana)
  - [Heimdall](#heimdall)


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

```


sudo adduser apham libvirt
sudo groupadd docker
sudo usermod -aG docker apham

docker network create --driver=bridge --subnet=172.18.0.0/16 --gateway=172.18.0.1 primenet

sudo curl -L https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp -o /usr/local/bin/yt-dlp
sudo chmod a+rx /usr/local/bin/yt-dlp

```

# Pure Server


Install base packages

```

sudo apt install -y \
    qemu-system \
    qemu-utils \
    virtinst \
    libvirt-clients \
    libvirt-daemon-system \
    bridge-utils \
    libosinfo-bin \
    dnsmasq \
    ncdu \
    git \
    ansible \
    docker.io \
    apparmor \
    haproxy \
    tmux \
    vim \
    maven \
    openjdk-11-jdk \
    prometheus-node-exporter \
    htop \
    curl \
    lshw \
    mediainfo \
    linux-cpupower 
    

```


# Workstation

## Initial install

* Install debian from scrach
  * choose Graphics
  * KDE Plasma
  * SSH server
  * Standard utils

## Graphical Desktop Preferences

### General

Go to system settings

* Appearance -> Global Theme
  * Choose Breeze Dark
* Workspace Behavior -> Virtual Desktop
  * 2 rows, 4 Desktops
* Workspace Behavior -> General Behavior
  *  Double-click to open files and folders
*  Start up and shutdown -> Login Screen
   *  Choose Chilli for Plasma
*  Start up and shutdown -> Desktop Session
   *  Start with an empty
*  Display and Monitor -> Compositor
   *  disable "enable compositor on startup"
*  (optional )Application Style -> Windows Decorations
   *  Uncheck Use themes default border size
   *  normal size

### with Trackpad

* Input Devices -> Touchpad
  * Mouse Click Emulation

## Konsole setup

* create default profile
  * General
    * /bin/bash
    * start in sale directory as current session
    * size 150 30
  * Appearance
    * Green on Black
    * Font Hack 12
  * Scrolling
    * Unlimited

## Configure Dolphine

* Startup
  * Show on startup
    * /home/apham
  * Check Show full path inside location bar
  * Uncheck Open new folders in tabs
* Switch to detailed view
* Add type column
* Show terminal
* Show infos

## Configure Panel

* Switch to task manger rather than icon only
* height 60
* always 2 rows

## Install packages

### Main packages
```
sudo apt install -y \
    vlc \
    ffmpeg \
    mpv \
    python3-mutagen \
    arandr \
    jackd2 \
    qjackctl \
    pulseaudio-module-jack  \
    ardour \
    v4l-utils \
    flatpak \
    snapd \
    virt-manager \
    krfb \
    krdc \
    mediainfo-gui

sudo apt install -y v4l2loopback-utils

sudo apt install -y vainfo intel-media-va-driver-non-free

sudo apt -y remove termit

```

### Apps packages

```

wget https://dl.google.com/linux/direct/google-chrome-stable_current_amd64.deb
sudo apt install ./google-chrome-stable_current_amd64.deb
sudo apt install easytag

sudo snap install obs-studio
sudo snap install dbeaver-ce
sudo snap install code --classic
sudo snap install postman
sudo snap install kdenlive
sudo snap install telegram-desktop
sudo snap install blender --classic
sudo snap install sweethome3d-homedesign
sudo snap install beekeeper-studio


sudo sh -c "$(curl -L  https://github.com/alainpham/aaap/raw/master/mlvapp/install.sh )"
sudo sh -c "$(curl -L  https://github.com/alainpham/aaap/raw/master/rawtherapee/install.sh )"
sudo sh -c "$(curl -L  https://github.com/alainpham/aaap/raw/master/beeref/install.sh )"
sudo sh -c "$(curl -L  https://github.com/alainpham/aaap/raw/master/viber/install.sh )"
sudo sh -c "$(curl -L  https://github.com/alainpham/aaap/raw/master/plex-media-player/install.sh )"

```

### Optional packages to be reviewed if necessary

```
optional stuff :
apt install \
    vdpauinfo 
    mesa-utils 
    intel-media-va-driver-non-free
    firmware-linux 
    intel-microcode
    intel-gpu-tools  






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