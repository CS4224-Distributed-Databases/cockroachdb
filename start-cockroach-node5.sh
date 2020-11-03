#!/bin/bash

server=$(hostname -s)
echo "Starting cockroach on $server"

ip=$(hostname -I)
echo "IP is $ip"

#cockroach start --insecure --background --store=node5 --join=192.168.48.169,192.168.48.170,192.168.48.171,192.168.48.172,192.168.48.173 --listen-addr=27004 --http-addr=8081 --cache=.25 --max-sql-memory=.25
cockroach start --store=node5 --join=192.168.48.169,192.168.48.170,192.168.48.171,192.168.48.172,192.168.48.173 --listen-addr=192.168.48.173:50003 --http-addr=192.168.48.173:9000 --cache=.25 --max-sql-memory=.25 --insecure --background
