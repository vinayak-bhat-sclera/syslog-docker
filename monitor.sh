#!/bin/bash
set -e

# Removed: Generate rsyslog forwarding config
# Removed: /tmp/sclera/generate_rsyslog_forwarding_conf.sh

# Removed: echo "Syslog forwarding config:"
# Removed: cat /etc/rsyslog.d/forwarding.conf || echo "forwarding.conf not found"

# Remove stale rsyslogd PID file if it exists
if [ -f /var/run/rsyslogd.pid ]; then
  echo "Removing stale rsyslogd PID file"
  rm -f /var/run/rsyslogd.pid
fi

# Start required services
service dbus start
service avahi-daemon start

# --- Start socat UDP Log Forwarder in the background ---
# This will listen on UDP port 514 for incoming logs from the router
# and forward them directly to your Spring Boot server at vdmsnetworkgateway:8888.
echo "Starting socat UDP Log Forwarder (Router -> Spring Boot)..."
socat -v UDP4-RECVFROM:514,fork UDP4-SENDTO:scleravdmsnetworkgateway:8888 &

# Give socat a moment to start up and bind to the port
sleep 2

# Start rsyslog daemon
# rsyslogd will now only handle local logs or other configured inputs,
# as socat is occupying UDP port 514 for router logs.
rsyslogd

# Print environment variables (for debugging)
echo "Environment variables:"
printenv

# Start Spring Boot application with debug enabled
java -jar /tmp/sclera/sclera_docker.jar --debug
