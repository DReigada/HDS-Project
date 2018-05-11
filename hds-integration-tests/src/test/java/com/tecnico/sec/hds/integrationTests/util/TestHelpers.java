package com.tecnico.sec.hds.integrationTests.util;

import com.tecnico.sec.hds.ServersWrapper;
import com.tecnico.sec.hds.util.Tuple;
import io.swagger.client.model.CheckAccountRequest;
import io.swagger.client.model.CheckAccountResponse;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static junit.framework.TestCase.*;

public class TestHelpers {


  public static Tuple<CheckAccountResponse, Long> verifyAmount(ServersWrapper server, long expected) {
    Optional<Tuple<CheckAccountResponse, Long>> checkAccount = server.checkAccount(new CheckAccountRequest(), false);

    assertTrue(checkAccount.isPresent());
    assertEquals(expected, (long) checkAccount.get().second);
    return checkAccount.get();
  }

  public static void verifyBadCheck(Optional<Tuple<CheckAccountResponse, Long>> checkAccount) {
    assertFalse(checkAccount.isPresent());
  }
  
  public static void verifyNumberOfTransactions(ServersWrapper serversWrapper, int expected) {
    Optional<Tuple<CheckAccountResponse, Long>> check_account =
        serversWrapper.checkAccount(new CheckAccountRequest(), false);

    Assert.assertTrue(check_account.isPresent());
    Assert.assertEquals(expected, check_account.get().first.getHistory().size());
  }

  public static void verifyNumberOfPendingTransactions(ServersWrapper client, int expected) {
    Optional<Tuple<CheckAccountResponse, Long>> check_account =
        client.checkAccount(new CheckAccountRequest(), false);

    Assert.assertTrue(check_account.isPresent());
    Assert.assertEquals(expected, check_account.get().first.getPending().size());
  }

  public static <A> void shuffleToDifferent(List<A> list) {
    if (list.size() > 1) {
      ArrayList<A> oldList = new ArrayList<>(list);
      Collections.shuffle(list);
      if (list.equals(oldList)) {
        shuffleToDifferent(list);
      }
    }
  }
}
