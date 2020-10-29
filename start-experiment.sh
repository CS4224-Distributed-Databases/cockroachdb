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
  sshpass -p $1 ssh cs4224j@xcnc20.comp.nus.edu.sg "source .bash_profile; cd cockroachdb && mvn clean dependency:copy-dependencies package;"
  echo "Built project on xcnc20"
  sshpass -p $1 ssh cs4224j@xcnc21.comp.nus.edu.sg "source .bash_profile; cd cockroachdb && mvn clean dependency:copy-dependencies package;"
  echo "Built project on xcnc21"
  sshpass -p $1 ssh cs4224j@xcnc22.comp.nus.edu.sg "source .bash_profile; cd cockroachdb && mvn clean dependency:copy-dependencies package;"
  echo "Built project on xcnc22"
  sshpass -p $1 ssh cs4224j@xcnc23.comp.nus.edu.sg "source .bash_profile; cd cockroachdb && mvn clean dependency:copy-dependencies package;"
  echo "Built project on xcnc23"
  if ($2 == 5) then
    sshpass -p $1 ssh cs4224j@xcnc24.comp.nus.edu.sg "source .bash_profile; cd cockroachdb && mvn clean dependency:copy-dependencies package;"
    echo "Built project on xcnc24"
  fi
}

# Upload csv datafiles to node to use IMPORT INTO fast load
uploadDataToNodes() {
  sshpass -p "$1" ssh cs4224j@xcnc20.comp.nus.edu.sg "mkdir node1/extern/; cp cockroach/main/java/DataSource/* ~/node1/extern;"
  echo "Uploaded data into node3"
}

echo "Starting to load data-files into all nodelocal"
uploadDataToNodes $1

echo "Starting to build on all servers"
# $1: Password, $2: Number of servers, could be 4 or 5
buildProject $1 $2
echo "Starting to run project with $2 instances"
# $1: Password, $2 Number of clients, could be 20 or 40
runProject $1 $2
echo "Complete experiment"