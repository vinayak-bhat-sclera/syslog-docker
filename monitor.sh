#!/bin/sh
# This script manages the startup of various services within the Docker container.
# It ensures rsyslog configuration is generated and other services are started.
# Crucially, it keeps the container alive as long as the rsyslogd daemon is running,
# even if other applications (like the Java app) exit.

# Ensure the directory for the generated rsyslog configuration exists
mkdir -p /etc/rsyslog.d/

# Generate rsyslog forwarding configuration using environment variables.
# This script reads SYSLOG_TARGET_IP_1, SYSLOG_TARGET_IP_2, etc.
echo "Generating rsyslog forwarding configuration from environment variables..."
# Use the full path for generate_rsyslog_forwarding_conf.sh
/usr/local/bin/generate_rsyslog_forwarding_conf.sh

# --- Start other background services ---
echo "Starting ./monitor (your other local monitor script)..."
./monitor -i "$NETWORK_ADDRESS" -m -C -D -v "$AGENT_ID" -n "$DOCKER_ID" &

echo "Starting docker_app_runner.sh..."
/bin/bash docker_app_runner.sh &

echo "Starting firewall_rules.sh..."
/bin/bash firewall_rules.sh &

echo "Starting dbus..."
/etc/init.d/dbus start &

# Avahi daemon is often not needed in Docker and causes issues.
# It's commented out here to prevent "Wrong IP address!!" errors and simplify startup.
# echo "Starting avahi-daemon..."
# /etc/init.d/avahi-daemon start & # Commented out

# --- IMPORTANT: Set LD_LIBRARY_PATH for rsyslog to find its modules ---
# Re-export right before rsyslogd for robustness, in case previous exports are lost.
export LD_LIBRARY_PATH=/usr/local/lib:$LD_LIBRARY_PATH
echo "LD_LIBRARY_PATH set to: $LD_LIBRARY_PATH"

# Start rsyslogd in the background.
# This is the primary process we want to keep running to ensure logs are forwarded.
echo "Starting rsyslogd..."
# Use the full path for rsyslogd for robustness
/usr/local/sbin/rsyslogd -n -f /etc/rsyslog.conf &
RSYSLOG_PID=$! # Capture the PID of the rsyslogd process

echo "Rsyslogd started with PID: $RSYSLOG_PID"

# --- Start your main Java application in the BACKGROUND ---
# It's important to run this in the background (`&`) so that monitor.sh can
# continue to the `wait` command and keep the container alive, even if the Java app crashes.
echo "Starting sclera_docker.jar in background..."
# Redirect Java app output to /dev/null to prevent excessive logging filling Docker logs
# if the app has issues, and to ensure it doesn't accidentally interfere with monitor.sh's output.
java -jar sclera_docker.jar > /dev/null 2>&1 &
JAVA_APP_PID=$! # Capture PID of the Java app if you need it for debugging/monitoring later

echo "Sclera Docker Java app initiated in background with PID: $JAVA_APP_PID"

# --- Keep the monitor.sh script (and thus the container) alive ---
# The container's lifecycle will now be tied to the rsyslogd process.
# If rsyslogd ever stops, the 'wait' command completes, and the container exits.
echo "Monitor script waiting for rsyslogd (PID: $RSYSLOG_PID) to remain active. Container will run as long as rsyslogd runs."
wait "$RSYSLOG_PID"

echo "Rsyslogd process terminated. Exiting monitor.sh and stopping container."
