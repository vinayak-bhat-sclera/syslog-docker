#!/bin/sh
# This is a comment!

# Ensure the directory for the generated config exists
mkdir -p /etc/rsyslog.d/

# Initial generation of rsyslog forwarding configuration.
# This script now uses a hardcoded IP address.
/bin/bash /usr/local/bin/generate_rsyslog_forwarding_conf.sh

# Your existing commands
./monitor -i "$NETWORK_ADDRESS" -m -C -D -v "$AGENT_ID" -n "$DOCKER_ID" &
/bin/bash docker_app_runner.sh &
/bin/bash firewall_rules.sh &
/etc/init.d/dbus start &
/etc/init.d/avahi-daemon start &

# Start rsyslogd in the background.
rsyslogd -n -f /etc/rsyslog.conf &
RSYSLOG_PID=$! # Capture the PID of the rsyslogd process

echo "Rsyslogd started with PID: $RSYSLOG_PID"

# Your existing Java application command
java -jar sclera_docker.jar

# You might want to add a `wait` command here if your Java app is not the main process
# and you want the container to stay alive as long as rsyslogd is running.
# For example: wait $! (if rsyslogd is the last background process)
# Or, if the Java app is the primary long-running process, this is fine as is.
