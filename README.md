# HDS-Project
HDS Coin - the best cryptocurrency ever created

## Compiling and running the tests

`mvn clean install test`

## Running the server

`mvn -pl hds-server exec:java`

## Running the client

`mvn -pl hds-client exec:java`

### Client commands

#### Register

`register`

#### Send Amount

`send_amount <destination address> <amount>`

#### Check Account

`check_account`

#### Receive Amount

`receive_amount <transaction ID>

#### Audit

`audit <address>`

