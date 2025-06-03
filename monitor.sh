#!/bin/sh
# This is a comment!
./monitor -i $NETWORK_ADDRESS -m -C -D -v $AGENT_ID -n $DOCKER_ID &
/bin/bash docker_app_runner.sh &
/bin/bash firewall_rules.sh &
/etc/init.d/dbus start
/etc/init.d/avahi-daemon start
rsyslogd -n -f /etc/rsyslog.conf & # rsyslogd will now read the environment variables from its config
java -jar sclera_docker.jar 



