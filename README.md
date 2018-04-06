# HDS-Project
HDS Coin - the best cryptocurrency ever created

## Compiling and running the tests

`mvn clean install test`

## Running the server

`mvn -pl hds-server exec:java`

## Running the client

`mvn -pl hds-client exec:java`

### Client commands

When you start the client you will be asked for a username and a password. They username will be used to identify the
files with the keys and the password will be used to cipher your private key.
If it is the first time using a account use register before any other command.
If the command Send Amount or Receive Amount fail you might need to use the Audit command with your public key
to get your most recent transaction hash.

## Caution
If you use the Audit command with the public key of someone else be sure to
use it again with your public key as this command rewrites the last hash.


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

