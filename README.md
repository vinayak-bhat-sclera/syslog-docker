# command to add a forwarding IP

docker run -d \
  --name my-sclera-instance \
  -e FORWARDING_IP="10.2.32.30" \
  -e NETWORK_ADDRESS="10.2.32.99" \
  -e AGENT_ID="VDMS385" \
  -e DOCKER_ID="your_specific_docker_instance_id_here" \
  sclera_image



# inorder to change the IP address
# 1. Stop the currently running container
docker stop my-sclera-instance

# 2. Remove the stopped container
docker rm my-sclera-instance

# 3. Run a new container with the updated IP address
docker run -d \
  --name my-sclera-instance \
  -e FORWARDING_IP="10.2.32.50" \
  -e NETWORK_ADDRESS="10.2.32.99" \
  -e AGENT_ID="VDMS385" \
  -e DOCKER_ID="your_specific_docker_instance_id_here" \
  sclera_image
