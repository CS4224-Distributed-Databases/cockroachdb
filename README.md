
## CS4224 Distributed Databases
### CockroachDB

*AY2020/2021 Semester 1*, *School of Computing*, *National University of Singapore*

## Team members
- [Ivan Ho](https://github.com/ihwk1996)
- [Kerryn Eer](https://github.com/KerrynEer)
- [Ooi Hui Ying](https://github.com/ooihuiying)
- [Wayne Seah](https://github.com/wayneswq)

## Project Summary
This learning tasks for this project are 
- Install a distributed database system on a cluster of machines
- Design a data model and implement transactions to support an application
- Benchmark the performance of an application

## Project Structure
The bulk of our code are in the folder src -> main -> java
- DataLoader Folder: Contains the code to load the data into the Cassandra database and create table schemas
- DataSource Folder: Since the csv files are too big, we will not upload them on github. Ensure that you copy the data csv files into `DataSource/data-files` and transaction txt files into `DataSource/xact-files` folder locally. <br>The files can be downloaded [here](http://www.comp.nus.edu.sg/~cs4224/project-files.zip).
- Transactions: Contains a base transaction file + 8 Transaction queries
- The file BasicExample.java is just a basic template code for reference. It is provided in Cockroachdb documentation and not in use.
- The file EndStateRunner and TotalStatsRunner helps to collate the experimental results
- The file InitialiseData helps to create tables/views on cockroachdb nodes cluster on the server and load data.
- The file Main helps to receive the transaction inputs and coordinates the running of the transactions on the server it is being run on.

We also have two script files to assist in setting up cockroachdb and run the experiments on the servers
- start-cockroach-all.sh: Helps to start cockroachdb on the server
- start-experiment.sh: Helps to run the experiments based on the transaction files being fed.

## Running the project
**Ensure that you have the latest code on the server (one of them will do since they have access to the same file space) and local computer** 
1. Clone the latest github project into your computer
2. Ensure that you copy the data csv files into `DataSource/data-files` and transaction txt files into `DataSource/xact-files` folder locally. <br>
The files can be downloaded [here](http://www.comp.nus.edu.sg/~cs4224/project-files.zip).
3. Go to the directory of the cloned project in your local computer
4. Copy the latest code to place it in the server, inside `/temp` directory: If there is an existing old cockroachdb in the server, run `rm -rf cockroachdb` first to remove it. <br>
 Then run `scp –r cockroachdb cs4224j@xcnc20.comp.nus.edu.sg:~/cockroachdb`

**Ensure that your server contains the following installations**
1. apache-maven-3.0.5
2. jdk1.8.0_261
3. cockroach-v19.2.9.linux-amd64

**Starting Cockroachdb on the servers**
1. The following script `start-cockroach-all.sh` will allow you to ssh into all xcnc20-24 servers. Should you want to change the servernames, make changes to the scripts accordingly. 
    - ssh cs4224j@xcnc20.comp.nus.edu.sg
    - ssh cs4224j@xcnc21.comp.nus.edu.sg
    - ssh cs4224j@xcnc22.comp.nus.edu.sg
    - ssh cs4224j@xcnc23.comp.nus.edu.sg
    - ssh cs4224j@xcnc24.comp.nus.edu.sg
2. Set up and build the nodes for cockroachdb cluster. Depending on how many nodes you want to add, follow the steps below in order. 
    - On your own, SSH into one of the server machine inside `/temp` directory, delete all the folders names node1, node2, ...node 5 if you want to have a clean database. `rm -rf node1` If you have issues deleting it, go to the last section of the README to kill the running process.
    - If you are running on a Windows computer, open Ubuntu App CMD (or download from the store) as the commands below are for linux.
    - Ensure you are at the correct directory of the cloned project in your local computer. 
    - Run `./start-cockroach-all.sh password numOfServers serverIPOne serverIPTwo serverIPThree serverIPFour serverIPFive` to start cockroachdb in all the required nodes. <br>
    - Note that you do not need to key in the last parameter serverIPFive if you do not intend to run on 5 server nodes. 
    - serverIPOne, serverIPTwo...etc simply denotes the IPAddresses of the servers you intend to run cockroachdb on. Note that we assume that you want to use the default ports for cockroachdb. Hence it is important to make sure that the ports are free on the servers you want to use. Otherwise, either kill the process using the ports in the servers or modify the script.   
    - Once the script has finished executing, if you have not initialised the cluster before, SSH into one of the server machines and run `cockroach init --insecure --host=IPADDRESS:PORT`. Note you can omit the last param --host=IPADDRESS:PORT if you are using the default ports for Cockroachdb.<br> 
3. To check that the cluster is working, SSH into one of the servers and type `cockroach node status --host=IPADDRESS:PORT --insecure`. Note you can omit the last param --host=IPADDRESS:PORT if you are using the default ports for Cockroachdb.
4. You will also see new folders titled node1, node2, node3, node4 and node5 (optional) being created inside the /temp directory.

>Check that file scripts permissions are `rwx------`
>If you experience any problems whereby ports are already in use, you can refer to the last section of the readme to find out how to kill the process.

**Compiling the project on the server**
1. ssh into one server `ssh cs4224j@xcnc20.comp.nus.edu.sg`
2. Inside `/temp` directory, run `cd cockroachdb` to enter the project directory 
3. Build the project by `mvn clean dependency:copy-dependencies package`
4. If you see that build has failed, then you will need to navigate to the Java project `cockroachdb/target/dependency` to remove all the running processes inside. Follow the Maven error message you see. 

**Running an experiment from your local computer**
0. Ensure that you have the latest script files on your local computer for this step. If you are running on a Windows computer, open Ubuntu App CMD (or download from the store) as the commands below are for linux.
1. Ensure you have sshpass installed in your computer. Otherwise run `sudo apt install sshpass`
2. Locally, in the root directory of the project (the one you cloned) in your local computer, run `./start-experiment.sh password numOfClients numOfServers serverOneIPAddr serverTwoIPAddr serverThreeIPAddr serverFourIPAddr serverFiveIPAddr`, 
replacing `password` with the password to the servers, `numOfClients` with 20 or 40, `numOfServers` with 4 or 5 and the remaining to be the IP addresses of the servers to use.
3. Similarly, if you do not want to SSH into the server names inside the scripts, please make the changes accordingly.
4. Output written to stdout can be found in `experiment-logs/i.out.log` and output written to stderr can be found in `experiment-logs/i.err.log` where i is the client number.
>Note that step2 will perform the steps to drop all existing tables, create new ones and load data into the cockroach database and then simulating an experiment according to the parameters input. 

**Generating statistics after an experiment**

*Generate the Database state*
1. run `java -Xms4g -Xmx4g -cp target/*:target/dependency/*:. EndStateRunner serverOneIPAddr directoryName`
, replacing serverIPAddress with the correct address of the server (can be the first server) and replacing directoryName with directory containing log files (remember to include / at the end)
2. Open `end_state.csv` file in the directory containing the logs
3. Manually copy the results into a row of the main `db-state.csv` which records all db end state for all experiments. 
Set the first column to be this experiment number. 

*Generate Performance and Throughput Statistics*
1. run `java -Xms4g -Xmx4g -cp target/*:target/dependency/*:. TotalStatsRunner numClients directoryName`
, replacing numClients with number of clients set and directoryName with directory containing log files (remember to include / at the end) for eg `src/main/java/experiment-logs/`
2. For Throughput Statistics: <br>
2.1 Open `throughput_stats.csv` file in the directory containing the logs <br>
2.2 Manually copy the results into a row of the main `throughput.csv` which records all min, avg and max throughputs for all experiments. 
Set the first column to be this experiment number. <br>
3. For Performance Statistics: <br>
3.1 Open `client_stats.csv` file in the directory containing the logs <br>
3.2 Manually copy the results into a row of the main `clients.csv` which records all clients statistics for all experiments. 
Set the first column to be this experiment number. <br>

*Shut down node*
- Open a new terminal from the node you want to shut down and type: `cockroach quit --host=IPADDRESS:PORT --insecure`. Note you can omit the last param --host=IPADDRESS:PORT if you are using the default ports for Cockroachdb.

*Check nodes status*
- SSH into one of the servers and type `cockroach node status --host=IPADDRESS:PORT --insecure`. Note you can omit the last param --host=IPADDRESS:PORT if you are using the default ports for Cockroachdb.

## Set up instructions for local development
(Start CockroachDB)
- Follow the instructions to download cockroachdb v19.2.9: https://www.cockroachlabs.com/docs/releases/v19.2.9.html 
- and zoneinfo zip into local machine and add ZoneInfo to path: https://www.cockroachlabs.com/docs/v20.1/install-cockroachdb-windows.html 
- Add cockroach to path: https://www.youtube.com/watch?v=6x9b0t-j1mM
- Follow instructions here to start local cluster: https://www.cockroachlabs.com/docs/v20.1/secure-a-cluster.html#step-1-generate-certificates.

(In IDLE)
- Clone this project into an IDLE of your choice
- Configure Maven to handle the dependencies required
- Add postgres as database. Read the article below and start from the section “Set CockroachDB as a Data Source in Intellij”.
- https://www.cockroachlabs.com/docs/stable/intellij-idea.html

## Notes about cockroachDB
- It is somewhat similar to PostgresDB.
- CockroachDB stores all user data (tables, indexes, etc.) and almost all system data in a giant sorted map of key-value pairs. 
- This keyspace is divided into “ranges”, contiguous chunks of the keyspace, so that every key can always be found in a single range.
- From a SQL perspective, a table and its secondary indexes initially map to a single range, where each key-value pair in the range represents a single row in the table (also called the primary index because the table is sorted by the primary key) or a single row in a secondary index. As soon as that range reaches 512 MiB in size, it splits into two ranges. This process continues for these new ranges as the table and its indexes continue growing.
- CockroachDB uses a cost-based optimiser to process a query, consider various plans and pick the one with the lowest cost or the best performance.
- Default internal implementation of a join is Hash join, but can hint to use other join types (merge, lookup)
- https://www.cockroachlabs.com/docs/stable/architecture/reads-and-writes-overview.html#network-and-i-o-bottlenecks

- More on Cockroachdb commands: https://www.bookstack.cn/read/CockroachDB/952e033fddd3295f.md

## Notes about using soc cluster server

##### To kill running processes
- To view the processes, type the command `ps -ef | grep username`. 
- If you want to kill the cockroach node process, search for the row with the word cockroachdb.
- The second column contains the pid.
- To kill it, type `kill -9 pid`

##### Use Screen tool to recover server state in the event of lost connection
 
##### Note that the script is not runnable if it is written in for eg, window environment and uploaded onto the server. If you make any edits to the script, be sure to open the file on the server with the following commands to make it a unix file
- `vim fileName`
- `:set ff=unix` 

##### To view output of script for eg, output.txt
- `./script-to-run > output.txt`

 
