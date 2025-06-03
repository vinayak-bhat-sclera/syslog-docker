function cleanup {
process_id=""
process_id=$(pgrep -f "docker_app.jar")
#echo $process_id
if ![[ -z "$process_id" ]]
then
kill -9 $process_id
fi
}

trap cleanup EXIT

while :
do
process_id=""
process_id=$(pgrep -f "docker_app.jar")
#echo $process_id
if [[ -z "$process_id" ]]
then
	bridge_ip=$(getent hosts scleravdmsnetworkgateway | awk {'print $1'})
	echo $bridge_ip
	java -jar docker_app.jar ws://$bridge_ip:8888/ws &
fi

# find ./logs/ -maxdepth 1 -mtime +6 -delete

sleep 5
done