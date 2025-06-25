#!/bin/bash
set -e

ENV_FILE="/etc/rsyslog.env"
CONFIG_FILE="/etc/rsyslog.d/forwarding.conf"

echo "# Auto-generated rsyslog forwarding configuration" > "$CONFIG_FILE"
echo "" >> "$CONFIG_FILE"

if [ -f "$ENV_FILE" ]; then
    source "$ENV_FILE"
else
    echo "Environment file not found: $ENV_FILE"
    exit 1
fi

if [[ -z "$FORWARDING_SERVERS" ]]; then
    echo "# No FORWARDING_SERVERS provided, skipping forwarding setup." >> "$CONFIG_FILE"
else
    # Handle both space or comma separated IPs
    FORWARDING_SERVERS=$(echo "$FORWARDING_SERVERS" | tr ',' ' ')
    for ip in $FORWARDING_SERVERS; do
        echo "# Forward to $ip" >> "$CONFIG_FILE"
        echo "*.* action(type=\"omfwd\" target=\"${ip%%:*}\" port=\"${ip##*:}\" protocol=\"udp\")" >> "$CONFIG_FILE"
        echo "" >> "$CONFIG_FILE"
    done
fi
