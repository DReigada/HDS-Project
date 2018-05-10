package com.tecnico.sec.hds.app.beans;

import com.tecnico.sec.hds.ServersWrapper;
import com.tecnico.sec.hds.app.ServerTypeWrapper;
import com.tecnico.sec.hds.server.app.beans.ApplicationConfig;
import com.tecnico.sec.hds.server.controllers.util.ReliableBroadcastHelper;
import com.tecnico.sec.hds.server.db.commands.util.QueryHelpers;
import com.tecnico.sec.hds.util.crypto.CryptoAgent;
import io.swagger.client.ApiClient;
import io.swagger.client.api.DefaultApi;
import org.bouncycastle.operator.OperatorCreationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.net.InetAddress;
import java.security.GeneralSecurityException;


@Configuration
public class ServerProxyConfig extends ApplicationConfig {
  private static final Logger logger = LoggerFactory.getLogger(ServerProxyConfig.class);


  @Autowired
  private ApplicationArguments arguments;

  @Bean
  public ServerTypeWrapper serverTypeWrapper(){
    return new ServerTypeWrapper(arguments.getSourceArgs()[0]);
  }

  @Bean
  @Override
  public ServersWrapper serversWrapper(){
    return createServersWrapper();
  }

  private ServersWrapper createServersWrapper() {
    try {
      String port = System.getProperty("server.port", "8080");
      String fileName = "banklocalhost_" + port;

      logger.info("Creating keystore on file: " + fileName);

      return new ServersWrapper(fileName, fileName);
    } catch (GeneralSecurityException | OperatorCreationException | IOException e) {
      e.printStackTrace();
      System.exit(1);
      return null;
    }
  }

}
