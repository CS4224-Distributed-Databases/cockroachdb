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

## Set up instructions for local development
(Start CockroachDB)
- Follow the instructions to download cockroachdb and zoneinfo zip into local machine and add ZoneInfo to path: https://www.cockroachlabs.com/docs/v20.1/install-cockroachdb-windows.html
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