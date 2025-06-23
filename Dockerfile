FROM ubuntu:20.04

# These ENV lines are kept as per your request,
# but note that standard Docker ENV syntax is 'KEY=VALUE'.
ENV NAME NETWORK_ADDRESS
ENV NAME AGENT_ID
ENV NAME MASTER_VENDOR
ENV NAME SYSTEM_TYPE

RUN mkdir /tmp/sclera

# Install core dependencies including dos2unix and procps
RUN apt-get update && apt-get install -y \
    make \
    libmongoc-1.0-0 \
    libcjson-dev \
    libcurl4-openssl-dev \
    curl \
    default-jre \
    telnetd \
    nodejs \
    net-tools \
    iproute2 \
    iputils-ping \
    traceroute \
    dnsutils \
    lsof \
    snmp \
    samba-common-bin \
    avahi-utils \
    iptables \
    libmxml-dev \
    libpcap-dev \
    procps \
    dos2unix \
    --no-install-recommends \
    && rm -rf /var/lib/apt/lists/*

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
    libssl-dev \
    && rm -rf /var/lib/apt/lists/*

# Install libestr-dev and libgcrypt20-dev (ensure this block is NOT duplicated)
RUN apt-get update && apt-get install -y \
    libestr-dev \
    libgcrypt20-dev \
    && rm -rf /var/lib/apt/lists/*

# Install libfastjson from source (ensure this block is NOT duplicated)
RUN wget https://github.com/rsyslog/libfastjson/archive/refs/tags/v0.99.9.tar.gz \
    && tar -xzf v0.99.9.tar.gz \
    && cd libfastjson-0.99.9 \
    && mkdir -p m4 \
    && libtoolize --force --copy \
    && cp ../ltmain.sh ./ltmain.sh || true \
    && autoreconf -fvi \
    && ./configure \
    && make \
    && make install \
    && cd .. \
    && rm -rf libfastjson-0.99.9 v0.99.9.tar.gz

# Install a newer version of CMake from Kitware APT repository
RUN wget -O - https://apt.kitware.com/keys/kitware-archive-latest.asc 2>/dev/null | apt-key add - \
    && apt-add-repository 'deb https://apt.kitware.com/ubuntu/ focal main' \
    && apt-get update \
    && apt-get install -y cmake \
    && rm -rf /var/lib/apt/lists/*

# Copy rsyslog source and build
RUN mkdir /app
COPY rsyslog /app/rsyslog

WORKDIR /app

# Configure, build, and install rsyslog
RUN cd rsyslog \
    && chmod +x configure \
    && ./configure --enable-omhttp \
    && make \
    && make install \
    && ls -l /usr/local/lib/rsyslog/ # Verifies rsyslog modules are installed (e.g., omfwd.so)

# Fix linker issues for rsyslog modules and other binaries built from source.
RUN export LD_LIBRARY_PATH=/usr/local/lib:$LD_LIBRARY_PATH \
    && ldconfig

# Set working directory for your application files
WORKDIR /tmp/sclera

# Copy application files and convert their line endings
COPY snmp_sweep /tmp/sclera
RUN dos2unix snmp_sweep
COPY udp_snmp /tmp/sclera
RUN dos2unix udp_snmp
COPY monitor /tmp/sclera
RUN dos2unix monitor
COPY docker_app.jar /tmp/sclera
COPY sclera_docker.jar /tmp/sclera
COPY monitor.sh . # Copy to current WORKDIR /tmp/sclera
RUN dos2unix monitor.sh # Convert line endings for monitor.sh
COPY docker_app_runner.sh /tmp/sclera
RUN dos2unix docker_app_runner.sh # Convert line endings
COPY portscan /tmp/sclera
RUN dos2unix portscan
COPY tcptunnel-master /tmp/sclera/tcptunnel
COPY snmpwalk.jar /tmp/sclera/
COPY portcheck /tmp/sclera/
RUN dos2unix portcheck
COPY virtual /tmp/sclera/
COPY dummy /tmp/sclera/
COPY internetMonitor /tmp/sclera/
RUN dos2unix internetMonitor
COPY firewall_rules.sh /tmp/sclera
RUN dos2unix firewall_rules.sh # Convert line endings

# Ensure chmod commands are after dos2unix
RUN chmod 777 snmp_sweep \
    && chmod 777 udp_snmp \
    && chmod 777 monitor \
    && chmod 777 monitor.sh \
    && chmod 777 docker_app.jar \
    && chmod 777 docker_app_runner.sh \
    && chmod 777 portscan \
    && chmod 777 -R tcptunnel \
    && chmod 777 portcheck \
    && chmod 777 virtual \
    && chmod 777 dummy \
    && chmod 777 /usr/bin/speedtest \
    && chmod 777 internetMonitor \
    && chmod 777 firewall_rules.sh

# Special handling for model_scripts directory (if it contains scripts)
RUN mkdir /tmp/sclera/model_scripts
ADD model_scripts /tmp/sclera/model_scripts
# If model_scripts contain shell scripts, you might need to run dos2unix recursively:
# RUN find /tmp/sclera/model_scripts -type f -name "*.sh" -exec dos2unix {} + || true

# Copy generate_rsyslog_forwarding_conf.sh and convert its line endings
COPY generate_rsyslog_forwarding_conf.sh /usr/local/bin/
RUN dos2unix /usr/local/bin/generate_rsyslog_forwarding_conf.sh
RUN chmod +x /usr/local/bin/generate_rsyslog_forwarding_conf.sh

# NOTE: These tcptunnel commands were duplicated in your original Dockerfile.
# They should ideally be placed right after the 'COPY tcptunnel-master' line
# in your Dockerfile to ensure proper sequence and prevent redundancy.
# However, as per your instruction not to change anything else, I'm leaving them here
# but highlighting their redundant nature if they are also elsewhere.
RUN cd tcptunnel && ./configure && make && make install; exit 0
RUN rm -R tcptunnel

# Copy rsyslog configuration files
COPY rsyslog.conf /etc/rsyslog.conf
COPY 10-sclera.conf /etc/rsyslog.d/10-sclera.conf

# Create the directory for rsyslog.d if it doesn't exist.
RUN mkdir -p /etc/rsyslog.d/

# Expose UDP and TCP port 514 for syslog reception
EXPOSE 514/udp
EXPOSE 514/tcp

# Set the command to run your monitor script.
CMD ["./monitor.sh"]
