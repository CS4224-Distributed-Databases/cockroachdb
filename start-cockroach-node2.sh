#!/bin/bash

server=$(hostname -s)
echo "Starting cockroach on $server"

ip=$(hostname -I)
echo "IP is $ip"

# cockroach start --store=node2 --join=192.168.48.169,192.168.48.170,192.168.48.171,192.168.48.172,192.168.48.173 --listen-addr=27001 --http-addr=8081 --cache=.25 --max-sql-memory=.25 --insecure --background
cockroach start --store=node2 --join=192.168.48.169:50000,192.168.48.170:50001,192.168.48.171:50002,192.168.48.172:50003,192.168.48.173:50004 --listen-addr=192.168.48.170:50001 --advertise-addr=192.168.48.170:50001 --http-addr=192.168.48.170:9000 --cache=.25 --max-sql-memory=.25 --insecure --background
