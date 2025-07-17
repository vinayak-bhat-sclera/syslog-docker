FROM ubuntu:20.04

ENV NAME NETWORK_ADDRESS
ENV NAME AGENT_ID
ENV NAME MASTER_VENDOR
ENV NAME SYSTEM_TYPE

RUN mkdir /tmp/sclera

COPY snmp_sweep /tmp/sclera
COPY udp_snmp /tmp/sclera
COPY monitor /tmp/sclera
COPY docker_app.jar /tmp/sclera
COPY sclera_docker.jar /tmp/sclera
COPY monitor.sh /tmp/sclera
COPY docker_app_runner.sh /tmp/sclera
COPY portscan /tmp/sclera
COPY tcptunnel-master /tmp/sclera/tcptunnel
COPY snmpwalk.jar /tmp/sclera/
COPY portcheck /tmp/sclera/
COPY virtual /tmp/sclera/
COPY dummy /tmp/sclera/
COPY internetMonitor /tmp/sclera/
COPY firewall_rules.sh /tmp/sclera/
RUN mkdir /tmp/sclera/model_scripts
ADD model_scripts /tmp/sclera/model_scripts
COPY speedtest /usr/bin

RUN chmod +x /tmp/sclera/internetMonitor

WORKDIR /tmp/sclera

RUN apt-get update
RUN apt-get -y install make
RUN apt-get -y install libmongoc-1.0-0
RUN apt-get -y install libcjson-dev
RUN apt-get -y install libcurl4-openssl-dev
RUN apt-get -y install curl
RUN apt-get -y install default-jre
RUN apt-get install telnetd -y
RUN curl -sL https://deb.nodesource.com/setup_12.x | bash -
RUN apt-get install -y nodejs
RUN apt-get -y install net-tools
RUN apt-get -y install iproute2
RUN apt-get -y install iputils-ping
RUN apt-get -y install traceroute
RUN apt-get -y install dnsutils
RUN apt-get -y install lsof
RUN apt-get -y install snmp
RUN apt-get -y install samba-common-bin
RUN apt-get -y install avahi-utils
RUN apt-get install -y iptables
RUN apt-get install -y libmxml-dev
RUN apt-get install -y libpcap-dev

# Install socat
RUN apt-get -y install socat

RUN chmod 777 snmp_sweep
RUN chmod 777 udp_snmp
RUN chmod 777 monitor
RUN chmod 777 monitor.sh
RUN chmod 777 docker_app.jar
RUN chmod 777 docker_app_runner.sh
RUN chmod 777 portscan
RUN chmod 777 -R tcptunnel
RUN chmod 777 portcheck
RUN chmod 777 virtual
RUN chmod 777 dummy
RUN chmod 777 /usr/bin/speedtest
RUN chmod 777 internetMonitor
RUN chmod 777 -R model_scripts
RUN chmod 777 firewall_rules.sh

RUN cd tcptunnel && ./configure && make && make install; exit 0
RUN rm -R tcptunnel

# Install dependencies required to add repositories and build from source
RUN apt-get update && apt-get install -y \
    software-properties-common \
    gnupg \
    wget \
    build-essential \
    autoconf \
    automake \
    libtool \
    zlib1g-dev \
    uuid-dev \
    pkg-config \
    libcurl4-openssl-dev \
    libssl-dev \
    && rm -rf /var/lib/apt/lists/*

# Install libestr-dev and libgcrypt20-dev which are available in the default repositories
RUN apt-get update && apt-get install -y \
    libestr-dev \
    libgcrypt20-dev \
    && rm -rf /var/lib/apt/lists/*

# Install libfastjson from source
RUN apt-get update && apt-get install -y \
    autoconf \
    automake \
    libtool \
    pkg-config \
    m4 \
    && wget https://github.com/rsyslog/libfastjson/archive/refs/tags/v0.99.9.tar.gz \
    && tar -xzf v0.99.9.tar.gz \
    && cd libfastjson-0.99.9 \
    && mkdir -p m4 \
    && libtoolize --force --copy \
    && cp ../ltmain.sh ./ltmain.sh || true \
    && autoreconf -fvi \
    && ./configure \
    && make \
    && make install \
    && cd prolifer \
    && make clean \
    && make \
    && make install \
    && cd ../.. \
    && rm -rf libfastjson-0.99.9 v0.99.9.tar.gz

# Install a newer version of CMake from Kitware APT repository
RUN apt-get update && apt-get install -y \
    apt-transport-https \
    ca-certificates \
    gnupg \
    && rm -rf /var/lib/apt/lists/*

RUN wget -O - https://apt.kitware.com/keys/kitware-archive-latest.asc 2>/dev/null | apt-key add - \
    && apt-add-repository 'deb https://apt.kitware.com/ubuntu/ focal main' \
    && apt-get update \
    && apt-get install -y cmake \
    && rm -rf /var/lib/apt/lists/*

# Prepare the app directory and copy rsyslog source
RUN mkdir /app
COPY rsyslog /app/rsyslog

WORKDIR /app

# Configure, build, and install rsyslog
RUN cd rsyslog \
    && chmod +x configure \
    && ./configure \
    && make \
    && make install

# Copy configuration files
# Removed: COPY generate_rsyslog_forwarding_conf.sh /tmp/sclera/
# Removed: COPY rsyslog.env /etc/rsyslog.env
COPY rsyslog.conf /etc/rsyslog.conf
#COPY 10-sclera.conf /etc/rsyslog.d/10-sclera.conf

# Optional: You can set environment here if needed
# ENV FORWARDING_SERVER_1=10.2.31.30

# Removed: RUN chmod +x /tmp/sclera/generate_rsyslog_forwarding_conf.sh

# Expose UDP and TCP for syslog (router will send to 514)
EXPOSE 514/udp
EXPOSE 514/tcp

# Fix linker issues
RUN export LD_LIBRARY_PATH=/usr/local/lib:$LD_LIBRARY_PATH \
    && ldconfig

# Copy and configure monitor.sh as entrypoint
COPY monitor.sh /monitor.sh
RUN chmod +x /monitor.sh

WORKDIR /tmp/sclera
ENTRYPOINT ["/monitor.sh"]
