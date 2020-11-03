#!/bin/bash

server=$(hostname -s)
echo "Starting cockroach on $server"

ip=$(hostname -I)
echo "IP is $ip"

#cockroach start --store=node3 --join=192.168.48.169,192.168.48.170,192.168.48.171,192.168.48.172,192.168.48.173 --listen-addr=27002 --http-addr=8081 --cache=.25 --max-sql-memory=.25 --insecure --background
cockroach start --store=node3 --join=192.168.48.169,192.168.48.170,192.168.48.171,192.168.48.172,192.168.48.173 --listen-addr=192.168.48.171:50002 --http-addr=192.168.48.171:9000 --cache=.25 --max-sql-memory=.25 --insecure --background
