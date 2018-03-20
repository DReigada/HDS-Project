CREATE TABLE IF NOT EXISTS accounts (
  publicKey VARCHAR PRIMARY KEY,
  balance   FLOAT
);


CREATE TABLE IF NOT EXISTS transactions (
  transID   INTEGER PRIMARY KEY,
  sourceKey VARCHAR,
  destKey   VARCHAR,
  amount    FLOAT,
  pending   BOOL,
  FOREIGN KEY (sourceKey) REFERENCES accounts (publicKey),
  FOREIGN KEY (destKey) REFERENCES accounts (publicKey)
);
