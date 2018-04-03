CREATE TABLE IF NOT EXISTS accounts (
  publicKey VARCHAR PRIMARY KEY NOT NULL,
  balance   FLOAT               NOT NULL
);


CREATE TABLE IF NOT EXISTS transactions (
  transID   INTEGER PRIMARY KEY AUTO_INCREMENT NOT NULL,
  sourceKey VARCHAR                            NOT NULL,
  destKey   VARCHAR                            NOT NULL,
  amount    FLOAT                              NOT NULL,
  pending   BOOLEAN                            NOT NULL,
  receive   BOOLEAN                            NOT NULL,
  signature VARCHAR                            NOT NULL,
  hash      VARCHAR                            NOT NULL,
  FOREIGN KEY (sourceKey) REFERENCES accounts (publicKey),
  FOREIGN KEY (destKey) REFERENCES accounts (publicKey)
);
