
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
- DataSource Folder: Since the csv files are too big, we will not upload them on github. Ensure that you copy the data csv files to this directory locally. 
- Transactions: Contains a base transaction file + 8 Transaction queries
- The file BasicExample.java is just a basic template code

## Running the project

**Ensure that you have the latest code on the server** 
1. Clone the latest github project into your computer
2. Ensure that you copy the data csv files into `DataSource/data-files` and transaction txt files into `DataSource/xact-files` folder locally. <br>
The files can be downloaded [here](http://www.comp.nus.edu.sg/~cs4224/project-files.zip).
3. Go to the directory of the cloned project 
4. Copy the latest code to the server, inside `/temp` directory: If there is an existing old cockroachdb in the server, run `rm -rf cockroachdb` first to remove it. <br>
 Then run `scp –r cockroachdb cs4224j@xcnc20.comp.nus.edu.sg:~/cockroachdb`
5. Copy the scripts files as mentioned below from cockroachdb project folder to be inside the /temp directory, on the same level as cockroachdb project folder
- start-cockroach-node1.sh
- start-cockroach-node2.sh
- start-cockroach-node3.sh
- start-cockroach-node4.sh
- start-cockroach-node5.sh
- start-experiment.sh
6. Run `ls -l` and check that files permissions are `rwx------`. If it is not, run `chmod -R 700 cockroachdb`. 

**Starting Cockroachdb on the servers**
1. ssh into all xcnc20-24 servers on 5 different terminals
    - ssh cs4224j@xcnc20.comp.nus.edu.sg
    - ssh cs4224j@xcnc21.comp.nus.edu.sg
    - ssh cs4224j@xcnc22.comp.nus.edu.sg
    - ssh cs4224j@xcnc23.comp.nus.edu.sg
    - ssh cs4224j@xcnc24.comp.nus.edu.sg
2. Set up and build the nodes for cockroachdb cluster. Depending on how many nodes you want to add, follow the steps below in order. 
    - From one of the server machine inside `/temp` directory, delete all the folders names node1, node2, ...node 5 if you want to have a clean database. `rm -rf node1` If you have issues deleting it, go to the last section of the README to kill the running process.
    - run `./start-cockroach-node1.sh` on server xcnc20 (Node1) <br>
    - run `./start-cockroach-node2.sh` on server xcnc21 (Node2) <br> 
    - run `./start-cockroach-node3.sh` on server xcnc22 (Node3) <br>   
    - run `./start-cockroach-node4.sh` on server xcnc23 (Node4) <br>  
    - run `./start-cockroach-node5.sh` on server xcnc24 (Node5) <br> 
    - If you have not initialised the cluster before, run `cockroach init --insecure` on either of the servers eg, xcnc20 <br> 
3. To check that the cluster is working, type `cockroach sql --host=192.168.48.169 --insecure` on xcnc20. Make sure to match the host address to the one set for that machine you are typing the command for. The ip can be seen in the script.
4. You will also see new folders titled node1, node2, node3...node5 being created inside the /temp directory.

>Check that file scripts permissions are `rwx------`
>If you experience any problems whereby ports have been binded, you can refer to the last section of the readme to find out how to kill the process.

**Compiling the project on the server**
1. ssh into one server `ssh cs4224j@xcnc20.comp.nus.edu.sg`
2. Inside `/temp` directory, run `cd cockroachdb` to enter the project directory 
3. Build the project by `mvn clean dependency:copy-dependencies package`

// TODO: Remove the section below
**Create Tables and Loading data on the server**
1. `java -Xms45g -Xmx45g -cp target/*:target/dependency/*:. InitialiseData` <br>
>Note that step 1 drops all existing tables and create new ones

**Running an experiment from your local computer**
0. If you are running on a Windows computer, open Ubuntu App CMD (or download from the store) as the commands below are for linux.
1. Ensure you have sshpass installed in your computer. Otherwise run `sudo apt install sshpass`
2. Locally, in the root directory of the project (the one you cloned) in your local computer, run `./start-experiment.sh password numOfServers numOfClients`, 
replacing `password` with the password to the servers, `numOfServers` with 4 or 5,`numOfClients` with 20 or 40.
3. Output written to stdout can be found in `experiment-logs/i.out.log` and output written to stderr can be found in `experiment-logs/i.err.log` where i is the client number.
>Note that step2 will perform the steps to drop all existing tables, create new ones and load data into the cockroach database and then simulating an experiment according to the parameters input. 

**Generating statistics after an experiment**

*Generate the Database state*
1. run `java -Xms45g -Xmx45g -cp target/*:target/dependency/*:. EndStateRunner`
2. Open `output/end_state.csv` file
3. Manually copy the results into a row of the main `db-state.csv` which records all db end state for all experiments. 
Set the first column to be this experiment number. 

*Generate Performance and Throughput Statistics*
1. run `java -Xms45g -Xmx45g -cp target/*:target/dependency/*:. TotalStatsRunner`
2. For Throughput Statistics: <br>
2.1 Open `output/throughput_stats.csv` file <br>
2.2 Manually copy the results into a row of the main `throughput.csv` which records all min, avg and max throughputs for all experiments. 
Set the first column to be this experiment number. <br>
3. For Performance Statistics: <br>
3.1 Open `output/client_stats.csv` file <br>
3.2 Manually copy the results into a row of the main `clients.csv` which records all clients statistics for all experiments.  <br>


*Shut down node*
- Open a new terminal from the node you want to shut down and type: `cockroach quit --insecure`


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
 
