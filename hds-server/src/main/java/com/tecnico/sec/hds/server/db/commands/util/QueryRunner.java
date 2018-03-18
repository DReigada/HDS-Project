package com.tecnico.sec.hds.server.db.commands.util;

import com.tecnico.sec.hds.server.db.commands.exceptions.DBException;

@FunctionalInterface
public interface QueryRunner<I, R> {
  R apply(I input) throws DBException;
}
