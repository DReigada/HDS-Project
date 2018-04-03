PRAGMA foreign_keys = ON;

CREATE TABLE IF NOT EXISTS accounts (
  publicKey VARCHAR PRIMARY KEY NOT NULL,
  balance   FLOAT               NOT NULL
);


CREATE TABLE IF NOT EXISTS transactions (
  transID   INTEGER PRIMARY KEY NOT NULL,
  sourceKey VARCHAR             NOT NULL,
  destKey   VARCHAR             NOT NULL,
  amount    FLOAT               NOT NULL,
  receive   BOOLEAN             NOT NULL CHECK (receive IN (0, 1)),
  signature VARCHAR             NOT NULL,
  hash      VARCHAR             NOT NULL,
  FOREIGN KEY (sourceKey) REFERENCES accounts (publicKey),
  FOREIGN KEY (destKey) REFERENCES accounts (publicKey)
);
