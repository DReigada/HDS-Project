CREATE TABLE IF NOT EXISTS accounts (
  publicKey VARCHAR PRIMARY KEY NOT NULL
);


CREATE TABLE IF NOT EXISTS transactions (
  transID      INTEGER PRIMARY KEY AUTO_INCREMENT NOT NULL,
  sourceKey    VARCHAR                            NOT NULL,
  destKey      VARCHAR                            NOT NULL,
  amount       BIGINT                             NOT NULL,
  pending      BOOLEAN                            NOT NULL,
  receive      BOOLEAN                            NOT NULL,
  signature    VARCHAR                            NOT NULL,
  hash         VARCHAR                            NOT NULL,
  receive_hash VARCHAR,
  FOREIGN KEY (sourceKey) REFERENCES accounts (publicKey),
  FOREIGN KEY (destKey) REFERENCES accounts (publicKey)
);

INSERT INTO accounts(publicKey) VALUES('0');