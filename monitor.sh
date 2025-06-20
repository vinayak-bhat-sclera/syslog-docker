#!/bin/sh

export LD_LIBRARY_PATH=/usr/local/lib:$LD_LIBRARY_PATH
echo "LD_LIBRARY_PATH set to: $LD_LIBRARY_PATH"

# Ensure the directory for the generated rsyslog configuration exists
mkdir -p /etc/rsyslog.d/

# Generate rsyslog forwarding configuration using environment variables.
# These environment variables will be injected by Docker from the --env-file.
echo "Generating rsyslog forwarding configuration from environment variables..."
/bin/bash /usr/local/bin/generate_rsyslog_forwarding_conf.sh

# --- Start other background services ---
echo "Starting ./monitor (your other local monitor script)..."
./monitor -i "$NETWORK_ADDRESS" -m -C -D -v "$AGENT_ID" -n "$DOCKER_ID" &

echo "Starting docker_app_runner.sh..."
/bin/bash docker_app_runner.sh &

echo "Starting firewall_rules.sh..."
/bin/bash firewall_rules.sh &

echo "Starting dbus..."
/etc/init.d/dbus start &

# Avahi daemon is commented out as it often causes issues in Docker.
# If you need it, you'll have to debug its specific setup for containerized environments.
# echo "Starting avahi-daemon..."
# /etc/init.d/avahi-daemon start &

# Start rsyslogd in the background.
# This is the primary process we want to keep running to ensure logs are forwarded.
echo "Starting rsyslogd..."
rsyslogd -n -f /etc/rsyslog.conf &
RSYSLOG_PID=$! # Capture the PID of the rsyslogd process

echo "Rsyslogd started with PID: $RSYSLOG_PID"

# --- Start your main Java application in the BACKGROUND ---
# It's important to run this in the background (`&`) so that monitor.sh can
# continue to the `wait` command and keep the container alive, even if the Java app crashes.
echo "Starting sclera_docker.jar in background..."
# Redirect Java app output to /dev/null to prevent excessive logging filling Docker logs
# if the app has issues, and to ensure it doesn't accidentally interfere with monitor.sh's output.
java -jar sclera_docker.jar > /dev/null 2>&1 &
JAVA_APP_PID=$! # Capture PID for reference, though not strictly used by 'wait' below.

echo "Sclera Docker Java app initiated in background with PID: $JAVA_APP_PID"

# --- Keep the monitor.sh script (and thus the container) alive ---
# The container's lifecycle will now be tied to the rsyslogd process.
# If rsyslogd ever stops, the 'wait' command completes, and the container exits.
echo "Monitor script waiting for rsyslogd (PID: $RSYSLOG_PID) to remain active. Container will run as long as rsyslogd runs."
wait "$RSYSLOG_PID"

echo "Rsyslogd process terminated. Exiting monitor.sh and stopping container."
