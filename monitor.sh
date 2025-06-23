#!/bin/sh


# Ensure the directory for the generated rsyslog configuration exists
mkdir -p /etc/rsyslog.d/

# Generate rsyslog forwarding configuration using environment variables.
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
echo "Starting avahi-daemon..."
/etc/init.d/avahi-daemon start &


export LD_LIBRARY_PATH=/usr/local/lib:$LD_LIBRARY_PATH
echo "LD_LIBRARY_PATH set to: $LD_LIBRARY_PATH"

# Start rsyslogd in the background using its full path for robustness.
echo "Starting rsyslogd..."
/usr/local/sbin/rsyslogd -n -f /etc/rsyslog.conf &
RSYSLOG_PID=$!

echo "Rsyslogd started with PID: $RSYSLOG_PID"

# --- Start your main Java application in the BACKGROUND ---
echo "Starting sclera_docker.jar in background..."
# Redirect Java app output to /dev/null to prevent excessive logging filling Docker logs
# if the app has issues, and to ensure it doesn't accidentally interfere with monitor.sh's output.
java -jar sclera_docker.jar > /dev/null 2>&1 &
JAVA_APP_PID=$! # Capture PID of the Java app if you need it for debugging/monitoring later

echo "Sclera Docker Java app initiated in background with PID: $JAVA_APP_PID"


echo "Monitor script waiting for rsyslogd (PID: $RSYSLOG_PID) to remain active. Container will run as long as rsyslogd runs."
wait "$RSYSLOG_PID"

echo "Rsyslogd process terminated. Exiting monitor.sh and stopping container."
