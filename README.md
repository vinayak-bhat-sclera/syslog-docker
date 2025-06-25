final docker run command:
docker run -dit --name sclera-syslog-forwarder --env-file ./rsyslog.env -v "$(pwd)/rsyslog.env:/etc/rsyslog.env" --network host sclera_image

cat -v rsyslog.env
docker exec -it fd4 /bin/bash 

nano /tmp/sclera/generate_rsyslog_forwarding_conf_clean.sh
sed -i 's/\r$//' /home/sclera/sclera_dir/scleraFile/dockerfile/rsyslog.env
chmod +x /tmp/sclera/generate_rsyslog_forwarding_conf_clean.sh
bash /tmp/sclera/generate_rsyslog_forwarding_conf_clean.sh

to add more servers
rsyslog.env
	FORWARDING_SERVERS="10.2.31.30:514 10.2.31.40:514 10.2.31.50:514"

docker restart sclera-syslog-forwarder
