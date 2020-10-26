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
4. Copy the latest code to the server: If there is an existing old cockroachdb in the server, run `rm -rf cockroachdb` first to remove it. <br>
 Then run `scp –r cockroachdb cs4224j@xcnc20.comp.nus.edu.sg:~/cockroachdb`
5. Run `ls -l` and check that file permissions are `rwx------`. If it is not, run `chmod -R 700 cockroachdb`. 

**Compiling the project on the server**
1. ssh into one server `ssh cs4224j@xcnc20.comp.nus.edu.sg`
2. Run `cd cockroachdb` to enter the project directory 
3. Build the project by `mvn clean dependency:copy-dependencies package`

**Create Tables and Loading data on the server**
1. `java -Xms45g -Xmx45g -cp target/*:target/dependency/*:. InitialiseData` <br>
>Note that step 1 drops all existing tables and create new ones

**Starting Cockroachdb**
1. ssh into all xcnc20-24 servers on 5 different terminals
    - ssh cs4224j@xcnc20.comp.nus.edu.sg
    - ssh cs4224j@xcnc21.comp.nus.edu.sg
    - ssh cs4224j@xcnc22.comp.nus.edu.sg
    - ssh cs4224j@xcnc23.comp.nus.edu.sg
    - ssh cs4224j@xcnc24.comp.nus.edu.sg
2. Set up and build the nodes for cockroachdb cluster. Depending on how many nodes you want to add, follow the steps below
    - run `./start-cockroach-node1.sh` for xcnc20 <br> (Node1)
    - run `./start-cockroach-node2.sh` for xcnc21 <br> (Node2)
    - run `./start-cockroach-node3.sh` for xcnc22 <br> (Node3)
    - run `./start-cockroach-node4.sh` for xcnc23 <br> (Node4)
    - run `./start-cockroach-node5.sh` for xcnc24 <br> (Node5)
    - run `./init-cockroach-cluster.sh` for xcnc20 <br> (Always run this after you have set up all the nodes you want)


**Running an experiment**
1. Ensure you have sshpass installed in your computer. Otherwise run `sudo apt install sshpass`
2. Locally, in the root directory of the project, run `./start-experiment.sh password numOfClients`, 
replacing `password` with the password to the servers, `numOfClients` with 20 or 40.
3. Output written to stdout can be found in `log/i.out.log` and output written to stderr can be found in `log/i.err.log` where i is the client number.

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
3.1 TODO <br>
3.2 TODO <br>

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
- https://www.cockroachlabs.com/docs/stable/architecture/reads-and-writes-overview.html#network-and-i-o-bottlenecks