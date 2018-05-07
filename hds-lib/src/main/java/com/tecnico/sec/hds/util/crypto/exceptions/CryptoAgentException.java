package com.tecnico.sec.hds.util.crypto.exceptions;

public class CryptoAgentException extends RuntimeException {
  public CryptoAgentException() {
  }

  public CryptoAgentException(String message) {
    super(message);
  }

  public CryptoAgentException(String message, Throwable cause) {
    super(message, cause);
  }

  public CryptoAgentException(Throwable cause) {
    super(cause);
  }
}
