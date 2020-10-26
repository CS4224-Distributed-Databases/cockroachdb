#!/bin/bash

runProject() {
  # Remove old log files on each server.
  for ((i=0; i<5; i++)); do
    server="xcnc$((20 + $i % 5))"
    sshpass -p $1 ssh cs4224j@$server.comp.nus.edu.sg "source .bash_profile; cd cockroachdb && rm -rf log && mkdir log"
    echo "Remove old logs"
  done

  # $2 is number of clients
  #iterate through each client and assign to the correct server to run
  for ((i=1; i<=$2; i++)); do
  	server="xcnc$((20 + $i % 5))"
  	echo "Assign client $i on $server"
  	
  	input_file="src/main/java/DataSource/xact-files/${i}.txt"
  	stdout_file="log/${i}.out.log"
  	stderr_file="log/${i}.err.log"

  	echo "Create Data and Load Data into Database via InitialiseData function"
  	sshpass -p $1 ssh cs4224j@$server.comp.nus.edu.sg "source .bash_profile; cd cockroachdb && java -Xms45g -Xmx45g -cp target/*:target/dependency/*:. InitialiseData"
  	echo "Start running transactions via Main function"
  	sshpass -p $1 ssh cs4224j@$server.comp.nus.edu.sg "source .bash_profile; cd cockroachdb && java -Xms45g -Xmx45g -cp target/*:target/dependency/*:. Main ${input_file} > ${stdout_file} 2> ${stderr_file} &" > /dev/null 2>&1 &
  	
  	echo "Finish running $i transaction file on $server"
  done
}

# Build project on each server and start a cockroachdb node on each of the cluster
buildProject() {
  sshpass -p $1 ssh cs4224j@xcnc20.comp.nus.edu.sg "source .bash_profile; cd cockroachdb && mvn clean dependency:copy-dependencies package; cd ../ && cockroach start --insecure --host=192.168.48.169"
  echo "Built project on xcnc20"
  sshpass -p $1 ssh cs4224j@xcnc21.comp.nus.edu.sg "source .bash_profile; cd cockroachdb && mvn clean dependency:copy-dependencies package; cd ../ && cockroach start --insecure --store=node2 --host=192.168.48.170 --port=26258 --http-port=8081 --join=192.168.48.169:26257"
  echo "Built project on xcnc21"
  sshpass -p $1 ssh cs4224j@xcnc22.comp.nus.edu.sg "source .bash_profile; cd cockroachdb && mvn clean dependency:copy-dependencies package; cd ../ && cockroach start --insecure --store=node3 --host=192.168.48.171 --port=26259 --http-port=8082 --join=192.168.48.169:26257"
  echo "Built project on xcnc22"
  sshpass -p $1 ssh cs4224j@xcnc23.comp.nus.edu.sg "source .bash_profile; cd cockroachdb && mvn clean dependency:copy-dependencies package; cd ../ && cockroach start --insecure --store=node4 --host=192.168.48.172 --port=26260 --http-port=8083 --join=192.168.48.169:26257"
  echo "Built project on xcnc23"
  if ($2 == 5) then
    sshpass -p $1 ssh cs4224j@xcnc24.comp.nus.edu.sg "source .bash_profile; cd cockroachdb && mvn clean dependency:copy-dependencies package; cd ../ && cockroach start --insecure --store=node5 --host=192.168.48.173 --port=26261 --http-port=8084 --join=192.168.48.169:26257"
    echo "Built project on xcnc24"
  fi
  sshpass -p $1 ssh cs4224j@xcnc20.comp.nus.edu.sg "cockroach init --insecure && grep 'node starting' node1/logs/cockroach.log -A 11; "
  echo "Initialise cluster on xcnc20"
}

echo "Starting to build on all servers"
# $1: Password, $2: Number of servers, could be 4 or 5
buildProject $1 $2
echo "Starting to run project with $2 instances"
# $1: Password, $2 Number of clients, could be 20 or 40
runProject $1 $2
echo "Complete experiment"