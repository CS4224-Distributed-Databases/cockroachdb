#!/bin/bash

# Only relevant if you are not using the default two ports used by cockroachdb
# Example, need to include port numbers behind IP addresses if not using default two ports: 192.168.48.169:50000,192.168.48.170:50001,192.168.48.171:50002,192.168.48.172:50003,192.168.48.173:50004
# Note that the port number used for -http-addr is different from the rest. Please read the cockroachdb documentation for more to know how to change the script below.

startCockroach() {
  if [ $2 == 5 ]
  then
    echo "Starting cockroach on all 5 nodes"
    sshpass -p $1 ssh cs4224j@xcnc20.comp.nus.edu.sg "source .bash_profile; cd temp/; cockroach start --store=node1 --join=$3,$4,$5,$6,$7 --listen-addr=$3 --advertise-addr=$3 --http-addr=$3 --cache=.25 --max-sql-memory=.25 --insecure --background" &
    echo "node 1"
    sshpass -p $1 ssh cs4224j@xcnc21.comp.nus.edu.sg "source .bash_profile; cd temp/; cockroach start --store=node2 --join=$3,$4,$5,$6,$7 --listen-addr=$4 --advertise-addr=$4 --http-addr=$4 --cache=.25 --max-sql-memory=.25 --insecure --background" &
    echo "node 2"
    sshpass -p $1 ssh cs4224j@xcnc22.comp.nus.edu.sg "source .bash_profile; cd temp/; cockroach start --store=node3 --join=$3,$4,$5,$6,$7 --listen-addr=$5 --advertise-addr=$5 --http-addr=$5 --cache=.25 --max-sql-memory=.25 --insecure --background" &
    echo "node 3"
    sshpass -p $1 ssh cs4224j@xcnc23.comp.nus.edu.sg "source .bash_profile; cd temp/; cockroach start --store=node4 --join=$3,$4,$5,$6,$7 --listen-addr=$6 --advertise-addr=$6 --http-addr=$6 --cache=.25 --max-sql-memory=.25 --insecure --background" &
    echo "node 4"
    sshpass -p $1 ssh cs4224j@xcnc24.comp.nus.edu.sg "source .bash_profile; cd temp/; cockroach start --store=node5 --join=$3,$4,$5,$6,$7 --listen-addr=$7 --advertise-addr=$7 --http-addr=$7 --cache=.25 --max-sql-memory=.25 --insecure --background" &
    echo "node 5"
    echo "Finish set up cockroachdb on all 5 nodes"
  else
    echo "Starting cockroach on all 4 nodes"
    sshpass -p $1 ssh cs4224j@xcnc20.comp.nus.edu.sg "source .bash_profile; cd temp/; cockroach start --store=node1 --join=$3,$4,$5,$6 --listen-addr=$3 --advertise-addr=$3 --http-addr=$3 --cache=.25 --max-sql-memory=.25 --insecure --background;" &
    echo "node 1"
    sshpass -p $1 ssh cs4224j@xcnc21.comp.nus.edu.sg "source .bash_profile; cd temp/; cockroach start --store=node2 --join=$3,$4,$5,$6 --listen-addr=$4 --advertise-addr=$4 --http-addr=$4 --cache=.25 --max-sql-memory=.25 --insecure --background;" &
    echo "node 2"
    sshpass -p $1 ssh cs4224j@xcnc22.comp.nus.edu.sg "source .bash_profile; cd temp/; cockroach start --store=node3 --join=$3,$4,$5,$6 --listen-addr=$5 --advertise-addr=$5 --http-addr=$5 --cache=.25 --max-sql-memory=.25 --insecure --background;" &
    echo "node 3"
    sshpass -p $1 ssh cs4224j@xcnc23.comp.nus.edu.sg "source .bash_profile; cd temp/; cockroach start --store=node4 --join=$3,$4,$5,$6 --listen-addr=$6 --advertise-addr=$6 --http-addr=$6 --cache=.25 --max-sql-memory=.25 --insecure --background;" &
    echo "node 4"
    echo "Finish set up cockroachdb on all 4 nodes"
  fi

  # Not going to include the command in this script for initialising cluster in case it has been initialised before.....
}

# ============================START=============================
## $1: Password, $2: Number of servers: 4/5, $3 $4 $5 $6 $7 servers IP Addresses
echo "Starting cockroach on all servers"
startCockroach $1 $2 $3 $4 $5 $6 $7
