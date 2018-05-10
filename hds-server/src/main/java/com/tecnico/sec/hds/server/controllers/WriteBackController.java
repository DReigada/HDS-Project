package com.tecnico.sec.hds.server.controllers;

import com.tecnico.sec.hds.ServersWrapper;
import com.tecnico.sec.hds.server.controllers.util.ReliableBroadcastHelper;
import com.tecnico.sec.hds.server.db.commands.util.QueryHelpers;
import com.tecnico.sec.hds.server.db.rules.WriteBackRules;
import io.swagger.api.WriteBackApi;
import io.swagger.model.Hash;
import io.swagger.model.WriteBackRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;
import java.util.List;

@Controller
public class WriteBackController implements WriteBackApi {
  private final ReliableBroadcastHelper reliableBroadcastHelper;
  private final WriteBackRules writeBackRules;

  public WriteBackController(ReliableBroadcastHelper reliableBroadcastHelper, ServersWrapper serversWrapper,
                             QueryHelpers queryHelpers) {
    this.reliableBroadcastHelper = reliableBroadcastHelper;
    writeBackRules = new WriteBackRules(serversWrapper, queryHelpers);
  }


  @Override
  public ResponseEntity<Void> writeBack(@Valid @RequestBody WriteBackRequest body) {

    List<Hash> missingTransactions = body.getMissingTransactions();

    System.err.println("Received writeback for account: " + body.getPublicKey().getValue());

    boolean isMissingTransactions = missingTransactions.stream()
        .map(trans -> reliableBroadcastHelper.get(trans.getValue()))
        .anyMatch(opt -> !opt.isPresent());

    if (isMissingTransactions) {
      System.err.println("Starting writeback");
      writeBackRules.doWriteBack(body.getPublicKey().getValue());
    } else {
      System.err.println("No missing transactions in writeback");
    }

    return new ResponseEntity<>(HttpStatus.OK);
  }
}
