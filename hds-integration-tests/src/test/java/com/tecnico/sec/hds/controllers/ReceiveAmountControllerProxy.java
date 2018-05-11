package com.tecnico.sec.hds.controllers;

import com.tecnico.sec.hds.ServersWrapper;
import com.tecnico.sec.hds.app.ServerTypeWrapper;
import com.tecnico.sec.hds.server.controllers.ReceiveAmountController;
import com.tecnico.sec.hds.server.controllers.converters.Converters;
import com.tecnico.sec.hds.server.controllers.util.ReliableBroadcastHelper;
import com.tecnico.sec.hds.server.db.commands.util.QueryHelpers;
import com.tecnico.sec.hds.util.crypto.CryptoAgent;
import io.swagger.api.ReceiveAmountApi;
import io.swagger.client.model.TransactionInformation;
import io.swagger.model.ReceiveAmountRequest;
import io.swagger.model.ReceiveAmountResponse;
import io.swagger.model.Signature;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;
import java.util.Base64;

@Controller
public class ReceiveAmountControllerProxy implements ReceiveAmountApi {

  private final ReliableBroadcastHelper reliableBroadcastHelper;
  private ReceiveAmountController receiveAmountController;

  private final ServersWrapper serversWrapper;

  private ServerTypeWrapper serverTypeWrapper;

  public ReceiveAmountControllerProxy(CryptoAgent cryptoAgent, QueryHelpers queryHelpers, ServersWrapper serversWrapper,
                                      ReliableBroadcastHelper reliableBroadcastHelper, ServerTypeWrapper serverTypeWrapper) {
    this.receiveAmountController = new ReceiveAmountController(cryptoAgent, queryHelpers, serversWrapper, reliableBroadcastHelper);
    this.serversWrapper = serversWrapper;
    this.reliableBroadcastHelper = reliableBroadcastHelper;
    this.serverTypeWrapper = serverTypeWrapper;
  }

  @Override
  public ResponseEntity<ReceiveAmountResponse> receiveAmount(@RequestBody @Valid ReceiveAmountRequest body) {

    ResponseEntity<ReceiveAmountResponse> response;

    switch (serverTypeWrapper.getType()) {
      case NORMAL:
      case BADORDER:
        return receiveAmountController.receiveAmount(body);
      case BYZANTINE:
        return null;
      case BADSIGN:
        response = receiveAmountController.receiveAmount(body);
        response.getBody().setSignature(new Signature().value(Base64.getEncoder().encodeToString("FakeSignature".getBytes())));
      case ECHOS10:
        TransactionInformation trans = Converters.createTransaction(body.getSourceKey().getValue(),
            body.getDestKey().getValue(), body.getAmount(), true, true,
            body.getHash().getValue(), body.getTransHash().getValue(), body.getSignature().getValue());
        serversWrapper.broadcast(reliableBroadcastHelper.createEchoRequest(trans));
        serversWrapper.broadcast(reliableBroadcastHelper.createEchoRequest(trans));
        serversWrapper.broadcast(reliableBroadcastHelper.createEchoRequest(trans));
        serversWrapper.broadcast(reliableBroadcastHelper.createEchoRequest(trans));
        return receiveAmountController.receiveAmount(body);
      default:
        throw new RuntimeException("This should never happen");
    }
  }
}
