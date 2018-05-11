package com.tecnico.sec.hds.controllers;

import com.tecnico.sec.hds.ServersWrapper;
import com.tecnico.sec.hds.app.ServerTypeWrapper;
import com.tecnico.sec.hds.server.controllers.WriteBackController;
import com.tecnico.sec.hds.server.controllers.util.ReliableBroadcastHelper;
import com.tecnico.sec.hds.server.db.commands.util.QueryHelpers;
import io.swagger.api.WriteBackApi;
import io.swagger.model.WriteBackRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;


@Controller
public class WriteBackControllerProxy implements WriteBackApi {

  private WriteBackController writeBackController;

  private ServerTypeWrapper serverTypeWrapper;

  public WriteBackControllerProxy(ReliableBroadcastHelper reliableBroadcastHelper, QueryHelpers queryHelpers,
                                  ServersWrapper serversWrapper, ServerTypeWrapper serverTypeWrapper) {
    this.writeBackController = new WriteBackController(reliableBroadcastHelper, serversWrapper, queryHelpers);
    this.serverTypeWrapper = serverTypeWrapper;
  }

  @Override
  public ResponseEntity<Void> writeBack(@RequestBody @Valid WriteBackRequest body) {
    switch (serverTypeWrapper.getType()) {
      case NORMAL:
        return writeBackController.writeBack(body);
      case BYZANTINE:
        return null;
      case BADSIGN:
        return writeBackController.writeBack(body);
      case IGNORE:
        Thread.currentThread().stop();
      default:
        return writeBackController.writeBack(body);
      //throw new RuntimeException("This should never happen");
    }
  }
}
