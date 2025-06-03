#!/bin/bash
# Check the value of the host_network environment variable
if [[ "$HOST_NETWORK" == "0" || "$HOST_NETWORK" == "false" ]]; then
    # Execute your commands here
    echo "Executing commands inside Docker..."
iptables -A INPUT -p icmp --icmp-type 8 -j DROP
iptables -A INPUT -i lo -p tcp -j ACCEPT
iptables -A INPUT -i lo -p udp -j ACCEPT
iptables -A INPUT -i eth1 -p tcp -j ACCEPT
iptables -A INPUT -i eth1 -p udp -j ACCEPT
iptables -A INPUT -p udp -m udp --dport 162 -j ACCEPT
iptables -A INPUT -p udp -m udp --dport 47808 -j ACCEPT
iptables -A INPUT -p udp -m udp --dport 1700 -j ACCEPT
iptables -A INPUT -p tcp -m state --state ESTABLISHED,RELATED -j ACCEPT
iptables -A INPUT -p tcp -j DROP
iptables -A INPUT -p udp -m state --state ESTABLISHED,RELATED -j ACCEPT
iptables -A INPUT -p udp -j DROP 

    # Add your commands here
fi
