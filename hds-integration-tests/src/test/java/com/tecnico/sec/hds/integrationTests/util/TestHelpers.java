package com.tecnico.sec.hds.integrationTests.util;

import com.tecnico.sec.hds.ServersWrapper;
import com.tecnico.sec.hds.app.ServerTypeWrapper;
import com.tecnico.sec.hds.util.Tuple;
import io.swagger.client.model.CheckAccountRequest;
import io.swagger.client.model.CheckAccountResponse;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

public class TestHelpers {


  public static Tuple<CheckAccountResponse, Long> verifyAmount(ServersWrapper server, long expected) {
    Optional<Tuple<CheckAccountResponse, Long>> checkAccount = server.checkAccount(new CheckAccountRequest(), false);

    assertTrue(checkAccount.isPresent());
    assertEquals(expected, (long) checkAccount.get().second);
    return checkAccount.get();
  }

  public static void verifyBadCheck(Optional<Tuple<CheckAccountResponse, Long>> checkAccount){
    assertFalse(checkAccount.isPresent());
  }

  public static void changeServersState(int first, int last, List<ServerTypeWrapper> serversType,
                                        ServerTypeWrapper.ServerType type){
    IntStream.range(first, last).forEach(i -> serversType.get(i).setType(type));
  }

}
