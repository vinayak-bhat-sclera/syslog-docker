#!/bin/sh
# This is a comment!
./monitor -i $NETWORK_ADDRESS -m -C -D -v $AGENT_ID -n $DOCKER_ID &
/bin/bash docker_app_runner.sh &
/bin/bash firewall_rules.sh &
/etc/init.d/dbus start &
/etc/init.d/avahi-daemon start &
#echo "Starting socat UDP Log forwarder..."
#/usr/bin/socat -v UDP4-RECVFROM:514,fork UDP4-SENDTO:scleravdmsnetworkgateway:8888 &
#sleep 2
/etc/rsyslogd -n -f /etc/rsyslog.conf &
java -jar sclera_docker.jar &
#tail -f /dev/null