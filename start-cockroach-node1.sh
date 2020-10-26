#!/bin/bash

server=$(hostname -s)
echo "Starting cockroach on $server"
cockroach start --insecure --host=192.168.48.169
