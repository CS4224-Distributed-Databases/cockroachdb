#!/bin/bash

cockroach start --insecure --store=node4 --host=192.168.48.172 --port=26260 --http-port=8083 --join=192.168.48.169:26257