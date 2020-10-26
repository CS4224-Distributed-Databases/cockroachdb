#!/bin/bash

cockroach start --insecure --store=node2 --host=192.168.48.170 --port=26258 --http-port=8081 --join=192.168.48.169:26257
