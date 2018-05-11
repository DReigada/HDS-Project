# HDS-Project
HDS Coin - the best cryptocurrency ever created

## Compiling and running the tests

`mvn clean install test`

## Before Running the Program

There are two files you might want change before running the clients and the servers. The files HDS-Project/hds-client/src/main/resources/conf/servers.conf and HDS-Project/hds-server/src/main/resources/conf/servers.conf must contain the url of all the servers, they have to contain the same urls and they must be in the format:
# http://localhost:818i
# http://localhost:818(i+1)



## Running the server

`mvn -pl hds-server exec:java`

## Running the client

`mvn -pl hds-client exec:java`

### Client commands

When you start the client you will be asked for a username and a password. The username will be used to identify the
files with the keys and the password will be used to cipher your private key.
If it is the first time using an account use register before any other command.
If the command Send Amount or Receive Amount fail you might need to use the Audit command with your public key
to get your most recent transaction hash.


#### Register

`register`

#### Send Amount

`send_amount <destination address> <amount>`

#### Check Account

`check_account`

#### Receive Amount

`receive_amount <transaction ID>`

#### Audit

`audit <public key>`

