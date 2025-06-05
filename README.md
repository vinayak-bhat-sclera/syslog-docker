#command to add a forwarding IP

docker run -d \
  --name my-sclera-instance \
  -e FORWARDING_IP="10.2.32.30" \
  -e NETWORK_ADDRESS="10.2.32.99" \
  -e AGENT_ID="VDMS385" \
  -e DOCKER_ID="your_specific_docker_instance_id_here" \
  sclera_image
