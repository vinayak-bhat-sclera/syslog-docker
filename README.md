final docker run command:
docker run -dit --name sclera-syslog-forwarder --env-file ./rsyslog.env -v "$(pwd)/rsyslog.env:/etc/rsyslog.env" --network host sclera_image
1) cat -v rsyslog.env
2) docker exec -it fd4 /bin/bash
3) nano /tmp/sclera/generate_rsyslog_forwarding_conf_clean.sh
4) sed -i 's/\r$//' /home/sclera/sclera_dir/scleraFile/dockerfile/rsyslog.env
5) chmod +x /tmp/sclera/generate_rsyslog_forwarding_conf_clean.sh
6) bash /tmp/sclera/generate_rsyslog_forwarding_conf_clean.sh
7) to add more servers
8) rsyslog.env
9) FORWARDING_SERVERS="10.2.31.30:514 10.2.31.40:514 10.2.31.50:514"
10) docker restart sclera-syslog-forwarder
