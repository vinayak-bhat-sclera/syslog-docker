1. unzip dockerfile.zip
2. cd dockerfile
3. docker buildx build -t sclera_image .
4. docker images   [see if sclera_image is created]
5. curl -X "POST" http://localhost:8888/api/settings/createNetworks
6. docker ps   [ see if host, guest and hvac containers are running ]
7. if in case you want to build the docker image again, delete the network first
8. curl -X "DELETE" http://localhost:8888/api/settings/deleteNetworks

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
