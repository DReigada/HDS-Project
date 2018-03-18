package com.tecnico.sec.hds.server.db.commands;

@FunctionalInterface
public interface QueryRunner<I, R> {
  R apply(I input) throws DBException;
}
