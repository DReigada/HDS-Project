package com.tecnico.sec.hds.server.db.commands.exceptions;

public class DBException extends Exception {
  public DBException(String message, Throwable cause) {
    super(message, cause);
  }

  public DBException(String message) {
    super(message);
  }
}
