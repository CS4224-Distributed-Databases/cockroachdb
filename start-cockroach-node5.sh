#!/bin/bash

cockroach start --insecure --store=node5 --host=192.168.48.173 --port=26261 --http-port=8084 --join=192.168.48.169:26257