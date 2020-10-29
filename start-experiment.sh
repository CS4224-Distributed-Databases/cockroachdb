#!/bin/bash

runExperiments() {
  # Remove old log files on each server.
  for ((i=0; i<5; i++)); do
    server="xcnc$((20 + $i % 5))"
    sshpass -p $1 ssh cs4224j@$server.comp.nus.edu.sg "source .bash_profile; cd temp/cockroachdb && rm -rf log && mkdir log"
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
  	sshpass -p $1 ssh cs4224j@$server.comp.nus.edu.sg "source .bash_profile; cd temp/cockroachdb && java -Xms45g -Xmx45g -cp target/*:target/dependency/*:. InitialiseData"
  	echo "Start running transactions via Main function"
  	sshpass -p $1 ssh cs4224j@$server.comp.nus.edu.sg "source .bash_profile; cd temp/cockroachdb && java -Xms45g -Xmx45g -cp target/*:target/dependency/*:. Main ${input_file} > ${stdout_file} 2> ${stderr_file} &" > /dev/null 2>&1 &
  	
  	echo "Finish running $i transaction file on $server"
  done
}

# Upload csv datafiles to node to use IMPORT INTO fast load
# Not sure why but sshpass command does not work and no error message shows up
# Manually key in password as prompted by the server
createExternFolderToStoreCSV() {
  echo "Create /extern directories for each node"
  echo $1
  # sshpass -p "$1" ssh cs4224j@xcnc20.comp.nus.edu.sg "source .bash_profile; mkdir temp/node1/extern/; cp -r temp/cockroachdb/src/main/java/DataSource/* ~/temp/node1/extern/;"
  ssh cs4224j@xcnc20.comp.nus.edu.sg "source .bash_profile; mkdir temp/node1/extern/; cp -r temp/cockroachdb/src/main/java/DataSource/* ~/temp/node1/extern/;"
  echo "node 1"
  ssh cs4224j@xcnc21.comp.nus.edu.sg "source .bash_profile; mkdir temp/node2/extern/; cp -r temp/cockroachdb/src/main/java/DataSource/* ~/temp/node2/extern/;"
  echo "node 2"
  ssh cs4224j@xcnc22.comp.nus.edu.sg "source .bash_profile; mkdir temp/node3/extern/; cp -r temp/cockroachdb/src/main/java/DataSource/* ~/temp/node3/extern/;"
  echo "node 3"
  ssh cs4224j@xcnc23.comp.nus.edu.sg "source .bash_profile; mkdir temp/node4/extern/; cp -r temp/cockroachdb/src/main/java/DataSource/* ~/temp/node4/extern/;"
  echo "node 4"
  ssh cs4224j@xcnc24.comp.nus.edu.sg "source .bash_profile; mkdir temp/node5/extern/; cp -r temp/cockroachdb/src/main/java/DataSource/* ~/temp/node5/extern/;"
  echo "node 5"
  echo "Created /extern directory for all nodes and transfer CSV files inside each directory"
}

loadDataToDatabaseFromExternFolder() {
  ssh cs4224j@xcnc20.comp.nus.edu.sg "source .bash_profile; cd temp/cockroachdb && mvn clean dependency:copy-dependencies package; java -Xms45g -Xmx45g -cp target/*:target/dependency/*:. InitialiseData"
  echo "Built project on xcnc20"
}


# ============================START=============================
## $1: Password, $2 Number of clients: 20/40, $3 number of servers: 4/5
echo "Starting to load data-files into all nodes' /extern directory"
# Currently $1 is not in used because sshpass is not working
createExternFolderToStoreCSV $1

echo "Load data-files into cockroachdb database.....call InitialiseData File Code in the Project"
loadDataToDatabaseFromExternFolder

echo "Start Experiment. Please key in parameters with the first being the number of clients followed by number of servers"
echo "starting to run with $2 number of clients"
echo "Starting to run with $3 number of servers"
runExperiments $2 $3
echo "Complete experiment"
