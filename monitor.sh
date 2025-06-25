#!/bin/bash
set -e

# Generate rsyslog forwarding config
/tmp/sclera/generate_rsyslog_forwarding_conf.sh

echo "Syslog forwarding config:"
cat /etc/rsyslog.d/forwarding.conf || echo "forwarding.conf not found"

# Remove stale rsyslogd PID file if it exists
if [ -f /var/run/rsyslogd.pid ]; then
  echo "Removing stale rsyslogd PID file"
  rm -f /var/run/rsyslogd.pid
fi

# Start required services
service dbus start
service avahi-daemon start

# Start rsyslog daemon
rsyslogd

# Print environment variables (for debugging)
echo "Environment variables:"
printenv

# Start Spring Boot application with debug enabled
java -jar /tmp/sclera/sclera_docker.jar --debug
