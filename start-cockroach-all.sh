#!/bin/bash

startCockroach() {
  echo "Starting cockroach on all 5 nodes"
  yes "\n" | sshpass -p $1 ssh cs4224j@xcnc20.comp.nus.edu.sg "source .bash_profile; cd temp/; ./start-cockroach-node1.sh;" &
  echo "node 1"
  yes "\n" | sshpass -p $1 ssh cs4224j@xcnc21.comp.nus.edu.sg "source .bash_profile; cd temp/; ./start-cockroach-node2.sh;" &
  echo "node 2"
  yes "\n" | sshpass -p $1 ssh cs4224j@xcnc22.comp.nus.edu.sg "source .bash_profile; cd temp/; ./start-cockroach-node3.sh;" &
  echo "node 3"
  yes "\n" | sshpass -p $1 ssh cs4224j@xcnc23.comp.nus.edu.sg "source .bash_profile; cd temp/; ./start-cockroach-node4.sh;" &
  echo "node 4"
  yes "\n" | sshpass -p $1 ssh cs4224j@xcnc24.comp.nus.edu.sg "source .bash_profile; cd temp/; ./start-cockroach-node5.sh; cockroach init --insecure --host=192.168.48.169:50000;" &
  echo "node 5"
  echo "Cockroach initialised"
}

# ============================START=============================
## $1: Password, $2 Number of clients: 20/40, $3 number of servers: 4/5
echo "Starting cockroach on all 5 servers"
startCockroach $1
