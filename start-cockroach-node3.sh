#!/bin/bash

cockroach start --insecure --store=node3 --host=192.168.48.171 --port=26259 --http-port=8082 --join=192.168.48.169:26257