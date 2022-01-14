

- [Common steps for all kinds of machines](#common-steps-for-all-kinds-of-machines)
  - [Setup user and ssh on server](#setup-user-and-ssh-on-server)
  - [Create self signed certifactes with root](#create-self-signed-certifactes-with-root)
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
  - [Nexus](#nexus)
  - [Docker registry](#docker-registry)
  - [Jenkins](#jenkins)
  - [Fileserver](#fileserver)
  - [AMQ Broker](#amq-broker)
  - [Blender](#blender)


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


sudo bash -c 'cat > /etc/docker/daemon.json << _EOF_
{
    "insecure-registries" : ["registry.hpel.lan" ],
    "dns": ["192.168.8.254", "8.8.8.8"]
}
_EOF_'

sudo systemctl restart docker

sudo curl -L https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp -o /usr/local/bin/yt-dlp
sudo chmod a+rx /usr/local/bin/yt-dlp

```

install latest ansible
sudo cp homelab-services/ansible/ansible-docker/dansible /usr/local/bin

## Create self signed certifactes with root

see this gist : https://gist.github.com/granella/01ba0944865d99227cf080e97f4b3cb6

```
keytool -genkey \
    -alias root  \
    -storepass password \
    -keyalg RSA \
    -keysize 2048 \
    -ext bc:c \
    -storetype PKCS12 \
    -dname "cn=*.ampi.lan" \
    -validity 365000 \
    -keystore tls/root.p12

keytool -genkey \
    -alias ca  \
    -storepass password \
    -keyalg RSA \
    -keysize 2048 \
    -ext bc:c \
    -storetype PKCS12 \
    -dname "cn=*.ampi.lan" \
    -validity 365000 \
    -keystore tls/ca.p12

keytool -exportcert -rfc -keystore tls/root.p12 -alias root -storepass password > tls/root.pem

keytool -keystore tls/ca.p12 -storepass password -certreq -alias ca \
| keytool -keystore tls/root.p12 -storepass password -gencert -alias root -ext bc=0 -ext san=dns:ca -rfc > tls/ca.pem


keytool -keystore tls/ca.p12 -storepass password -importcert -trustcacerts -noprompt -alias root -file tls/root.pem
keytool -keystore tls/ca.p12 -storepass password -importcert -alias ca -file tls/ca.pem

```

simple approach

```
keytool -genkey \
    -alias ${HOSTNAME}  \
    -storepass password \
    -keyalg RSA \
    -keysize 4096 \
    -storetype PKCS12 \
    -dname "cn=*.${HOSTNAME}.lan" \
    -validity 365000 \
    -keystore tls/${HOSTNAME}.p12

chmod 600 tls/${HOSTNAME}.p12

keytool -export \
    -alias ${HOSTNAME} \
    -rfc \
    -storepass password \
    -keystore tls/${HOSTNAME}.p12 \
    -file tls/${HOSTNAME}.pem

openssl pkcs12 -in tls/${HOSTNAME}.p12 -nodes -password pass:password -nocerts -out tls/${HOSTNAME}.key

keytool -import \
    -alias ${HOSTNAME} \
    -storepass password \
    -storetype PKCS12 \
    -noprompt \
    -keystore tls/${HOSTNAME}-truststore.p12 \
    -file tls/${HOSTNAME}.pem

keytool -list -storepass password -keystore tls/${HOSTNAME}.p12 -v
keytool -list -storepass password -keystore tls/${HOSTNAME}-truststore.p12 -v
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
    docker-compose \
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


sudo sh -c "$(curl -L  https://github.com/alainpham/appp/raw/master/mlvapp/install.sh )"
sudo sh -c "$(curl -L  https://github.com/alainpham/appp/raw/master/rawtherapee/install.sh )"
sudo sh -c "$(curl -L  https://github.com/alainpham/appp/raw/master/beeref/install.sh )"
sudo sh -c "$(curl -L  https://github.com/alainpham/appp/raw/master/viber/install.sh )"
sudo sh -c "$(curl -L  https://github.com/alainpham/appp/raw/master/plex-media-player/install.sh )"

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

sudo apt install dnsmasq git ansible docker.io docker-compose apparmor haproxy tmux vim maven openjdk-11-jdk prometheus-node-exporter
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
-v /home/apham/apps/pihole/etc-pihole:/etc/pihole \
-v /home/apham/apps/pihole/etc-dnsmasq.d:/etc/dnsmasq.d \
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
docker run -d --net primenet --restart=unless-stopped --ip 172.18.0.20 --name rentman registry.hpel.lan/rentman:latest

```

## Teddycast

```
docker run -d --net primenet --ip 172.18.0.21 \
  --name teddycast \
  -v /home/apham/plex/videos/general-videos:/videos \
  -v /home/apham/plex/music:/music \
  registry.hpel.lan/teddycast:latest
```

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

mkdir -p /home/apham/apps/traefik/config/

cat /home/apham/apps/traefik/config/traefik.template.yml | sed -E "s/HOSTNAME/${HOSTNAME}/">/home/apham/apps/traefik/config/traefik.yml
cat /home/apham/apps/traefik/config/tls.template.yml | sed -E "s/HOSTNAME/${HOSTNAME}/">/home/apham/apps/traefik/config/tls.yml

docker stop traefik 
docker rm traefik

docker run -d --name traefik --restart=unless-stopped \
  --net primenet --ip 172.18.0.43 \
  -p 80:80 -p 8080:8080 -p 443:443 \
  -v /home/apham/apps/traefik/config/traefik.yml:/etc/traefik/traefik.yml \
  -v /home/apham/apps/traefik/config/tls.yml:/config/tls.yml \
  -v /home/apham/apps/tls:/certs \
  -v /var/run/docker.sock:/var/run/docker.sock \
  traefik:latest



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

## Nexus

```

mkdir -p /home/apham/apps/nexus/data && chown -R 200 /home/apham/apps/nexus/data

docker run --name nexus --restart=unless-stopped \
    -d --net primenet --ip 172.18.0.41 -v /home/apham/apps/nexus/data:/nexus-data \
	sonatype/nexus3


curl -X 'POST' -u admin:admin \
  "http://nexus.${HOSTNAME}.lan/service/rest/v1/repositories/maven/proxy" \
  -H 'accept: application/json' \
  -H 'Content-Type: application/json' \
  -d '{
  "name": "red-hat-ga-repository",
  "online": true,
  "storage": {
    "blobStoreName": "default",
    "strictContentTypeValidation": true
  },
  "proxy": {
    "remoteUrl": "https://maven.repository.redhat.com/ga",
    "contentMaxAge": 1440,
    "metadataMaxAge": 1440
  },
  "negativeCache": {
    "enabled": true,
    "timeToLive": 1440
  },
  "httpClient": {
    "blocked": false,
    "autoBlock": true
  },
  "maven": {
    "versionPolicy": "RELEASE",
    "layoutPolicy": "STRICT",
    "contentDisposition": "INLINE"
  }
}'

curl -X 'POST' -u admin:admin \
  "http://nexus.${HOSTNAME}.lan/service/rest/v1/repositories/maven/proxy" \
  -H 'accept: application/json' \
  -H 'Content-Type: application/json' \
  -d '{
  "name": "red-hat-early-access-repository",
  "online": true,
  "storage": {
    "blobStoreName": "default",
    "strictContentTypeValidation": true
  },
  "proxy": {
    "remoteUrl": "https://maven.repository.redhat.com/earlyaccess/all",
    "contentMaxAge": 1440,
    "metadataMaxAge": 1440
  },
  "negativeCache": {
    "enabled": true,
    "timeToLive": 1440
  },
  "httpClient": {
    "blocked": false,
    "autoBlock": true
  },
  "maven": {
    "versionPolicy": "RELEASE",
    "layoutPolicy": "STRICT",
    "contentDisposition": "INLINE"
  }
}'

curl -X 'PUT' -v -u admin:admin \
  "http://nexus.${HOSTNAME}.lan/service/rest/v1/repositories/maven/group/maven-public" \
  -H 'accept: application/json' \
  -H 'Content-Type: application/json' \
  -d '
      {
        "name": "maven-public",
        "online": true,
        "storage": {
          "blobStoreName": "default",
          "strictContentTypeValidation": true
        },
        "group": {
          "memberNames": [
            "maven-releases",
            "maven-snapshots",
            "maven-central",
            "red-hat-ga-repository",
            "red-hat-early-access-repository"
          ]
        }
      }'

{
  "name": "maven-public",
  "format": "maven2",
  "url": "http://nexus.hpel.lan/repository/maven-public",
  "online": true,
  "storage": {
    "blobStoreName": "default",
    "strictContentTypeValidation": true
  },
  "group": {
    "memberNames": [
      "maven-releases",
      "maven-snapshots",
      "maven-central"
    ]
  },
  "type": "group"
}


for deleting

curl -X 'DELETE' -u admin:admin \
  "http://nexus.${HOSTNAME}.lan/service/rest/v1/repositories/red-hat-ga-repository" \
  -H 'accept: application/json'

curl -X 'DELETE' -u admin:admin \
  "http://nexus.${HOSTNAME}.lan/service/rest/v1/repositories/red-hat-early-access-repository" \
  -H 'accept: application/json'
```

## Docker registry

```

mkdir -p /home/apham/apps/registry/data
sudo rm -rf /home/apham/apps/registry/data*
app=registry
docker stop $app
docker rm $app
docker run -d --net primenet --ip 172.18.0.42 --restart unless-stopped --name registry -v /home/apham/apps/registry/data:/var/lib/registry -e REGISTRY_STORAGE_DELETE_ENABLED=true \
  -l "traefik.http.routers.$app.entrypoints=https" \
  -l "traefik.http.routers.$app.tls=true" \
  -l "traefik.http.routers.$app.rule=Host(\`$app.${HOSTNAME}.lan\`)" \
  -l "traefik.http.routers.$app.service=$app" \
  registry

docker run -d --net primenet --ip 172.18.0.45 --restart unless-stopped --name regui -e NGINX_PROXY_PASS_URL=http://registry:5000 -e DELETE_IMAGES=true -e SINGLE_REGISTRY=true  joxit/docker-registry-ui

to garbage collect storage
bin/registry garbage-collect  /etc/docker/registry/config.yml  

```

## Jenkins

```
mkdir -p /home/apham/apps/jenkins/data
docker run -d --net primenet --ip 172.18.0.46 --name jenkins --restart unless-stopped  -v /home/apham/apps/jenkins/data:/var/jenkins_home jenkins/jenkins:lts-jdk11
```

## Fileserver

```
mkdir -p /home/apham/apps/fileserver/data
sudo chown 1001 /home/apham/apps/fileserver/data

docker stop fileserver
docker rm fileserver
docker run -d --net primenet --ip 172.18.0.47 --name fileserver --restart unless-stopped \
  -v  /home/apham/apps/fileserver/data:/deployments/datafolder \
  alainpham/fileserver:latest

docker stop fileserver
docker rm fileserver
docker run -d --net primenet --ip 172.18.0.47 --name fileserver --restart unless-stopped \
  -v /home/apham/apps/fileserver/data:/deployments/datafolder \
  -l "traefik.http.routers.fileserver.entrypoints=http" \
  -l "traefik.http.routers.fileserver.tls=false" \
  -l "traefik.http.routers.fileserver.rule=Host(\`fileserver.${HOSTNAME}.lan\`)" \
  -l "traefik.http.routers.fileserver.service=fileserver" \
  alainpham/fileserver:latest


docker stop fileserver
docker rm fileserver
docker run -d --net primenet --ip 172.18.0.47 --name fileserver --restart unless-stopped \
  -v /home/apham/apps/fileserver/data:/deployments/datafolder \
  -v /home/apham/apps/tls/${HOSTNAME}.p12:/tls/${HOSTNAME}.p12:ro \
  -e SERVER_SSL_KEY_STORE=/tls/${HOSTNAME}.p12 \
  -e SERVER_SSL_KEY-STORE-PASSWORD=password \
  -e SERVER_SSL_KEYSTORETYPE=PKCS12 \
  -e SERVER_HTTP2_ENABLED=true \
  -e SERVER_HTTP2_USE_FORWARD_HEADERS=true \
  -l "traefik.enable=true" \
  -l "traefik.tcp.services.fileserver.loadbalancer.server.port=8080" \
  -l "traefik.tcp.routers.fileserver.entrypoints=https" \
  -l "traefik.tcp.routers.fileserver.tls=true" \
  -l "traefik.tcp.routers.fileserver.tls.passthrough=true" \
  -l "traefik.tcp.routers.fileserver.rule=HostSNI(\`fileserver.${HOSTNAME}.lan\`)" \
  -l "traefik.tcp.routers.fileserver.service=fileserver" \
  alainpham/fileserver:latest

docker stop fileserver
docker rm fileserver
docker run -d --net primenet --ip 172.18.0.47 --name fileserver --restart unless-stopped \
  -v /home/apham/apps/fileserver/data:/deployments/datafolder \
  -e SERVER_HTTP2_ENABLED=true \
  -l "traefik.enable=true" \
  -l "traefik.http.services.fileserver.loadbalancer.server.port=8080" \
  -l "traefik.http.routers.fileserver.entrypoints=http" \
  -l "traefik.http.routers.fileserver.tls=false" \
  -l "traefik.http.routers.fileserver.rule=Host(\`fileserver.${HOSTNAME}.lan\`)" \
  -l "traefik.http.routers.fileserver.service=fileserver" \
  alainpham/fileserver:latest

docker stop web-socket-test
docker rm web-socket-test
docker run -d -p 8010:8010 --name web-socket-test  \
  -l "traefik.enable=true" \
  
  -l "traefik.http.services.sck.loadbalancer.server.port=8010" \
  -l "traefik.http.routers.sck.entrypoints=https" \
  -l "traefik.http.routers.sck.tls=true" \
  -l "traefik.http.routers.sck.rule=Host(\`sck.${HOSTNAME}.lan\`)" \
  -l "traefik.http.routers.sck.service=sck" \
  ksdn117/web-socket-test


  -l "traefik.http.services.sck.loadbalancer.sticky.cookie=true" \
  -l "traefik.http.services.sck.loadbalancer.sticky.cookie.secure=true" \
  -l "traefik.http.middlewares.sslheader.headers.customrequestheaders.X-Forwarded-Proto=https" \
  -l "traefik.http.routers.sck.middlewares=sslheader@docker" \

```

## AMQ Broker

```
docker stop amqbroker
docker rm amqbroker
docker run  --restart unless-stopped \
    -e AMQ_USER="adm" \
    -e AMQ_PASSWORD="password" \
    -e AMQ_ROLE="admin" \
    -e AMQ_REQUIRE_LOGIN="false" \
    -e AMQ_ENABLE_METRICS_PLUGIN="true" \
    -d --name amqbroker  \
    -d --net primenet --ip 172.18.0.115 \
    -l "traefik.enable=true" \
    -l "traefik.tcp.services.amqbroker.loadbalancer.server.port=61616" \
    -l "traefik.tcp.routers.amqbroker.entrypoints=https" \
    -l "traefik.tcp.routers.amqbroker.tls=true" \
    -l "traefik.tcp.routers.amqbroker.rule=HostSNI(\`amqbroker.${HOSTNAME}.lan\`)" \
    -l "traefik.tcp.routers.amqbroker.service=amqbroker" \
    -l "traefik.http.services.amqconsole.loadbalancer.server.port=8161" \
    -l "traefik.http.routers.amqconsole.entrypoints=http" \
    -l "traefik.http.routers.amqconsole.tls=false" \
    -l "traefik.http.routers.amqconsole.rule=Host(\`amqconsole.${HOSTNAME}.lan\`)" \
    -l "traefik.http.routers.amqconsole.service=amqconsole" \
    registry.redhat.io/amq7/amq-broker:latest
```


## Blender
docker run -d --net primenet --name bqueue -e QUARKUS_ARTEMIS_URL=tcp://amqbroker:61616?consumerWindowSize=0 -e BLENDERQUEUE_HOSTNAME=alpha bqueue
