package com.tecnico.sec.hds.client.commands.util;

import com.tecnico.sec.hds.util.crypto.ChainHelper;
import com.tecnico.sec.hds.util.crypto.CryptoAgent;
import domain.Transaction;
import io.swagger.client.model.Hash;
import io.swagger.client.model.PubKey;
import io.swagger.client.model.Signature;
import org.bouncycastle.operator.OperatorCreationException;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.function.Consumer;

public class SecurityHelper {
  public final CryptoAgent cryptoAgent;
  public final PubKey key;
  public final ChainHelper chainHelper;
  private Hash lastHash;

  public SecurityHelper(String username, String password) throws GeneralSecurityException, IOException, OperatorCreationException {
    cryptoAgent = new CryptoAgent(username, password);
    key = new PubKey().value(cryptoAgent.getStringPublicKey());
    lastHash = new Hash();
    lastHash.setValue("");
    chainHelper = new ChainHelper();
  }


  public void signMessage(String message, Consumer<Signature> setSignature) throws GeneralSecurityException {

    String sign = cryptoAgent.generateSignature(message);
    Signature signature = new Signature().value(sign);
    setSignature.accept(signature);
  }

  public boolean verifySignature(String message, String signature, String port) throws GeneralSecurityException, IOException {

    return cryptoAgent.verifyBankSignature(message, signature, port);
  }

  public boolean verifyTransactionsSignaturesAndChain(List<Transaction> transactions) throws GeneralSecurityException {

    return cryptoAgent.verifyTransactionsSignature(transactions)
        && chainHelper.verifyTransaction(transactions, key.getValue());
  }

  public Hash getLastHash() {
    return lastHash;
  }

  public void setLastHash(Hash lastHash) {
    this.lastHash = lastHash;
  }

}
