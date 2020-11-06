#!/bin/bash

# Output of Experiment logs will be written directly to the project Directory experiment-logs
runExperiments() {

  # Store IP addresses in a list
  allIPAddr=($4 $5 $6 $7 $8)

  # Remove experiment-logs directory and create new one
  echo "Create new experiment-logs directory in project structure"
  sshpass -p $1 ssh cs4224j@xcnc20.comp.nus.edu.sg "source .bash_profile; rm -rf temp/cockroachdb/src/main/java/experiment-logs; mkdir temp/cockroachdb/src/main/java/experiment-logs"

  # Compilation of code -> Uncomment for debug if we are only running this function
  sshpass -p $1 ssh cs4224j@xcnc20.comp.nus.edu.sg "source .bash_profile; cd temp/cockroachdb && mvn clean dependency:copy-dependencies package;"

  # $2 is number of clients
  #iterate through each client and assign to the correct server to run
  for ((i=0; i<$2; i++)); do
    logIndex=$(($i + 1)) # Indexed 1
    # $3 contains the number of servers to run on
    nodeid=$(($i % $3))
    serverIP=${allIPAddr[nodeid]} # Used to pass inside Main Function so we know which node's IP address to connect to
    serverID=$((20 + nodeid))
  	server="xcnc${serverID}"
  	echo "Assign client $(($i + 1)) on $server with IP Address being $serverIP" # client Indexed 1

  	input_file="src/main/java/DataSource/xact-files/${logIndex}.txt"
  	stdout_file="src/main/java/experiment-logs/${logIndex}.out.log"
  	stderr_file="src/main/java/experiment-logs/${logIndex}.err.log"

  	# Make empty txt, out, err files so that the next ssh line won't have error when we feed into Main Function
  	echo "Create empty log files"
  	sshpass -p $1 ssh cs4224j@${server}.comp.nus.edu.sg "source .bash_profile; touch temp/cockroachdb/src/main/java/experiment-logs/${logIndex}.txt; touch temp/cockroachdb/src/main/java/experiment-logs/${logIndex}.out.log; touch temp/cockroachdb/src/main/java/experiment-logs/${logIndex}.err.log"

  	echo "Start running transactions via Main function"
    #sshpass -p $1 ssh cs4224j@${server}.comp.nus.edu.sg "source .bash_profile; cd temp/cockroachdb && java -Xms4g -Xmx4g -cp target/*:target/dependency/*:. Main $serverIP < ${input_file} > ${stdout_file} 2> ${stderr_file} &"> /dev/null 2>&1 &
  	echo "Finish running ${i+1} transaction file on $server"
  done
}

loadDataToDatabaseFromExternFolder() {
  sshpass -p $1 ssh cs4224j@xcnc20.comp.nus.edu.sg "source .bash_profile; cd temp/cockroachdb && mvn clean dependency:copy-dependencies package; java -Xms4g -Xmx4g -cp target/*:target/dependency/*:. InitialiseData $2"
  echo "Built project on xcnc20"
}

# Upload csv datafiles to node to use IMPORT INTO fast load
createExternFolderToStoreCSV() {
  echo "Create new /extern directories for each node"
  echo $1
  sshpass -p $1 ssh cs4224j@xcnc20.comp.nus.edu.sg "source .bash_profile; rm -rf temp/node1/extern/; mkdir temp/node1/extern/; cp -r temp/cockroachdb/src/main/java/DataSource/* ~/temp/node1/extern/;"
  echo "node 1"
  sshpass -p $1 ssh cs4224j@xcnc21.comp.nus.edu.sg "source .bash_profile; rm -rf temp/node2/extern/; mkdir temp/node2/extern/; cp -r temp/cockroachdb/src/main/java/DataSource/* ~/temp/node2/extern/;"
  echo "node 2"
  sshpass -p $1 ssh cs4224j@xcnc22.comp.nus.edu.sg "source .bash_profile; rm -rf temp/node3/extern/; mkdir temp/node3/extern/; cp -r temp/cockroachdb/src/main/java/DataSource/* ~/temp/node3/extern/;"
  echo "node 3"
  sshpass -p $1 ssh cs4224j@xcnc23.comp.nus.edu.sg "source .bash_profile; rm -rf temp/node4/extern/; mkdir temp/node4/extern/; cp -r temp/cockroachdb/src/main/java/DataSource/* ~/temp/node4/extern/;"
  echo "node 4"

  if [ $2 == 5 ]
  then
  sshpass -p $1 ssh cs4224j@xcnc24.comp.nus.edu.sg "source .bash_profile; rm -rf temp/node5/extern/; mkdir temp/node5/extern/; cp -r temp/cockroachdb/src/main/java/DataSource/* ~/temp/node5/extern/;"
  echo "node 5"
  fi
  echo "Created /extern directory for all nodes and transfer CSV files inside each directory"
}



# ============================START=============================
## $1: Password, $2 Number of clients: 20/40, $3 number of servers: 4/5, $4 $5 $6 $7 $8 servers IP Addresses
echo "Starting to load data-files into all nodes' /extern directory"
createExternFolderToStoreCSV $1 $3

# We load data into the first server address given
echo "Load data-files into cockroachdb database.....call InitialiseData File in the Project"
loadDataToDatabaseFromExternFolder $1 $4

echo "Start Experiment. Please key in parameters with the first being the number of clients followed by number of servers and then IP addresses of the servers to use"
echo "Example Usage: password 20 5 ipOne ipTwo ipThree ipFour ipFive"
echo "starting to run with $2 number of clients"
echo "Starting to run with $3 number of servers"
echo "Server IP Addresses are $4 $5 $6 $7 $8"
runExperiments $1 $2 $3 $4 $5 $6 $7 $8
echo "Complete experiment"
