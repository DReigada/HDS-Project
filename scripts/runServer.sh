#!/usr/bin/env bash

mvn -pl hds-server exec:java -Dserver.port="$1"
