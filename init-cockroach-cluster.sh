#!/bin/bash

cockroach init --insecure 

grep 'node starting' node1/logs/cockroach.log -A 11;

echo "Initialise cluster on xcnc20"