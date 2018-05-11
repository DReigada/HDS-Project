package com.tecnico.sec.hds.integrationTests.util;

import com.tecnico.sec.hds.ServersWrapper;
import com.tecnico.sec.hds.util.crypto.ChainHelper;
import io.swagger.client.model.SendAmountRequest;
import io.swagger.client.model.Signature;
import org.bouncycastle.operator.OperatorCreationException;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class ByzantineWrapper extends ServersWrapper {

  public ByzantineWrapper(String user, String pass, List<String> serversUrls) throws IOException, GeneralSecurityException, OperatorCreationException {
    super(user, pass, serversUrls);
  }

  public void sendAmount(SendAmountRequest normalBody, SendAmountRequest changedBody, int n) {
    Set<String> serversNoneByzantine = servers.keySet().stream().skip(n).collect(Collectors.toSet());
    forEachServer(server -> {
      if (serversNoneByzantine.contains(server.getApiClient().getBasePath())) {
        return server.sendAmount(normalBody);
      } else {
        return server.sendAmount(changedBody);
      }
    }).collect(Collectors.toList());
  }

  public SendAmountRequest getSendAmountBody(SendAmountRequest body) throws GeneralSecurityException {
    body.setHash(securityHelper.createHash(
        Optional.of(securityHelper.getLastHash().getValue()),
        Optional.empty(),
        securityHelper.key.getValue(),
        body.getDestKey().getValue(), body.getAmount(),
        ChainHelper.TransactionType.SEND_AMOUNT));

    body.sourceKey(securityHelper.key);

    String message = securityHelper.key.getValue()
        + body.getDestKey().getValue()
        + body.getAmount().toString()
        + body.getHash().getValue();

    securityHelper.signMessage(message, body::setSignature);
    return body;
  }

  public void setLastHash(String hash) {
    securityHelper.getLastHash().setValue(hash);
  }

  public String getLastHash() {
    return super.securityHelper.getLastHash().getValue();
  }

  public Signature signMessage(String message) {
    return new Signature().value(super.securityHelper.cryptoAgent.generateSignature(message));
  }

}